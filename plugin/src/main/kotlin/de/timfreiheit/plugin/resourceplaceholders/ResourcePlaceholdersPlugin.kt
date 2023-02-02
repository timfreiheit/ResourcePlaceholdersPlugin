package de.timfreiheit.plugin.resourceplaceholders

import com.android.build.api.AndroidPluginVersion
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.AndroidSourceSet
import com.android.build.gradle.api.BaseVariant
import com.android.builder.model.SourceProvider
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import java.io.File

class ResourcePlaceholdersPlugin : Plugin<Project> {

    private lateinit var config: ResourcePlaceholdersExtension

    private var pluginConfigured = false
    private var srcDirsAdded = false

    override fun apply(project: Project) {
        config = project.extensions.create(
            "resourcePlaceholders",
            ResourcePlaceholdersExtension::class.java
        )

        checkIfNeedsToAddSrcDirs(project)

        // wait for other plugins added to support applying this before the android plugin
        project.plugins.whenPluginAdded {
            checkIfNeedsToAddSrcDirs(project)

            if (project.state.executed) {
                configure(project)
            } else {
                project.afterEvaluate { _ ->
                    configure(project)
                }
            }
        }
        configure(project)
    }

    private fun configure(project: Project) {
        if (pluginConfigured) {
            // this plugin might be configured multiple times depending on the apply order from other plugins
            return
        }
        project.plugins.all {
            val variants: DomainObjectSet<out BaseVariant>? = when (it) {
                is AppPlugin -> project.extensions.getByType(AppExtension::class.java).applicationVariants
                is LibraryPlugin -> project.extensions.getByType(LibraryExtension::class.java).libraryVariants
                else -> null
            }
            if (variants?.isNotEmpty() == true) {
                pluginConfigured = true
                configureAndroid(project, variants)
            }
        }
    }

    private fun <T : BaseVariant> configureAndroid(project: Project, variants: DomainObjectSet<T>) {
        variants.forEach { variant ->
            var files: FileCollection = project.files()
            variant.sourceSets.forEach { sourceSet ->
                val collectedFiles = searchFilesInSourceSet(sourceSet)
                val fileCollection = project.files(collectedFiles)
                files = files.plus(fileCollection) ?: fileCollection
            }

            val outputDirectory = getOutputDirForVariant(project, variant.name)
            // add new resource folder to sourceSet with the highest priority
            // this makes sure the new icons will override the original one
            val sourceProvider = variant.sourceSets[variant.sourceSets.size - 1]
            if (sourceProvider is AndroidSourceSet) {
                sourceProvider.res.srcDir(outputDirectory)
            } else {
                throw IllegalStateException("sourceProvider is not an AndroidSourceSet")
            }

            val taskName = "resourcePlaceholdersFor${variant.name.capitalize()}"
            val task = project.tasks.create(taskName, ResourcePlaceholdersTask::class.java).apply {
                sources = files
                outputDir = outputDirectory
                placeholders =
                    variant.buildType.manifestPlaceholders + variant.mergedFlavor.manifestPlaceholders.toMutableMap()
                    .apply {
                        put("applicationId", variant.applicationId)
                    }.toMap()
            }

            // register task to make it run before resource merging
            // add dummy folder because the folder is already added to an sourceSet
            // when using the folder defined in the argument the generated resources are at the lowest priority
            // and will cause an conflict with the existing once
            variant.registerGeneratedResFolders(
                project.files(File(outputDirectory, "_dummy")).builtBy(task)
            )
        }
    }

    private fun searchFilesInSourceSet(sourceSet: SourceProvider): List<File> {
        val files = mutableListOf<File>()
        for (resDirectory in sourceSet.resDirectories) {
            config.files.forEach { fileName ->
                val file = File(resDirectory, fileName)
                if (file.exists() && !file.isDirectory) {
                    files.add(file)
                }
            }
        }
        return files
    }

    private fun getOutputDirForVariant(project: Project, variant: String): File {
        return project.file("${project.buildDir}/generated/res/resourcesPlaceholders/$variant")
    }

    /** With the new Variant API we need to add the source dirs before variants to be fully configured */
    private fun checkIfNeedsToAddSrcDirs(project: Project) {
        val extension = project
            .extensions
            .findByType(AndroidComponentsExtension::class.java) ?: return

        if (extension.pluginVersion >= AndroidPluginVersion(7, 3) && !srcDirsAdded) {
            addSrcDirsBeforeVariants(
                extension = extension,
                androidBaseExtension = project.extensions.getByType(BaseExtension::class.java),
                project = project
            )
            srcDirsAdded = true
        }
    }

    private fun addSrcDirsBeforeVariants(
        extension: AndroidComponentsExtension<*, *, *>,
        androidBaseExtension: BaseExtension,
        project: Project
    ) {
        extension.onVariants {
            androidBaseExtension.sourceSets.forEach {
                val dir = getOutputDirForVariant(project, it.name)
                it.res.srcDir(dir)
            }
        }
    }
}

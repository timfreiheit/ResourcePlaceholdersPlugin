package de.timfreiheit.plugin.resourceplaceholders


import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
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

    lateinit var config: ResourcePlaceholdersExtension

    override fun apply(project: Project) {

        config = project.extensions.create("resourcePlaceholders", ResourcePlaceholdersExtension::class.java)

        project.afterEvaluate {
            project.plugins.all {
                when (it) {
                    is AppPlugin -> configureAndroid(project,
                            project.extensions.getByType(AppExtension::class.java).applicationVariants)
                    is LibraryPlugin -> configureAndroid(project,
                            project.extensions.getByType(LibraryExtension::class.java).libraryVariants)
                }
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

            val outputDirectory = getOutputDirForVariant(project, variant)
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
                placeholders = variant.buildType.manifestPlaceholders + variant.mergedFlavor.manifestPlaceholders.toMutableMap().apply {
                    put("applicationId", variant.applicationId)
                }.toMap()
            }

            // register task to make it run before resource merging
            // add dummy folder because the folder is already added to an sourceSet
            // when using the folder defined in the argument the generated resources are at the lowest priority
            // and will cause an conflict with the existing once
            variant.registerGeneratedResFolders(project.files(File(outputDirectory, "_dummy")).builtBy(task))
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

    private fun getOutputDirForVariant(project: Project, variant: BaseVariant): File {
        return project.file("${project.buildDir}/generated/res/resourcesPlaceholders/${variant.flavorName}/${variant.buildType.name}/")
    }

}
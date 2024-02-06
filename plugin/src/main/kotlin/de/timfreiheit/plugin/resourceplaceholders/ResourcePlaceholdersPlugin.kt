package de.timfreiheit.plugin.resourceplaceholders

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

private const val EXTENSION_NAME = "resourcePlaceholders"
internal const val BUILD_FOLDER = "resourcePlaceholders/"

class ResourcePlaceholdersPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create(
            EXTENSION_NAME,
            ResourcePlaceholdersExtension::class.java,
            project,
        )

        // In case the plugin is applied after the android application or library
        project.getAndroidPluginOrNull()?.let {
            configure(project, extension)
        }

        // In case the plugin is applied before the android application or library
        project.plugins.whenPluginAdded {
            if (isAndroidPlugin(it)) {
                configure(project, extension)
            }
        }
    }

    private fun configure(
        project: Project,
        extension: ResourcePlaceholdersExtension
    ) {
        val androidExtension = project.extensions.getByType(AndroidComponentsExtension::class.java)

        androidExtension.onVariants { variant ->

            val taskName =
                "resourcePlaceholdersFor${variant.name.replaceFirstChar { char -> char.uppercase() }}"
            val outputDir = project.layout.buildDirectory.dir(BUILD_FOLDER + variant.name)

            val files = project.getResDirs()

            val task =
                project.tasks.register(taskName, ResourcePlaceholdersTask::class.java) { task ->
                    task.inputs.files(files)
                    task.group = "resource-placeholders"
                    task.variantName = variant.name
                    task.source = variant.sources.res?.all
                    task.overrideFiles.set(extension.files)
                    task.placeholders.set(variant.manifestPlaceholders)
                    task.destination.set(outputDir)
                }

            variant.sources.res?.addGeneratedSourceDirectory(
                task,
                ResourcePlaceholdersTask::destination
            )
        }
    }

    private fun isAndroidPlugin(plugin: Plugin<*>): Boolean {
        return plugin is AndroidBasePlugin
    }

    /**
     * Search for all the directories with .xml files and check if they have a parent called res
     * then returns the res folders
     */
    private fun Project.getResDirs(): List<File> {
        return project.layout.projectDirectory.asFileTree.filter {
            !it.absolutePath.contains(project.layout.buildDirectory.get().asFile.absolutePath) && it.extension == "xml"
        }.mapNotNull { file ->
            var currentDirectory = file.parentFile
            var isInsideResDirectory = false

            while (currentDirectory != null) {
                if (currentDirectory.name == "res") {
                    isInsideResDirectory = true
                    break
                }
                currentDirectory = currentDirectory.parentFile
            }

            if (isInsideResDirectory) currentDirectory else null
        }.distinct()
    }

    private fun Project.getAndroidPluginOrNull(): AndroidBasePlugin? {
        return project.plugins.findPlugin(AndroidBasePlugin::class.java)
    }
}

package de.timfreiheit.plugin.resourceplaceholders

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileTree
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.slf4j.LoggerFactory
import java.io.File

/**
 * tasks must not be final
 */
abstract class ResourcePlaceholdersTask : DefaultTask() {

    private val logger = LoggerFactory.getLogger("Resource Placeholder")

    @get:Internal
    abstract var source: Provider<List<Collection<Directory>>>?

    @get:Input
    abstract val overrideFiles: ListProperty<String>

    @get:Input
    abstract var variantName: String

    @get:OutputDirectory
    abstract val destination: DirectoryProperty

    @get:Input
    abstract val placeholders: MapProperty<String, String>

    @TaskAction
    fun action() {
        source?.get()?.forEach {
            it.forEach {
                logger.info("Source: ${it.asFile.absolutePath}")
            }
        }
        logger.info("Placeholders: ${placeholders.get()}")
        logger.info("Name: $variantName")

        val folder = destination.get()
        cleanBuildDirectory(folder.asFileTree)

        source?.get()?.forEach {
            it.forEach { dir ->
                val collectedFiles = searchFilesInSourceSet(dir)
                collectedFiles.forEach { file ->
                    applyPlaceholders(file)
                }
            }
        }
    }

    private fun cleanBuildDirectory(fileTree: FileTree) {
        fileTree.files.forEach {
            logger.info("Deleting ${it.name}")
            it.deleteRecursively()
        }
    }

    private fun searchFilesInSourceSet(directory: Directory): List<File> {
        val files = mutableListOf<File>()
        overrideFiles.get().forEach { fileName ->
            val file = File(directory.asFile, fileName)
            if (file.exists() && !file.isDirectory) {
                files.add(file)
            }
        }

        return files
    }

    private fun applyPlaceholders(file: File) {
        var content = file.readText(charset = Charsets.UTF_8)

        placeholders.get().forEach { (key, value) ->
            content = content.replace("\${$key}", value.toString())
        }

        val outputFileDir = File(
            destination.get().asFile,
            file.parentFile.name
        )
        outputFileDir.mkdirs()

        val outputFile = File(outputFileDir, file.name)
        outputFile.createNewFile()

        outputFile.writeText(content, charset = Charsets.UTF_8)
        logger.info("Creating ${outputFile.absolutePath}")
    }
}

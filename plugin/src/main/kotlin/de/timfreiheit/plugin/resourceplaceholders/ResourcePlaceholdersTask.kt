package de.timfreiheit.plugin.resourceplaceholders

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import java.io.File

/**
 * tasks must not be final
 */
open class ResourcePlaceholdersTask : DefaultTask() {

    @get:InputFiles
    lateinit var sources: FileCollection

    @get:Input
    lateinit var placeholders: Map<String, Any>

    /**
     * The output directory.
     */
    @get:OutputDirectory
    lateinit var outputDir: File

    @TaskAction
    fun execute() {
        sources
            .associateBy { outputFile(it) }
            .forEach { (outputFile, inputFile) ->
                applyPlaceholders(inputFile, outputFile)
            }
    }

    private fun outputFile(inputFile: File): File {
        val outputFileDir = File(outputDir, inputFile.parentFile.name)
        return File(outputFileDir, inputFile.name)
    }

    private fun applyPlaceholders(inputFile: File, outputFile: File) {
        var content = inputFile.readText(charset = Charsets.UTF_8)

        placeholders.forEach { (key, value) ->
            content = content.replace("\${$key}", value.toString())
        }

        outputFile.apply {
            parentFile.mkdirs()
            createNewFile()
            writeText(content, charset = Charsets.UTF_8)
        }
    }
}

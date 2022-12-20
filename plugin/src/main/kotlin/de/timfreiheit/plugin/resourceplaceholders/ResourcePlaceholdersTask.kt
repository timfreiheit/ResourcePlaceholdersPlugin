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
    fun execute(inputs: IncrementalTaskInputs) {
        for (file in sources) {
            applyPlaceholders(file)
        }
    }

    private fun applyPlaceholders(file: File) {
        var content = file.readText(charset = Charsets.UTF_8)

        placeholders.forEach { (key, value) ->
            content = content.replace("\${$key}", value.toString())
        }

        val outputFileDir = File(outputDir, file.parentFile.name)
        outputFileDir.mkdirs()

        val outputFile = File(outputFileDir, file.name)
        outputFile.createNewFile()

        outputFile.writeText(content, charset = Charsets.UTF_8)
    }
}

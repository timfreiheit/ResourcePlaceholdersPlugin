package de.timfreiheit.plugin.resourceplaceholders

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty

open class ResourcePlaceholdersExtension(project: Project) {
    var files: ListProperty<String> = project.objects.listProperty(String::class.java)
}

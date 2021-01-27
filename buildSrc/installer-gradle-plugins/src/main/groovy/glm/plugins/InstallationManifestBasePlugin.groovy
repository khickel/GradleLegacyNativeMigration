package glm.plugins

import glm.InstallationManifestExtension
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class InstallationManifestBasePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create('installationManifest', InstallationManifestExtension)
    }
}

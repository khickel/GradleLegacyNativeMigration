package glm.plugins

import glm.InstallerExtension
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project

@CompileStatic
class InstallerBasePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create('installer', InstallerExtension)
    }
}

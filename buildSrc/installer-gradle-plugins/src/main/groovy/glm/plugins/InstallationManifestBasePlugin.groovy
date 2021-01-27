package glm.plugins

import glm.InstallationManifest
import groovy.transform.CompileStatic
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.reflect.TypeOf

@CompileStatic
class InstallationManifestBasePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.add(new TypeOf<NamedDomainObjectContainer<InstallationManifest>>() {}, 'installationManifests', project.objects.domainObjectContainer(InstallationManifest))
    }
}

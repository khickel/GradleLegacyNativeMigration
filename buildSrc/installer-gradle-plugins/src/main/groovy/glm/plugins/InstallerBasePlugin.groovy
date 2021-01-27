package glm.plugins

import glm.Installer
import groovy.transform.CompileStatic
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.reflect.TypeOf

@CompileStatic
class InstallerBasePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.add(new TypeOf<NamedDomainObjectContainer<Installer>>() {}, 'installers', project.objects.domainObjectContainer(Installer))
    }
}

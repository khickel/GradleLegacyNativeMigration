package glm.plugins

import glm.InstallationManifest
import glm.InstallationManifestBaseDirectoryFactory
import glm.Installer
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.reflect.TypeOf
import org.gradle.api.tasks.Sync

@CompileStatic
class InstallerBasePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def extension = project.objects.domainObjectContainer(Installer, createInstaller(project))
        project.extensions.add(new TypeOf<NamedDomainObjectContainer<Installer>>() {}, 'installers', extension)

        extension.all { Installer installer ->
            def stageTask = project.tasks.register("stage${installer.name.capitalize()}Installer", Sync, { Sync task ->
                task.with(installer.contentSpec)
                task.destinationDir = project.layout.buildDirectory.dir("tmp/${task.name}").get().asFile
            } as Action<Sync>)

            installer.destinationDirectory.fileProvider(stageTask.map { it.destinationDir }).disallowChanges()
        }
    }

    private static NamedDomainObjectFactory<Installer> createInstaller(Project project) {
        return new NamedDomainObjectFactory<Installer>() {
            @Override
            Installer create(String name) {
                return project.objects.newInstance(Installer, name, project.copySpec(), InstallationManifestBaseDirectoryFactory.forProject(project))
            }
        }
    }
}

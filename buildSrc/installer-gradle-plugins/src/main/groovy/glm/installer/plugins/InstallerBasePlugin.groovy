package glm.installer.plugins


import glm.installer.InstallationManifestBaseDirectoryFactory
import glm.installer.Installer
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.reflect.TypeOf
import org.gradle.api.tasks.Sync

import java.nio.file.Files

@CompileStatic
class InstallerBasePlugin implements Plugin<Project> {
    // Extension type - because of Java's type erasure
    static final TypeOf<NamedDomainObjectContainer<Installer>> INSTALLERS_EXTENSION_TYPE = new TypeOf<NamedDomainObjectContainer<Installer>>() {}

    @Override
    void apply(Project project) {
        // Register installers extension
        def extension = project.objects.domainObjectContainer(Installer, createInstaller(project))
        project.extensions.add(INSTALLERS_EXTENSION_TYPE, 'installers', extension)

        // For each installer...
        extension.all { Installer installer ->
            // Create a staging task to assemble the installer files
            def stageTask = project.tasks.register("stage${installer.name.capitalize()}Installer", Sync, { Sync task ->
                // Use the content spec from the installer to determine which files needs to be copied
                task.with(installer.contentSpec)

                // Stage all files in the task's temporary directory
                task.destinationDir = project.layout.buildDirectory.dir("tmp/${task.name}").get().asFile

                // Keep empty directories intact
                task.includeEmptyDirs = true

                // Ensure that we fail when files overwrite each other
                task.setDuplicatesStrategy(DuplicatesStrategy.FAIL)

                // Additional action that create and assert the specified empty directories
                task.doLast("empty directories", new Action<Task>() {
                    @Override
                    void execute(Task t) {
                        def baseDirectory = task.destinationDir.toPath()
                        installer.emptyDirectories.each {
                            def emptyDirectoryPath = baseDirectory.resolve(it).normalize()

                            // Make sure the empty directory is inside the installer staging directory
                            if (!emptyDirectoryPath.startsWith(baseDirectory)) {
                                throw new IllegalArgumentException("Empty directory '${emptyDirectoryPath}' is outside of the installer staging directory '${baseDirectory}.")
                            }

                            // Create the directories (covers the nested directory case)
                            Files.createDirectories(emptyDirectoryPath)

                            // Make sure the directories are indeed empty
                            Files.list(emptyDirectoryPath).withCloseable {
                                if (it.findFirst().isPresent()) {
                                    throw new IllegalStateException("Directory '${emptyDirectoryPath}' should be empty, but wasn't.")
                                }
                            }
                        }
                    }
                })
            } as Action<Sync>)
            // ... attach the staging task output as the installer staging directory
            installer.destinationDirectory.fileProvider(stageTask.map { it.destinationDir }).disallowChanges()

            // Create lifecycle task for the installer (i.e. debugInstaller)
            project.tasks.register(installerTaskName(installer), { Task task ->
                task.dependsOn(stageTask)
                task.dependsOn(installer.packages*.installerFile)
                task.setGroup('installer')
            } as Action<Task>)
        }
    }

    // Create installer lifecycle task name
    static String installerTaskName(Installer installer) {
        return "${installer.name}Installer"
    }

    // Create factory for installer model
    private static NamedDomainObjectFactory<Installer> createInstaller(Project project) {
        return new NamedDomainObjectFactory<Installer>() {
            @Override
            Installer create(String name) {
                return project.objects.newInstance(Installer, name, project.copySpec(), InstallationManifestBaseDirectoryFactory.forProject(project))
            }
        }
    }
}

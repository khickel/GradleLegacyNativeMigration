package glm

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Transformer
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Usage
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.model.ObjectFactory

import static glm.plugins.InstallationManifestBasePlugin.INSTALLATION_MANIFEST_USAGE_NAME
import static glm.plugins.InstallationManifestBasePlugin.MANIFEST_IDENTITY_ATTRIBUTE

@CompileStatic
final class InstallationManifestBaseDirectoryFactory {
    private final ConfigurationContainer configurations
    private final DependencyHandler dependencies
    private final ObjectFactory objects

    private InstallationManifestBaseDirectoryFactory(ConfigurationContainer configurations, DependencyHandler dependencies, ObjectFactory objects) {
        this.objects = objects
        this.dependencies = dependencies
        this.configurations = configurations
    }

    static InstallationManifestBaseDirectoryFactory forProject(Project project) {
        return new InstallationManifestBaseDirectoryFactory(project.configurations, project.dependencies, project.objects)
    }

    InstallationManifestBaseDirectory create(Object notation, String identity) {
        def configuration = configurations.detachedConfiguration(dependencies.create(notation))
        configuration.canBeConsumed = false
        configuration.canBeResolved = true
        configuration.attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, INSTALLATION_MANIFEST_USAGE_NAME))
        configuration.attributes.attribute(MANIFEST_IDENTITY_ATTRIBUTE, identity)

        def artifactFileProvider = configuration.incoming.files.elements.map(expectSingleFile())

        return new InstallationManifestBaseDirectory(artifactFileProvider)
    }

    private static Transformer<File, Set<FileSystemLocation>> expectSingleFile() {
        return new Transformer<File, Set<FileSystemLocation>>() {
            @Override
            File transform(Set<FileSystemLocation> fileSystemLocations) {
                if (fileSystemLocations.size() == 1) {
                    return fileSystemLocations.first().asFile
                }
                throw new IllegalStateException('More artifacts than expected')
            }
        }
    }
}

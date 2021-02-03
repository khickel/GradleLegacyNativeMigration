package glm.installer

import glm.installer.plugins.InstallationManifestBasePlugin
import groovy.transform.CompileStatic
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Transformer
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.attributes.Usage
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.model.ObjectFactory

import static glm.installer.plugins.InstallationManifestBasePlugin.INSTALLATION_MANIFEST_USAGE_NAME
import static glm.installer.plugins.InstallationManifestBasePlugin.MANIFEST_IDENTITY_ATTRIBUTE

@CompileStatic
final class InstallationManifestBaseDirectoryFactory {
    private final ConfigurationContainer configurations
    private final DependencyHandler dependencies
    private final ObjectFactory objects
    private final String currentProjectPath

    private InstallationManifestBaseDirectoryFactory(String currentProjectPath, ConfigurationContainer configurations, DependencyHandler dependencies, ObjectFactory objects) {
        this.currentProjectPath = currentProjectPath
        this.objects = objects
        this.dependencies = dependencies
        this.configurations = configurations
    }

    static InstallationManifestBaseDirectoryFactory forProject(Project project) {
        return new InstallationManifestBaseDirectoryFactory(project.getPath(), project.configurations, project.dependencies, project.objects)
    }

    /**
     * Creates a manifest base directory for the specified dependency notation and identity.
     * Both are used to select the right installation manifest base directory.
     *
     * @param notation  a dependency notation
     * @param identity  a manifest identity, e.g. name, see {@link InstallationManifest#getName()}
     * @return a manifest base directory
     */
    InstallationManifestBaseDirectory create(Object notation, String identity) {
        // When the notation is the current project, shortcut the dependency engine
        if (isCurrentProject(notation)) {
            def project = (Project) notation
            try {
                // Find the installationManifests extension
                def installationManifests = (NamedDomainObjectContainer<InstallationManifest>)project.extensions.getByName("installationManifests")

                // Find the manifest identity
                def manifest = installationManifests.getByName(identity)

                // Get the manifest staging directory
                def artifactFileProvider = manifest.destinationDirectory.map { Directory it -> it.asFile }
                return new InstallationManifestBaseDirectory(artifactFileProvider)
            } catch (Throwable ex) {
                throw new IllegalArgumentException("Project '${project.path}' does declare any installation manifests, please apply 'glm.installation-manifest-base' and declare your manifests.")
            }
        } else {
            // Else, resolve the notation through the dependency engine
            def configuration = configurations.detachedConfiguration(dependencies.create(notation))

            // This time, the configuration is resolvable, e.g. incoming
            configuration.canBeConsumed = false
            configuration.canBeResolved = true

            // Configure matching attributes with the outgoing manifest
            configuration.attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, INSTALLATION_MANIFEST_USAGE_NAME))
            configuration.attributes.attribute(MANIFEST_IDENTITY_ATTRIBUTE, identity)

            // Map the incoming files into a single base directory (this is what we expect)
            def artifactFileProvider = configuration.incoming.files.elements.map(expectSingleFile())

            return new InstallationManifestBaseDirectory(artifactFileProvider)
        }
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

    private boolean isCurrentProject(Object notation) {
        return notation instanceof Project && notation.path == currentProjectPath
    }
}

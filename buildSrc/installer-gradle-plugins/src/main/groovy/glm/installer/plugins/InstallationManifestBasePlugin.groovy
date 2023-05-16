package glm.installer.plugins

import glm.installer.InstallationManifest
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.model.ObjectFactory
import org.gradle.api.reflect.TypeOf
import org.gradle.api.tasks.Sync

/**
 * The base plugin for declaring installation manifest (e.g. the installer producer).
 * The manifest's purpose is to provide a set of files under a common base root.
 * Each manifest has an identity (e.g. the manifest's name) and is exposed through Gradle's dependency engine.
 * We use the manifest identity when selecting files to build the installer.
 * See {@link Installer#manifest(Object, String, Action)} for more information on selecting a manifests.
 */
@CompileStatic
class InstallationManifestBasePlugin implements Plugin<Project> {
    // Used by the dependency engine - Usage
    static final String INSTALLATION_MANIFEST_USAGE_NAME = 'installation-manifest'

    // Used by the dependency engine - Manifest Identity
    static final Attribute<String> MANIFEST_IDENTITY_ATTRIBUTE = Attribute.of('glm.manifest-identity', String)

    // Extension type - because of Java's type erasure
    static final TypeOf<NamedDomainObjectContainer<InstallationManifest>> INSTALLATION_MANIFESTS_EXTENSION_TYPE = new TypeOf<NamedDomainObjectContainer<InstallationManifest>>() {}

    @Override
    void apply(Project project) {
        // Register installationManifests extension
        def extension = project.objects.domainObjectContainer(InstallationManifest, createInstallationManifest(project))
        project.extensions.add(INSTALLATION_MANIFESTS_EXTENSION_TYPE, 'installationManifests', extension)

        // For each manifests...
        extension.all { InstallationManifest manifest ->
            // Create the outgoing Gradle configuration for the dependency engine
            def manifestElements = project.configurations.create("${manifest.name}InstallationManifestElements", forManifest(manifest.name, project.objects))
            // ... attach the assembled manifest as an outgoing artifact
            manifestElements.outgoing.artifact(manifest.destinationDirectory)

            // Create the staging task
            def stageTask = project.tasks.register("stage${manifest.name.capitalize()}InstallationManifest".toString(), Sync, { Sync task ->
                // Use the content spec from the manifest to determine which files needs to be copied
                task.with(manifest.contentSpec)

                // Ensure that we fail when files overwrite each other
                task.setDuplicatesStrategy(DuplicatesStrategy.FAIL)

                // Stage all files in the task's temporary directory
                task.destinationDir = project.layout.buildDirectory.dir("tmp/${task.name}".toString()).get().asFile
            } as Action<Sync>)
            // ... attach the staging task output as the assembled manifest directory
            // (the `destinationDirectory` will carry the stage task dependency implicitly)
            manifest.destinationDirectory.fileProvider(stageTask.map { it.destinationDir }).disallowChanges()
        }
    }

    // Create a configure action for the Gradle's configuration to expose the manifest to the dependency engine
    private static Action<Configuration> forManifest(String manifestIdentity, ObjectFactory objects) {
        return { Configuration configuration ->
            // Mark the configuration as consumable (e.g. outgoing)
            configuration.setCanBeConsumed(true)
            configuration.setCanBeResolved(false)

            // Configure description for documentation
            configuration.setDescription("Installation manifest elements for '${manifestIdentity}'.")

            // Configure the attributes to allow the right dependency to be selected by the installer
            configuration.attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, INSTALLATION_MANIFEST_USAGE_NAME))
            configuration.attributes.attribute(MANIFEST_IDENTITY_ATTRIBUTE, manifestIdentity)
        } as Action<Configuration>
    }

    // Create factory for installation manifest
    private static NamedDomainObjectFactory<InstallationManifest> createInstallationManifest(Project project) {
        return new NamedDomainObjectFactory<InstallationManifest>() {
            @Override
            InstallationManifest create(String name) {
                return project.objects.newInstance(InstallationManifest, name, project.copySpec())
            }
        }
    }
}

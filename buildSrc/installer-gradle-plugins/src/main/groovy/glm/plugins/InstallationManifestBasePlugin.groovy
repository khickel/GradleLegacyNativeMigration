package glm.plugins

import glm.InstallationManifest
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.api.model.ObjectFactory
import org.gradle.api.reflect.TypeOf

@CompileStatic
class InstallationManifestBasePlugin implements Plugin<Project> {
    static final String INSTALLATION_MANIFEST_USAGE_NAME = 'installation-manifest'

    static final Attribute<String> MANIFEST_IDENTITY_ATTRIBUTE = Attribute.of('glm.manifest-identity', String)

    @Override
    void apply(Project project) {
        def extension = project.objects.domainObjectContainer(InstallationManifest)
        project.extensions.add(new TypeOf<NamedDomainObjectContainer<InstallationManifest>>() {}, 'installationManifests', extension)

        extension.all { InstallationManifest manifest ->
            project.configurations.create("${manifest.name}InstallationManifestElements", forManifest(manifest.name, project.objects))
        }
    }

    private static Action<Configuration> forManifest(String manifestIdentity, ObjectFactory objects) {
        return { Configuration configuration ->
            configuration.setCanBeConsumed(true)
            configuration.setCanBeResolved(false)
            configuration.setDescription("Installation manifest elements for '${manifestIdentity}'.")
            configuration.attributes.attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, INSTALLATION_MANIFEST_USAGE_NAME))
            configuration.attributes.attribute(MANIFEST_IDENTITY_ATTRIBUTE, manifestIdentity)
        } as Action<Configuration>
    }
}

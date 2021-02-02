package glm.installer.plugins

import glm.installer.InstallationManifest
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import spock.lang.Specification
import spock.lang.Subject

import static glm.installer.TestUtils.rootProject

@Subject(InstallationManifestBasePlugin)
class InstallationManifestBasePluginTest extends Specification {
    private final Project project = rootProject()

    def setup() {
        project.apply plugin: 'glm.installation-manifest-base'
    }

    def "registers installation manifest extension"() {
        expect:
        project.extensions.installationManifests instanceof NamedDomainObjectContainer
    }

    def "can create manifests"() {
        when:
        installationManifests.create('debug')
        installationManifests.create('release')

        then:
        installationManifests*.name == ['debug', 'release']
    }

    def "creates consumable configurations based on manifests"() {
        when:
        installationManifests.create('debug')

        then:
        def debugConfiguration = project.configurations.debugInstallationManifestElements
        !debugConfiguration.canBeResolved
        debugConfiguration.canBeConsumed
        debugConfiguration.description == "Installation manifest elements for 'debug'."
        debugConfiguration.attributes.getAttribute(Usage.USAGE_ATTRIBUTE).name == 'installation-manifest'
        debugConfiguration.attributes.getAttribute(Attribute.of('glm.manifest-identity', String)) == 'debug'

        when:
        installationManifests.create('release')

        then:
        def releaseConfiguration = project.configurations.releaseInstallationManifestElements
        !releaseConfiguration.canBeResolved
        releaseConfiguration.canBeConsumed
        releaseConfiguration.description == "Installation manifest elements for 'release'."
        releaseConfiguration.attributes.getAttribute(Usage.USAGE_ATTRIBUTE).name == 'installation-manifest'
        releaseConfiguration.attributes.getAttribute(Attribute.of('glm.manifest-identity', String)) == 'release'
    }

    private NamedDomainObjectContainer<InstallationManifest> getInstallationManifests() {
        return project.extensions.installationManifests
    }
}

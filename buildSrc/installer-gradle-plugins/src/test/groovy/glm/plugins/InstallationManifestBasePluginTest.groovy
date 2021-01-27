package glm.plugins

import glm.InstallationManifest
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import spock.lang.Specification
import spock.lang.Subject

import static glm.plugins.TestUtils.rootProject

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

    private NamedDomainObjectContainer<InstallationManifest> getInstallationManifests() {
        return project.extensions.installationManifests
    }
}

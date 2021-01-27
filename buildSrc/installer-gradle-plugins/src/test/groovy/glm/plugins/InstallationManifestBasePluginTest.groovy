package glm.plugins

import spock.lang.Specification
import spock.lang.Subject

import static glm.plugins.TestUtils.rootProject

@Subject(InstallationManifestBasePlugin)
class InstallationManifestBasePluginTest extends Specification {
    def "can apply plugin using id"() {
        expect:
        rootProject().apply plugin: 'glm.installation-manifest-base'
    }
}

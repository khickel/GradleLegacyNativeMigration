package glm.plugins

import spock.lang.Specification
import spock.lang.Subject

import static glm.plugins.TestUtils.rootProject

@Subject(InstallerBasePlugin)
class InstallerBasePluginTest extends Specification {
    def "can apply plugin using id"() {
        expect:
        rootProject().apply plugin: 'glm.installer-base'
    }
}

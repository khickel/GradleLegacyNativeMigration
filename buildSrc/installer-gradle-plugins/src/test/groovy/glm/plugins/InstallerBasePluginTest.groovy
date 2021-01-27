package glm.plugins


import glm.Installer
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import spock.lang.Specification
import spock.lang.Subject

import static glm.plugins.TestUtils.rootProject

@Subject(InstallerBasePlugin)
class InstallerBasePluginTest extends Specification {
    private final Project project = rootProject()

    def setup() {
        project.apply plugin: 'glm.installer-base'
    }

    def "registers installer extension"() {
        expect:
        project.extensions.installers instanceof NamedDomainObjectContainer
    }

    def "can create installers"() {
        when:
        installers.create('debug')
        installers.create('release')

        then:
        installers*.name == ['debug', 'release']
    }

    private NamedDomainObjectContainer<Installer> getInstallers() {
        return project.extensions.installers
    }
}

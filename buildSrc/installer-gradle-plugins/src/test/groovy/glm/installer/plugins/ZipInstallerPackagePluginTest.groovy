package glm.installer.plugins

import glm.installer.Installer
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import spock.lang.Specification

import static glm.installer.TestUtils.rootProject

class ZipInstallerPackagePluginTest extends Specification {
    private final Project project = rootProject()

    def setup() {
        project.apply plugin: 'glm.zip-installer-package'
    }

    def "apply installer base plugin"() {
        expect:
        project.plugins.hasPlugin('glm.installer-base')
    }

    def "apply installer zip package base plugin"() {
        expect:
        project.plugins.hasPlugin('glm.zip-installer-package-base')
    }

    def "register zip package on each installer"() {
        when:
        installers.create('debug')
        installers.create('release')

        then:
        installers*.packages.flatten()*.name.unique() == ['zip']
    }

    private NamedDomainObjectContainer<Installer> getInstallers() {
        return project.extensions.installers
    }
}

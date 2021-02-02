package glm.installer.fixtures

import glm.installer.Installer
import glm.installer.InstallerPackage
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import spock.lang.Specification

import static glm.installer.fixtures.TestUtils.rootProject

abstract class AbstractInstallerPackagePluginTest extends Specification {
    private final Project project = rootProject()

    protected abstract String getPluginIdUnderTest()
    protected abstract Class<? extends InstallerPackage> getPackageTypeUnderTest()

    def setup() {
        project.apply plugin: pluginIdUnderTest
    }

    def "apply installer base plugin"() {
        expect:
        project.plugins.hasPlugin('glm.installer-base')
    }

    def "apply installer package base plugin"() {
        expect:
        project.plugins.hasPlugin("${pluginIdUnderTest}-base")
    }

    def "register package on each installer"() {
        when:
        installers.create('debug')
        installers.create('release')

        then:
        installers*.packages*.withType(packageTypeUnderTest).flatten()*.getClass().every { packageTypeUnderTest.isAssignableFrom(it) }
    }

    private NamedDomainObjectContainer<Installer> getInstallers() {
        return project.extensions.installers
    }
}

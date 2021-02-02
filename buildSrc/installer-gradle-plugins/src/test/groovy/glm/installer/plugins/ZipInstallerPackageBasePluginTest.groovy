package glm.installer.plugins

import glm.installer.Installer
import glm.installer.ZipInstallerPackage
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import spock.lang.Specification

import static glm.installer.TestUtils.rootProject

class ZipInstallerPackageBasePluginTest extends Specification {
    private final Project project = rootProject()

    def setup() {
        project.apply plugin: 'glm.zip-installer-package-base'
    }

    def "does not apply installer base plugin"() {
        expect:
        !project.plugins.hasPlugin('glm.installer-base')
    }

    def "register zip package type when on each installer"() {
        given:
        project.apply plugin: 'glm.installer-base'

        when:
        installers.create('debug')
        installers.create('release')
        installers*.packages*.create('foo', ZipInstallerPackage)

        then:
        noExceptionThrown()
    }

    def "creates zip tasks for each zip package"() {
        given:
        project.apply plugin: 'glm.installer-base'
        def installer = installers.create('debug')

        when:
        installer.packages.create('foo', ZipInstallerPackage)
        installer.packages.create('bar', ZipInstallerPackage)

        then:
        project.tasks.getByName('createFooDebugInstaller') instanceof Zip
        project.tasks.getByName('createBarDebugInstaller') instanceof Zip
    }

    private NamedDomainObjectContainer<Installer> getInstallers() {
        return project.extensions.installers
    }
}

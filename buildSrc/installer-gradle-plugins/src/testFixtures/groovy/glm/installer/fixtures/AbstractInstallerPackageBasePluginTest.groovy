package glm.installer.fixtures

import glm.installer.Installer
import glm.installer.InstallerPackage
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import spock.lang.Specification

import static glm.installer.fixtures.TestUtils.rootProject

abstract class AbstractInstallerPackageBasePluginTest extends Specification {
    private final Project project = rootProject()

    protected abstract String getPluginIdUnderTest()
    protected abstract Class<? extends InstallerPackage> getPackageTypeUnderTest()
    protected abstract Class<? extends Task> getPackageTaskType()

    def setup() {
        project.apply plugin: pluginIdUnderTest
    }

    def "does not apply installer base plugin"() {
        expect:
        !project.plugins.hasPlugin('glm.installer-base')
    }

    def "register package type when on each installer"() {
        given:
        project.apply plugin: 'glm.installer-base'

        when:
        installers.create('debug')
        installers.create('release')
        installers*.packages*.create('foo', packageTypeUnderTest)

        then:
        noExceptionThrown()
    }

    def "creates tasks for each package"() {
        given:
        project.apply plugin: 'glm.installer-base'
        def installer = installers.create('debug')

        when:
        installer.packages.create('foo', packageTypeUnderTest)
        installer.packages.create('bar', packageTypeUnderTest)

        then:
        packageTaskType.isInstance(project.tasks.getByName('createFooDebugInstaller'))
        packageTaskType.isInstance(project.tasks.getByName('createBarDebugInstaller'))
    }

    private NamedDomainObjectContainer<Installer> getInstallers() {
        return project.extensions.installers
    }
}

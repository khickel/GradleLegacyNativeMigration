package glm.plugins

import glm.InstallationManifestBaseDirectoryFactory
import glm.Installer
import spock.lang.Specification
import spock.lang.Subject

import static glm.plugins.TestUtils.rootProject

@Subject(Installer)
class InstallerTest extends Specification {
    private Installer subject

    def setup() {
        def project = rootProject()
        subject = project.objects.newInstance(Installer, 'test', project.copySpec(), InstallationManifestBaseDirectoryFactory.forProject(project))
    }

    def "throws exception when empty directory path is empty"() {
        when:
        subject.emptyDirectory('')
        then:
        def ex1 = thrown(IllegalArgumentException)
        ex1.message == "'destinationPath' must not be empty"

        when:
        subject.emptyDirectory('  ')
        then:
        def ex2 = thrown(IllegalArgumentException)
        ex2.message == "'destinationPath' must not be empty"
    }

    def "throw exception when empty directory path is null"() {
        when:
        subject.emptyDirectory(null)

        then:
        def ex = thrown(NullPointerException)
        ex.message == "'destinationPath' must not be null"
    }
}

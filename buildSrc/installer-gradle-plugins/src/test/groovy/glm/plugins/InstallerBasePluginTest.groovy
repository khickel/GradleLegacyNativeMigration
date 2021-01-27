package glm.plugins

import glm.InstallationManifestExtension
import glm.InstallerExtension
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
        project.extensions.installer instanceof InstallerExtension
    }
}

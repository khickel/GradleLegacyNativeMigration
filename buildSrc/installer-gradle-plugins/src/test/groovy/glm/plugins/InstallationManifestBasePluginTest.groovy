package glm.plugins

import glm.InstallationManifestExtension
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
        project.extensions.installationManifest instanceof InstallationManifestExtension
    }
}

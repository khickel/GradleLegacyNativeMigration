package glm.lifecycle.plugins

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ReleaseLifecycleBasePluginTest extends Specification {
    def "can apply plugin"() {
        when:
        ProjectBuilder.builder().build().apply plugin: 'glm.release-lifecycle'

        then:
        noExceptionThrown()
    }
}

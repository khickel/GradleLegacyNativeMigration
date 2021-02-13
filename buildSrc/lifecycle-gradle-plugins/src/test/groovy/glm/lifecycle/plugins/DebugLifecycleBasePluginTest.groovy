package glm.lifecycle.plugins

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class DebugLifecycleBasePluginTest extends Specification {
    def "can apply plugin"() {
        when:
        ProjectBuilder.builder().build().apply plugin: 'glm.debug-lifecycle'

        then:
        noExceptionThrown()
    }
}

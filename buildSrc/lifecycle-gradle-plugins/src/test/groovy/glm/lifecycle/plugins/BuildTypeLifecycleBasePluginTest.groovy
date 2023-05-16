package glm.lifecycle.plugins

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class BuildTypeLifecycleBasePluginTest extends Specification {
    def "can apply plugin"() {
        when:
        ProjectBuilder.builder().build().apply plugin: 'glm.build-type-lifecycle-base'

        then:
        noExceptionThrown()
    }
}

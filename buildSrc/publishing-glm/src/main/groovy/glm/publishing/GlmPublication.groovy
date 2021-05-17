package glm.publishing

import groovy.transform.CompileStatic
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.publish.Publication

@CompileStatic
abstract class GlmPublication extends AbstractPublication implements Publication {
    private final String name

    // Use default class
    protected GlmPublication(String name) {
        this.name = name
    }

    @Override
    String getName() {
        return name
    }

    void artifact(Object source) {
        artifacts.from(source)
    }

    abstract ConfigurableFileCollection getArtifacts()

    @Override
    void withoutBuildIdentifier() {
        throw new UnsupportedOperationException()
    }

    @Override
    void withBuildIdentifier() {
        throw new UnsupportedOperationException()
    }
}

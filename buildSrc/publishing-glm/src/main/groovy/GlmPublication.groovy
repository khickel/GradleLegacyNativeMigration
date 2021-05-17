import groovy.transform.CompileStatic

import javax.inject.Inject

@CompileStatic
abstract class GlmPublication extends glm.publishing.GlmPublication {
    @Inject
    GlmPublication(String name) {
        super(name)
    }
}

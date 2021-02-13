package glm.prebuilt

import groovy.transform.ToString
import org.gradle.api.Named
import org.gradle.api.provider.ListProperty

import javax.inject.Inject
import java.util.concurrent.Callable

@ToString
abstract class PrebuiltMavenRepository implements Named {
    private final String name
    private final GradleMetadataCacheBuilder builder

    @Inject
    PrebuiltMavenRepository(String name, GradleMetadataCacheBuilder builder) {
        this.name = name
        this.builder = builder
    }

    String getName() {
        return name
    }

    abstract String getGroup()
    abstract void setGroup(String group)

    Object getUrl() {
        return builder.create(getGroup())
    }
}
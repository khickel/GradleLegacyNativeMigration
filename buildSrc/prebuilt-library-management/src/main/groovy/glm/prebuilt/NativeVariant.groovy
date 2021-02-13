package glm.prebuilt


import org.gradle.api.Named
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty

abstract class NativeVariant implements Named {
    private final String name

    NativeVariant(String name) {
        this.name = name
    }

    String getName() {
        return name
    }

    abstract RegularFileProperty getIncludeRoot()

    abstract MapProperty<Attribute<?>, Object> getAttributes()
}

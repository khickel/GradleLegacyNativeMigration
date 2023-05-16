package glm.prebuilt

import groovy.transform.ToString
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.api.attributes.Attribute
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

import javax.inject.Inject

@ToString
abstract class NativeLibraryComponent implements Named {
    private final String name
    private final ExtensiblePolymorphicDomainObjectContainer<NativeVariant> variants;

    @Inject
    NativeLibraryComponent(String name, ObjectFactory objectFactory) {
        this.name = name
        this.variants = objectFactory.polymorphicDomainObjectContainer(NativeVariant.class)
    }

    String getName() {
        return name
    }

    abstract Property<String> getGroupId()
    abstract Property<String> getArtifactId();
    abstract Property<String> getVersion()
    abstract MapProperty<Attribute<?>, Object> getAttributes()
    PolymorphicDomainObjectContainer<NativeVariant> getVariants() {
        return variants
    }
}

package glm.prebuilt.plugins

import glm.prebuilt.NativeLibraryComponent
import glm.prebuilt.NativeVariant
import glm.prebuilt.PrebuiltLibraryManagement
import glm.prebuilt.SharedLibraryVariant
import glm.prebuilt.StaticLibraryVariant
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project

class PrebuiltLibraryManagementBasePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        def extension = project.getExtensions().create("prebuiltLibraryManagement", PrebuiltLibraryManagement)
        extension.libraries.all { NativeLibraryComponent library ->
            ((ExtensiblePolymorphicDomainObjectContainer<NativeVariant>) library.variants).registerFactory(defaultClass(SharedLibraryVariant), { project.objects.newInstance(defaultClass(SharedLibraryVariant), it) })
            ((ExtensiblePolymorphicDomainObjectContainer<NativeVariant>) library.variants).registerFactory(defaultClass(StaticLibraryVariant), { project.objects.newInstance(defaultClass(StaticLibraryVariant), it) })
        }
    }

    private static <T> Class<? extends T> defaultClass(Class<T> clazz) {
        return Class.forName(clazz.getSimpleName())
    }
}

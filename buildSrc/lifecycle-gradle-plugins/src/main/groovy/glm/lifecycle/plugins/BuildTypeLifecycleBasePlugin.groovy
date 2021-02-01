package glm.lifecycle.plugins

import dev.nokee.platform.base.Binary
import dev.nokee.platform.base.ComponentContainer
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.VariantAwareComponent
import dev.nokee.platform.nativebase.NativeBinary
import dev.nokee.platform.nativebase.internal.DefaultTargetBuildTypeFactory
import dev.nokee.runtime.nativebase.TargetBuildType
import dev.nokee.utils.ProviderUtils
import glm.lifecycle.tasks.BuildTypeLifecycleTask
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import dev.nokee.platform.base.Component
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Provider

import java.util.concurrent.Callable

//@CompileStatic
class BuildTypeLifecycleBasePlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.tasks.withType(BuildTypeLifecycleTask).configureEach { task ->
            // TODO(nokeedev): Provide a way to give access to a TargetBuildTypeFactory instance
            task.dependsOn { allNativeBinaries(allNativeComponents(project.extensions),
                    DefaultTargetBuildTypeFactory.INSTANCE.named(task.buildType.get())) }
        }
    }

    private static Set<Component> allNativeComponents(ExtensionContainer extensions) {
        // TODO(nokeedev): Upgrade to latest nokee to access elements without internal API
        def components = extensions.findByType(ComponentContainer)
        if (components == null) {
            return [] as Set
        }
        return components.elements.get()
    }

    private static Object allNativeBinaries(Set<Component> components, TargetBuildType buildType) {
        def result = components.collect { component ->
            def g = (VariantAwareComponent<Variant>) component
            return g.variants.filter {it.buildVariant.hasAxisOf(buildType) }
                    .map(ProviderUtils.flatMap { Variant it -> it.binaries.get() }) // TODO(nokeedev): Upgrade to latest nokee to access flatTransformEach (which will later become grava)
                    .map(ProviderUtils.filter { NativeBinary it -> it.buildable }).get()
        }.flatten()
        return result
    }
}

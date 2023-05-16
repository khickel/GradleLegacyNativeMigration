package glm.lifecycle.plugins

import dev.nokee.platform.base.*
import dev.nokee.platform.nativebase.internal.DefaultTargetBuildTypeFactory
import dev.nokee.runtime.nativebase.TargetBuildType
import dev.nokee.utils.TransformerUtils
import glm.lifecycle.tasks.BuildTypeLifecycleTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer

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
                    .map(TransformerUtils.flatTransformEach { Variant it -> [it.developmentBinary.get()] })
                    .map(TransformerUtils.matching { Binary it -> it.buildable }).get()
        }.flatten()
        return result
    }
}

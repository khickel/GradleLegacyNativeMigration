package glm.codesigning.plugins

import dev.nokee.platform.base.Binary
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.VariantAwareComponent
import dev.nokee.platform.base.internal.BaseVariant
import dev.nokee.platform.nativebase.*
import glm.codesigning.CodeSigningExtension
import glm.codesigning.SignedBinary
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.specs.Spec

@CompileStatic
abstract /*final*/ class CodeSigningBasePlugin implements Plugin<Project> {
    void apply(Project project) {
        CodeSigningExtension extension = project.extensions.create('codeSigning', CodeSigningExtension)

        project.pluginManager.withPlugin('dev.nokee.c-application',
                configureCodeSigning(project, extension, 'application'))
        project.pluginManager.withPlugin('dev.nokee.c-library',
                configureCodeSigning(project, extension, 'library'))
        project.pluginManager.withPlugin('dev.nokee.cpp-application',
                configureCodeSigning(project, extension, 'application'))
        project.pluginManager.withPlugin('dev.nokee.cpp-library',
                configureCodeSigning(project, extension, 'library'))
        project.pluginManager.withPlugin('dev.nokee.native-application',
                configureCodeSigning(project, extension, 'application'))
        project.pluginManager.withPlugin('dev.nokee.native-library',
                configureCodeSigning(project, extension, 'library'))
    }

    private static Action<AppliedPlugin> configureCodeSigning(Project project, CodeSigningExtension extension, String entryPointName) {
        return { AppliedPlugin pid ->
            configureCodeSigning(project, extension, (VariantAwareComponent<? extends Variant>) project.extensions.getByName(entryPointName))
        } as Action<AppliedPlugin>
    }

    private static void configureCodeSigning(Project project, CodeSigningExtension extension, VariantAwareComponent<Variant> component) {
        component.variants.configureEach { Variant variant ->
            def identifier = ((BaseVariant) variant).identifier.name
            if (variant instanceof NativeApplication) {
                def binary = variant.binaries.filter({ it instanceof ExecutableBinary } as Spec<Binary>).map { (ExecutableBinary) it.first() }
                def signedBinary = new SignedBinary.Executable(extension.sign("executable${identifier.capitalize()}", binary), binary)
                ((ExtensionAware) variant).extensions.add('signedBinary', signedBinary)
            } else if (variant instanceof NativeLibrary) {
                def linkages = ((TargetLinkageAwareComponent) component).linkages
                if (variant.buildVariant.hasAxisOf(linkages.shared)) {
                    def binary = variant.binaries.filter({ it instanceof SharedLibraryBinary } as Spec<Binary>).map { (SharedLibraryBinary) it.first() }
                    def signedBinary = new SignedBinary.SharedLibrary(extension.sign("sharedLibrary${identifier.capitalize()}", binary), binary)
                    ((ExtensionAware) variant).extensions.add('signedBinary', signedBinary)
                }
            }
        }
    }
}

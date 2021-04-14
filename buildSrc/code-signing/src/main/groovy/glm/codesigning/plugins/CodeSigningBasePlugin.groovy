package glm.codesigning.plugins

import dev.nokee.platform.base.Binary
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.VariantAwareComponent
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import glm.codesigning.CodeSigningExtension
import glm.codesigning.SignCode
import glm.codesigning.SignedBinary
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Buildable
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.TaskProvider

import javax.annotation.Nullable

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
        return {
            configureCodeSigning(extension, (VariantAwareComponent<? extends Variant>) project.extensions.getByName(entryPointName))
        } as Action<AppliedPlugin>
    }

    private static void configureCodeSigning(CodeSigningExtension extension, VariantAwareComponent<Variant> component) {
        component.variants.configureEach { Variant variant ->
            variant.binaries.configureEach(ExecutableBinary) { ExecutableBinary binary ->
                def signedBinary = new SignedBinary(extension.sign(binary))
                ((ExtensionAware) variant).extensions.add('signedBinary', signedBinary)
            }
            variant.binaries.configureEach(SharedLibraryBinary) { SharedLibraryBinary binary ->
                def signedBinary = new SignedBinary(extension.sign(binary))
                ((ExtensionAware) variant).extensions.add('signedBinary', signedBinary)
            }
        }
    }
}

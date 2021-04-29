package glm.codesigning.plugins

import dev.nokee.platform.base.Binary
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.VariantAwareComponent
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent
import glm.codesigning.CodeSigningExtension
import glm.codesigning.SignedBinary
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property

@CompileStatic
abstract /*final*/ class CodeSigningPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.pluginManager.apply(CodeSigningBasePlugin)

        def extension = project.extensions.getByType(CodeSigningExtension)
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
        return { AppliedPlugin p ->
            configureCodeSigning(project, extension, ((VariantAwareComponent<Variant>) project.extensions.getByName(entryPointName)))
        } as Action<AppliedPlugin>
    }

    private static void configureCodeSigning(Project project, CodeSigningExtension extension, VariantAwareComponent<Variant> component) {
        extension.signingCertificate.set(project.rootProject.file('MySPC.pfx'))

        def buildTypes = ((TargetBuildTypeAwareComponent) component).buildTypes
        def releaseBuildType = buildTypes.named('release')
        component.variants.configureEach { Variant variant ->
            def signedBinary = ((ExtensionAware) variant).extensions.findByType(SignedBinary)
            if (signedBinary != null && variant.buildVariant.hasAxisOf(releaseBuildType)) {
                ((Property<Binary>) variant.developmentBinary).set(signedBinary)
            }
        }
    }
}

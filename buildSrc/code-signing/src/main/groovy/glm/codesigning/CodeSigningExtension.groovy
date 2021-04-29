package glm.codesigning

import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.NativeBinary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.nativebase.tasks.LinkExecutable
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.Transformer
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.nativeplatform.tasks.AbstractLinkTask

import javax.inject.Inject

// TODO: Use binary.name instead of binary.linkTask.name
@CompileStatic
abstract /*final*/ class CodeSigningExtension {
    abstract DirectoryProperty getCertificateDirectory()

    abstract RegularFileProperty getSigningCertificate()
    abstract Property<String> getSigningCertificatePassword()

    abstract RegularFileProperty getSigntool()

    TaskProvider<SignCode> sign(ExecutableBinary binary) {
        tasks.register("sign${binary.linkTask.name.capitalize()}", SignCode,
                configureTask(binary.linkTask.map(asGradleLinkTask())))
    }

    TaskProvider<SignCode> sign(SharedLibraryBinary binary) {
        tasks.register("sign${binary.linkTask.name.capitalize()}", SignCode,
                configureTask(binary.linkTask.map(asGradleLinkTask())))
    }

    TaskProvider<SignCode> sign(String name, Provider<? extends NativeBinary> binary) {
        return tasks.register("sign${name.capitalize()}", SignCode, configureTask(binary.flatMap {
            if (it instanceof ExecutableBinary) {
                return ((ExecutableBinary) it).linkTask.map(asGradleLinkTask())
            } else {
                return ((SharedLibraryBinary) it).linkTask.map(asGradleLinkTask())
            }
        }))
    }

    private static <T extends Task> Transformer<AbstractLinkTask, T> asGradleLinkTask() {
        return { linkTask -> (AbstractLinkTask) linkTask } as Transformer<AbstractLinkTask, T>
    }

    private Action<SignCode> configureTask(Provider<AbstractLinkTask> linkTask) {
        return { SignCode task ->
            def linkedFile = linkTask.flatMap { it.getLinkedFile() }
            task.getUnsignedFile().set(linkedFile)
            task.getSignedFile().set(layout.buildDirectory.flatMap { Directory buildDir ->
                return buildDir.file(linkedFile
                        .map(extractOutputBaseDirectoryPath(buildDir.dir(baseBuildDirectory(linkTask))))
                        .map(withCodeSignsPrefix()))
            })
            task.signingCertificate.set(signingCertificate)
            task.signingCertificatePassword.set(signingCertificatePassword)
            task.signtoolTool.set(getSigntool())
        } as Action<SignCode>
    }

    private static Transformer<String, RegularFile> extractOutputBaseDirectoryPath(Directory baseDirectory) {
        return { RegularFile file -> baseDirectory.asFile.toPath().relativize(file.asFile.toPath()).toString() } as Transformer<String, RegularFile>
    }

    private static Transformer<String, String> withCodeSignsPrefix() {
        return { "codesigns/${it}" } as Transformer<String, String>
    }

    private static String baseBuildDirectory(Provider<AbstractLinkTask> linkTaskProvider) {
        if (linkTaskProvider.get() instanceof LinkExecutable) {
            return 'exes'
        } else {
            return 'libs'
        }
    }

    @Inject
    protected abstract TaskContainer getTasks()

    @Inject
    protected abstract ProjectLayout getLayout();
}
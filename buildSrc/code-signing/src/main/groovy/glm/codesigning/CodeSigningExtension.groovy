package glm.codesigning

import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.nativebase.tasks.LinkExecutable
import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Transformer
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.nativeplatform.tasks.AbstractLinkTask

import javax.inject.Inject

// TODO: Use binary.name instead of binary.linkTask.name
@CompileStatic
abstract /*final*/ class CodeSigningExtension {
    abstract DirectoryProperty getCertificateDirectory()

    abstract RegularFileProperty getSingingCertificate()

    TaskProvider<SignCode> sign(ExecutableBinary binary) {
        tasks.register("sign${binary.linkTask.name.capitalize()}", SignCode,
                configureTask(binary.linkTask.map { (AbstractLinkTask) it }))
    }

    TaskProvider<SignCode> sign(SharedLibraryBinary binary) {
        tasks.register("sign${binary.linkTask.name.capitalize()}", SignCode,
                configureTask(binary.linkTask.map { (AbstractLinkTask) it }))
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
            task.signingCertificate.set(singingCertificate)
        } as Action<SignCode>
    }

    private static Transformer<String, RegularFile> extractOutputBaseDirectoryPath(Directory baseDirectory) {
        return { RegularFile file -> file.asFile.toPath().relativize(baseDirectory.asFile.toPath()).toString() } as Transformer<String, RegularFile>
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
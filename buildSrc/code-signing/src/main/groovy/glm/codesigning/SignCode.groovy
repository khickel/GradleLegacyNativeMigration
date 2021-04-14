package glm.codesigning

import org.gradle.api.DefaultTask
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec

import javax.inject.Inject
import java.nio.file.Files

abstract /*final*/ class SignCode extends DefaultTask {
    @InputFile
    abstract RegularFileProperty getUnsignedFile()

    @InputFile
    abstract RegularFileProperty getSigningCertificate()

    @OutputFile
    abstract RegularFileProperty getSignedFile()

    @Inject
    protected abstract ExecOperations getExecOperations()

    @TaskAction
    private void doCodeSigning() {
        signedFile.get().asFile.parentFile.mkdirs()
        Files.copy(unsignedFile.asFile.get().toPath(), signedFile.asFile.get().toPath())
        execOperations.exec { ExecSpec spec ->
            spec.commandLine('signtool', 'sign',
                    '/tr', 'http://timestamp.digicert.com',
                    '/td', 'sha256',
                    '/fd', 'sha256',
                    '/f', signingCertificate.get().asFile,
                    '/a', signedFile.get())
        }
    }
}

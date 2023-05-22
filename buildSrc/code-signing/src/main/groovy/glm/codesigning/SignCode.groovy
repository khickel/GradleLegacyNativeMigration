package glm.codesigning

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec

import javax.inject.Inject
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@CompileStatic
abstract /*final*/ class SignCode extends DefaultTask {
    @InputFile
    abstract RegularFileProperty getUnsignedFile()

    @InputFile
    abstract RegularFileProperty getSigningCertificate()

    @InputFile
    abstract RegularFileProperty getSigntoolTool()

    @Internal
    abstract Property<String> getSigningCertificatePassword()

    @OutputFile
    abstract RegularFileProperty getSignedFile()

    @Inject
    protected abstract ExecOperations getExecOperations()

    @TaskAction
    private void doCodeSigning() {
        if(org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.currentOperatingSystem.isWindows()) {

            signedFile.get().asFile.parentFile.mkdirs()
            Files.copy(unsignedFile.asFile.get().toPath(), signedFile.asFile.get().toPath(), StandardCopyOption.REPLACE_EXISTING)

            def outputs = new File(temporaryDir, 'outputs.txt')
            outputs.withOutputStream { outStream ->
                execOperations.exec { ExecSpec spec ->
                    spec.commandLine(signtoolTool.get(), 'sign',
                                     '/debug',
                                     '/tr', 'http://timestamp.digicert.com',
                                     '/td', 'sha256',
                                     '/fd', 'sha256',
                                     '/f', signingCertificate.get().asFile,
                                     '/p', signingCertificatePassword.get(),
                                     signedFile.get().asFile)
                    spec.setStandardOutput(outStream)
                    spec.setErrorOutput(outStream)
                }
            }
        }
    }
}

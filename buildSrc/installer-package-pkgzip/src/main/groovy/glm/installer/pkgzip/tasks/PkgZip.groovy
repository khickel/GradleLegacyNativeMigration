package glm.installer.pkgzip.tasks

import groovy.transform.CompileStatic
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec

import javax.inject.Inject

@CompileStatic
abstract class PkgZip extends DefaultTask {

    @InputDirectory
    abstract DirectoryProperty getSourceDirectory()

    @InputFile
    abstract RegularFileProperty getZNST()

    @InputFile
    abstract RegularFileProperty getBDRY()

    @InputFile
    abstract RegularFileProperty getZSFX()

    @Input
    abstract Property<Boolean> getSilentInstaller()

    @InputFile
    abstract RegularFileProperty getCodeSignCert()

    @Input
    abstract Property<String> getCodeSignPassword()

    @OutputFile
    abstract RegularFileProperty getInstallerFile();

    @Inject
    protected abstract ExecOperations getExecOperations()

    @Input
    abstract Property<String> getZipInputSpec()

    @TaskAction
    void doPkgZip() {
        installerFile.get().asFile.delete() // If the file already exists, the 7z command fails.

        def ZPKG = new File(temporaryDir, "pkg.zip")
        def ZNST = this.ZNST.get().asFile
        def ZSFX = this.ZSFX.get().asFile
        def BDRY = this.BDRY.get().asFile
        def instFile = this.installerFile.get().asFile
        execOperations.exec { ExecSpec spec ->
            spec.workingDir sourceDirectory.get().asFile
            spec.standardOutput = new FileOutputStream(new File(temporaryDir, 'outputs_7z.txt'))
            spec.commandLine('7z', 'a', '-r',
                             ZPKG,
                             zipInputSpec.get())
        }
        execOperations.exec { ExecSpec spec ->
            spec.workingDir(temporaryDir)
            if(silentInstaller.get()) {
                spec.commandLine("cmd", "/C", "copy", "/b", "${ZNST}+${BDRY}+${ZSFX}+${ZPKG}", instFile)
                spec.standardOutput = new FileOutputStream(new File(temporaryDir, 'outputs_copy.txt'))
            } else {
                logger.info("Executing command: cmd /C copy /b ${ZNST}+${BDRY}+${ZSFX}+${ZPKG}, ${instFile.getPath()}")
                spec.commandLine("cmd", "/C", "copy", "/b", "${ZNST}+${BDRY}+${ZSFX}+${ZPKG}", instFile)
                spec.standardOutput = new FileOutputStream(new File(temporaryDir, 'outputs_copy.txt'))
            }
        }
        execOperations.exec { ExecSpec spec ->
            spec.workingDir(temporaryDir)
            spec.standardOutput = new FileOutputStream(new File(temporaryDir, 'outputs_sign.txt'))
            spec.commandLine('signtool', 'sign', '/fd', 'SHA256', '/a', '/v', '/f', codeSignCert.get().asFile,
                    '/p', codeSignPassword.get(), instFile.name)
        }
    }
}

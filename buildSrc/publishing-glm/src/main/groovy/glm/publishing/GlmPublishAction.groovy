package glm.publishing

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

import java.nio.file.CopyOption
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest

@PackageScope
@CompileStatic
abstract class GlmPublishAction implements WorkAction<Parameter> {
    @Override
    void execute() {
        writeArtifact()
        writeHash(MessageDigestAlgorithms.SHA3_512)
        writeHash(MessageDigestAlgorithms.SHA_256)
    }

    private void writeArtifact() {
        def artifactFile = parameters.artifactFile.get().asFile
        Files.copy(artifactFile.toPath(), outputFile(artifactFile.name).toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    private void writeHash(String algorithm) {
        outputFile(forHash(algorithm)).text = Hex.encodeHexString(DigestUtils.digest(DigestUtils.getDigest(algorithm), parameters.artifactFile.get().asFile))
    }

    private String forHash(String algorithm) {
        // Strip off the file extension and append the sha's extension.
        // This assums that the incoming filename includes an extension, but that is
        // always the case for this project.
        String name = parameters.artifactFile.asFile.get().name
        name = name.take(name.lastIndexOf('.'))
        return "${name}.${fileExtension(algorithm)}"
    }

    private static String fileExtension(String algorithm) {
        switch (algorithm) {
            case MessageDigestAlgorithms.SHA3_512: return 'sha3-256'
            case MessageDigestAlgorithms.SHA_256: return 'sha512'
            default: throw new UnsupportedOperationException('Unsupported digest')
        }
    }

    private File outputFile(String fileName) {
        return parameters.publishingDirectory.get().file(fileName).asFile
    }

    static interface Parameter extends WorkParameters {
        RegularFileProperty getArtifactFile()
        DirectoryProperty getPublishingDirectory()
    }
}

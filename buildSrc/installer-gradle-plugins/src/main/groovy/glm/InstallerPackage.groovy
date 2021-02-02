package glm

import groovy.transform.CompileStatic
import org.gradle.api.Named
import org.gradle.api.file.RegularFileProperty

@CompileStatic
abstract class InstallerPackage implements Named {
    private final String name

    protected InstallerPackage(String name) {
        this.name = name
    }

    String getName() {
        return name
    }

    abstract RegularFileProperty getInstallerFile()
}

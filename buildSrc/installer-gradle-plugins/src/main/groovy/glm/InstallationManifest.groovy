package glm

import org.gradle.api.Named

abstract class InstallationManifest implements Named {
    private final String name

    InstallationManifest(String name) {
        this.name = name
    }

    String getName() {
        return name
    }
}

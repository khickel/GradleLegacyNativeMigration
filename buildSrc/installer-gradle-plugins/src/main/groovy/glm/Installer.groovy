package glm

import org.gradle.api.Named

abstract class Installer implements Named {
    private final String name

    Installer(String name) {
        this.name = name
    }

    String getName() {
        return name
    }
}

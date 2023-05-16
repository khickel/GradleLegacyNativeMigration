package glm.lifecycle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal

abstract class BuildTypeLifecycleTask extends DefaultTask {
    BuildTypeLifecycleTask() {
        buildType.convention(name).forUseAtConfigurationTime()
    }

    @Internal
    abstract Property<String> getBuildType()
}

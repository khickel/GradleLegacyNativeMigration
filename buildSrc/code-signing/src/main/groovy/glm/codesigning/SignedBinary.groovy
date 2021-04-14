package glm.codesigning;

import dev.nokee.platform.base.Binary;
import org.gradle.api.Buildable
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

final class SignedBinary implements Binary, Buildable {
    private final TaskProvider<SignCode> signTask

    SignedBinary(TaskProvider<SignCode> signTask) {
        this.signTask = signTask
    }

    TaskProvider<SignCode> getSignTask() {
        return signTask
    }

    @Override
    TaskDependency getBuildDependencies() {
        return { [signTask.get()] as Set<? extends Task> } as TaskDependency
    }
}
package glm.codesigning

import dev.nokee.platform.base.Binary
import dev.nokee.platform.nativebase.NativeBinary
import dev.nokee.platform.nativebase.internal.HasOutputFile
import groovy.transform.CompileStatic
import org.gradle.api.Buildable
import org.gradle.api.Task
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.TaskProvider

@CompileStatic
final class SignedBinary implements Binary, Buildable, HasOutputFile {
    private final TaskProvider<SignCode> signTask
    private final Provider<NativeBinary> binary

    SignedBinary(TaskProvider<SignCode> signTask, Provider<NativeBinary> binary) {
        this.binary = binary
        this.signTask = signTask
    }

    TaskProvider<SignCode> getSignTask() {
        return signTask
    }

    boolean isBuildable() {
        return binary.get().buildable
    }

    @Override
    TaskDependency getBuildDependencies() {
        return { [signTask.get()] as Set<? extends Task> } as TaskDependency
    }

    @Override
    Provider<RegularFile> getOutputFile() {
        return signTask.flatMap { it.signedFile }
    }
}
package glm.codesigning

import dev.nokee.language.base.tasks.SourceCompile
import dev.nokee.platform.base.Binary
import dev.nokee.platform.base.TaskView
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.NativeBinary
import dev.nokee.platform.nativebase.SharedLibraryBinary
import dev.nokee.platform.nativebase.internal.HasHeaderSearchPaths
import dev.nokee.platform.nativebase.internal.HasOutputFile
import dev.nokee.platform.nativebase.tasks.LinkExecutable
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary
import groovy.transform.CompileStatic
import org.gradle.api.Buildable
import org.gradle.api.Task
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskDependency
import org.gradle.api.tasks.TaskProvider

@CompileStatic
abstract class SignedBinary implements NativeBinary, Buildable, HasOutputFile, HasHeaderSearchPaths {
    private final TaskProvider<SignCode> signTask
    private final Provider<? extends NativeBinary> binary

    SignedBinary(TaskProvider<SignCode> signTask, Provider<? extends NativeBinary> binary) {
        this.binary = binary
        this.signTask = signTask
    }

    TaskProvider<SignCode> getSignTask() {
        return signTask
    }

    @Override
    TaskView<SourceCompile> getCompileTasks() {
        return binary.get().compileTasks
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

    @Override
    Provider<Set<FileSystemLocation>> getHeaderSearchPaths() {
        return ((HasHeaderSearchPaths) binary.get()).headerSearchPaths
    }

    static final class Executable extends SignedBinary implements ExecutableBinary {
        private final Provider<ExecutableBinary> binary

        Executable(TaskProvider<SignCode> signTask, Provider<ExecutableBinary> binary) {
            super(signTask, binary)
            this.binary = binary
        }

        @Override
        TaskProvider<LinkExecutable> getLinkTask() {
            return ((ExecutableBinary) binary.get()).linkTask
        }
    }

    static final class SharedLibrary extends SignedBinary implements SharedLibraryBinary {
        private final Provider<SharedLibraryBinary> binary

        SharedLibrary(TaskProvider<SignCode> signTask, Provider<SharedLibraryBinary> binary) {
            super(signTask, binary)
            this.binary = binary
        }

        @Override
        TaskProvider<LinkSharedLibrary> getLinkTask() {
            return ((SharedLibraryBinary) binary.get()).linkTask
        }
    }
}
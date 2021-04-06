package dev.nokeesamples.sonarqube;

import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.internal.provider.DefaultProvider;
import org.gradle.api.provider.Provider;
import org.gradle.language.c.tasks.CCompile;
import org.gradle.language.cpp.tasks.CppCompile;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.objectivec.tasks.ObjectiveCCompile;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.Clang;
import org.gradle.nativeplatform.toolchain.Gcc;
import org.gradle.nativeplatform.toolchain.VisualCpp;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.ToolType;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

public class SonarQubeBuildWrapperUtils {
    public static String getCurrentVersion() {
        // Version check is pluginInfo.pluginVersion.startsWith("version" + ".") so using only major version is fine
        return "6";
    }

    public static String getDefaultBuildWrapperFileName() {
        return "build-wrapper-dump.json";
    }

    public static Callable<Object> captureIfPresentInTaskGraph(AbstractNativeCompileTask task) {
        return () -> {
            task.getProject().getTasks().named("generateSonarqube", GenerateSonarQubeBuildWrapperTask.class).configure(it -> {
                it.getCaptures().add(captureToolChainVersion(task));
                it.getCaptures().addAll(capture(task));
            });
            return Collections.emptyList();
        };
    }

    private static Provider<SonarQubeBuildWrapper.Capture> captureToolChainVersion(AbstractNativeCompileTask task) {
        return new DefaultProvider<>(() -> {
            val executable = executable(task);
            val stdOut = new ByteArrayOutputStream();
            val stdErr = new ByteArrayOutputStream();
            task.getProject().exec(spec -> {
                spec.commandLine(executable);
                if (!executable.endsWith("cl.exe")) {
                    spec.args("--version");
                }
                spec.setStandardOutput(stdOut);
                spec.setErrorOutput(stdErr);
            }).assertNormalExitValue();

            return new SonarQubeBuildWrapper.Capture(compiler(task), executable, stdOut.toString(), stdErr.toString(), null, null, null);
        });
    }

    private static Provider<Iterable<SonarQubeBuildWrapper.Capture>> capture(AbstractNativeCompileTask task) {
        return new DefaultProvider<>(() -> toCapture(task));
    }

    private static Iterable<SonarQubeBuildWrapper.Capture> toCapture(AbstractNativeCompileTask task) {
        val compiler = compiler(task);
        val cwd = task.getProject().getProjectDir().getAbsolutePath();
        val executable = executable(task);
        val generalCmd = load(optionFile(task));
        val env = environmentVariables();
        return task.getSource().getFiles().stream().map(it -> {
            val builder = SonarQubeBuildWrapper.Capture.builder()
                    .compiler(compiler)
                    .cwd(cwd)
                    .executable(executable)
                    .env(env);
            builder.cmd(executable);
            generalCmd.forEach(builder::cmd);
            builder.cmd(it.getAbsolutePath());
            return builder.build();
        }).collect(toList());
    }

    private static File optionFile(AbstractNativeCompileTask task) {
        return new File(task.getTemporaryDir(), "options.txt");
    }

    @SneakyThrows
    private static List<String> load(File optionsFile) {
        return Files.lines(optionsFile.toPath(), UTF_8).map(SonarQubeBuildWrapperUtils::unquote).collect(toList());
    }

    private static List<String> environmentVariables() {
        return System.getenv().entrySet().stream().map(it -> it.getKey() + "=" + it.getValue()).collect(toList());
    }

    private static String executable(AbstractNativeCompileTask task) {
        return ((NativeToolChainInternal)task.getToolChain().get()).select((NativePlatformInternal) task.getTargetPlatform().get()).locateTool(compilerType(task)).getTool().getAbsolutePath();
    }

    private static String compiler(AbstractNativeCompileTask task) {
        if (task.getToolChain().get() instanceof VisualCpp) {
            return "msvc-cl";
        } else if (task.getToolChain().get() instanceof Clang) {
            return "clang";
        } else if (task.getToolChain().get() instanceof Gcc) {
            return "clang";
        }
        throw new UnsupportedOperationException("Please fill the value for the other compiler type...");
    }

    private static ToolType compilerType(AbstractNativeCompileTask task) {
        if (task instanceof CppCompile) {
            return ToolType.CPP_COMPILER;
        } else if (task instanceof CCompile) {
            return ToolType.C_COMPILER;
        } else if (task instanceof ObjectiveCCompile) {
            return ToolType.OBJECTIVEC_COMPILER;
        }
        throw new UnsupportedOperationException("Native compile task not supported...");
    }

    private static String unquote(String s) {
        if (s.startsWith("\"") && s.endsWith("\"")) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }
}

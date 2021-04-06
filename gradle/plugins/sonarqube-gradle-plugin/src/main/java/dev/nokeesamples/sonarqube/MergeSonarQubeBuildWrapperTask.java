package dev.nokeesamples.sonarqube;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.val;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static dev.nokeesamples.sonarqube.SonarQubeBuildWrapperUtils.getCurrentVersion;
import static java.nio.charset.StandardCharsets.UTF_8;

public abstract class MergeSonarQubeBuildWrapperTask extends DefaultTask {
    @InputFiles
    public abstract ConfigurableFileCollection getSources();

    @OutputFile
    public abstract RegularFileProperty getBuildWrapperFile();

    @TaskAction
    public void doMerge() throws IOException {
        val builder = SonarQubeBuildWrapper.builder().version(getCurrentVersion());
        for (File source : getSources().getFiles()) {
            val buildWrapper = new Gson().fromJson(Files.newBufferedReader(source.toPath(), UTF_8), SonarQubeBuildWrapper.class);
            buildWrapper.getCaptures().forEach(builder::capture);
        }
        Files.write(getBuildWrapperFile().getAsFile().get().toPath(), new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(builder.build()).replace("/", "\\/").getBytes(UTF_8));
    }
}

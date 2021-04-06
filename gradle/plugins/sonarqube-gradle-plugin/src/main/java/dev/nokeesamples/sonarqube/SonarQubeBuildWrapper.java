package dev.nokeesamples.sonarqube;

import groovy.lang.Singleton;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import java.util.List;

@Value
@Builder
public class SonarQubeBuildWrapper {
    String version;
    @Singular List<Capture> captures;

    @Value
    @Builder
    public static class Capture {
        @Input @Optional String compiler;
        @Input @Optional String executable;
        @Input @Optional String stdout;
        @Input @Optional String stderr;
        @Input @Optional @Singular("cmd") List<String> cmd;
        @Input @Optional String cwd;
        @Input @Optional List<String> env;
    }
}
package glm.prebuilt;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import dev.gradleplugins.grava.publish.metadata.GradleModuleMetadata;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.file.Directory;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static dev.gradleplugins.grava.publish.metadata.GradleModuleMetadata.Attribute.ofAttribute;
import static dev.gradleplugins.grava.publish.metadata.GradleModuleMetadata.Component.ofComponent;
import static dev.gradleplugins.grava.publish.metadata.GradleModuleMetadata.CreatedBy.ofGradle;
import static dev.gradleplugins.grava.publish.metadata.GradleModuleMetadata.withWriter;

final class GradleMetadataCache implements Callable<URI> {
    private final Set<NativeLibraryComponent> libraries;
    private final Provider<Directory> cachingDirectory;
    private boolean initialized = false;
    private final Object lock = new Object();

    GradleMetadataCache(Set<NativeLibraryComponent> libraries, Provider<Directory> cachingDirectory) {
        this.cachingDirectory = cachingDirectory;
        this.libraries = libraries;
    }

    private void initialize() {
        if (!initialized) {
            synchronized (lock) {
                if (!initialized) {
                    build();
                    initialized = true;
                }
            }
        }
    }

    private List<NativeVariant> getVariants() {
        List<NativeVariant> variants = new ArrayList<>();
        libraries.forEach(it -> variants.addAll(it.getVariants()));
        return variants;
    }

    private void build() {
        try {
            libraries.forEach(this::writeMetadata);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void writeMetadata(NativeLibraryComponent library) {
        File moduleFile = moduleFile(library);
        try {
            withWriter(moduleFile, writer -> {
                File moduleDirectory = moduleFile.getParentFile();
                GradleModuleMetadata.Builder builder = GradleModuleMetadata.builder();
                builder.formatVersion("1.1");
                builder.component(ofComponent(library.getGroupId().get(), library.getArtifactId().get(), library.getVersion().get()));
                builder.createdBy(ofGradle("4.3", "abc123"));
                library.getVariants().forEach(it -> {
                    builder.localVariant(api(moduleDirectory, it))
                            .localVariant(link(moduleDirectory, it))
                            .localVariant(runtime(moduleDirectory, it));
                });

                try {
                    writer.write(builder.build());
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Consumer<GradleModuleMetadata.LocalVariant.Builder> api(File moduleDirectory, NativeVariant variant) {
        return builder -> {
            builder.name("api" + StringUtils.capitalize(variant.getName()));
            builder.attribute(ofAttribute("org.gradle.usage", "cplusplus-api"));
            variant.getAttributes().get().forEach((Attribute<?> k, Object v) -> builder.attribute(ofAttribute(k.getName(), v)));
            variant.getIncludeRoot().map(it -> builder.file(artifactFile(moduleDirectory, it.getAsFile()))).getOrNull();
        };
    }

    private Consumer<GradleModuleMetadata.LocalVariant.Builder> link(File moduleDirectory, NativeVariant variant) {
        return builder -> {
            if (variant instanceof SharedLibraryVariant) {
                builder.name("linkShared" + StringUtils.capitalize(variant.getName()));
                ((SharedLibraryVariant) variant).getImportLibraryFile().map(it -> builder.file(artifactFile(moduleDirectory, it.getAsFile()))).getOrNull();
            } else if (variant instanceof StaticLibraryVariant) {
                builder.name("linkStatic" + StringUtils.capitalize(variant.getName()));
                ((StaticLibraryVariant) variant).getLibraryFile().map(it -> builder.file(artifactFile(moduleDirectory, it.getAsFile()))).getOrNull();
            } else {
                builder.name("link" + StringUtils.capitalize(variant.getName()));
            }
            builder.attribute(ofAttribute("org.gradle.usage", "native-link"));
            variant.getAttributes().get().forEach((Attribute<?> k, Object v) -> builder.attribute(ofAttribute(k.getName(), v)));
        };
    }

    private Consumer<GradleModuleMetadata.LocalVariant.Builder> runtime(File moduleDirectory, NativeVariant variant) {
        return builder -> {
            if (variant instanceof SharedLibraryVariant) {
                builder.name("runtimeShared" + StringUtils.capitalize(variant.getName()));
                ((SharedLibraryVariant) variant).getRuntimeLibraryFile().map(it -> builder.file(artifactFile(moduleDirectory, it.getAsFile()))).getOrNull();
            } else if (variant instanceof StaticLibraryVariant) {
                builder.name("runtimeStatic" + StringUtils.capitalize(variant.getName()));
            } else {
                builder.name("runtime" + StringUtils.capitalize(variant.getName()));
            }
            builder.attribute(ofAttribute("org.gradle.usage", "native-runtime"));
            variant.getAttributes().get().forEach((Attribute<?> k, Object v) -> builder.attribute(ofAttribute(k.getName(), v)));
        };
    }

    private Consumer<GradleModuleMetadata.File.Builder> artifactFile(File moduleDirectory, File target) {
        return builder -> {
            try {
                if (target.isFile()) {
                    builder.name(ByteSource.wrap(target.getAbsolutePath().getBytes(StandardCharsets.UTF_8)).hash(Hashing.md5()).toString() + "-" + target.getName());
                } else {
                    builder.name(target.getName());
                }
                builder.url(relativize(moduleDirectory, target));

                ByteSource byteSource = ByteSource.wrap(target.getAbsolutePath().getBytes(StandardCharsets.UTF_8));
                if (target.isFile()) {
                    byteSource = Files.asByteSource(target);
                }
                builder.size(byteSource.size());
                builder.sha1(byteSource.hash(Hashing.sha1()).toString());
                builder.md5(byteSource.hash(Hashing.md5()).toString());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static String relativize(File moduleDirectory, File target) {
        StringBuilder pathBackToRoot = new StringBuilder();

        long n = moduleDirectory.getAbsolutePath().replace('\\', '/').split("/").length;
        for (int i = 0; i < n; i++) {
            pathBackToRoot.append("../");
        }
        return pathBackToRoot.toString() + target.getAbsolutePath().replace('\\', '/');
    }

    private File moduleFile(NativeLibraryComponent library) {
        File result = cachingDirectory.get().file(library.getGroupId().get().replace('.', '/') + "/" + library.getArtifactId().get() + "/" + library.getVersion().get() + "/" + library.getArtifactId().get() + "-" + library.getVersion().get() + ".module").getAsFile();
        result.getParentFile().mkdirs();
        return result;
    }

    @Override
    public URI call() throws Exception {
        initialize();
        return cachingDirectory.get().getAsFile().toURI();
    }
}

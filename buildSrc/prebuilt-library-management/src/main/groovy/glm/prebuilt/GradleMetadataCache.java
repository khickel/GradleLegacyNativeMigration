package glm.prebuilt;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import dev.gradleplugins.grava.publish.metadata.GradleModuleMetadata;
import dev.gradleplugins.grava.publish.metadata.GradleModuleMetadataWriter;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

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
        try {
            write(new MainModule(library));
            for (NativeVariant variant : library.getVariants()) {
                write(new VariantModule(library, variant));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }

    private interface ModuleFile {
        File getFile();
    }

    @SuppressWarnings("unchecked")
    private static void write(ModuleFile module) throws IOException {
        File file = module.getFile();
        Files.createParentDirs(file);
        withWriter(file, (Consumer<GradleModuleMetadataWriter>) module);
    }

    private final class MainModule implements ModuleFile, Consumer<GradleModuleMetadataWriter> {
        private final GradleModuleMetadata.Builder builder = GradleModuleMetadata.builder();
        private final NativeLibraryComponent library;

        MainModule(NativeLibraryComponent library) {
            this.library = library;
        }

        @Override
        public void accept(GradleModuleMetadataWriter writer) {
            builder.formatVersion("1.1");
            builder.component(ofComponent(library.getGroupId().get(), library.getArtifactId().get(), library.getVersion().get()));
            builder.createdBy(ofGradle("4.3", "abc123"));
            library.getVariants().forEach(this::registerRemoteVariants);

            try {
                writer.write(builder.build());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private void registerRemoteVariants(NativeVariant variant) {
            builder.remoteVariant(api(variant))
                    .remoteVariant(link(variant))
                    .remoteVariant(runtime(variant));
        }

        private Consumer<GradleModuleMetadata.RemoteVariant.Builder> api(NativeVariant variant) {
            return named("api-" + variant.getName())
                    .andThen(availableAt(variant))
                    .andThen(attributes(ImmutableMap.<Attribute<?>, Object>builder()
                            .put(Usage.USAGE_ATTRIBUTE, Usage.C_PLUS_PLUS_API)
                            .putAll(variant.getAttributes().get())
                            .build()));
        }

        private Consumer<GradleModuleMetadata.RemoteVariant.Builder> link(NativeVariant variant) {
            return named("link-" + variant.getName())
                    .andThen(availableAt(variant))
                    .andThen(attributes(ImmutableMap.<Attribute<?>, Object>builder()
                            .put(Usage.USAGE_ATTRIBUTE, Usage.NATIVE_LINK)
                            .putAll(variant.getAttributes().get())
                            .build()));
        }

        private Consumer<GradleModuleMetadata.RemoteVariant.Builder> runtime(NativeVariant variant) {
            return named("runtime-" + variant.getName())
                    .andThen(availableAt(variant))
                    .andThen(attributes(ImmutableMap.<Attribute<?>, Object>builder()
                            .put(Usage.USAGE_ATTRIBUTE, Usage.NATIVE_RUNTIME)
                            .putAll(variant.getAttributes().get())
                            .build()));
        }

        private Consumer<GradleModuleMetadata.RemoteVariant.Builder> attributes(Map<Attribute<?>, Object> attributes) {
            return builder -> {
                attributes.forEach((k, v) -> builder.attribute(GradleModuleMetadata.Attribute.ofAttribute(k.getName(), v)));
            };
        }

        private Consumer<GradleModuleMetadata.RemoteVariant.Builder> availableAt(NativeVariant variant) {
            return builder -> builder.availableAt(it -> it.group(library.getGroupId().get())
                    .module(library.getArtifactId().get() + "_" + variant.getName())
                    .version(library.getVersion().get())
                    .url("../../" + library.getArtifactId().get() + "_" + variant.getName() + "/" + library.getVersion().get() + "/" + library.getArtifactId().get() + "_" + variant.getName() + "-" + library.getVersion().get() + ".module"));
        }

        private Consumer<GradleModuleMetadata.RemoteVariant.Builder> named(String name) {
            return builder -> builder.name(name);
        }

        @Override
        public File getFile() {
            return cachingDirectory.get().file(group(library) + "/" + library.getArtifactId().get() + "/" + library.getVersion().get() + "/" + library.getArtifactId().get() + "-" + library.getVersion().get() + ".module").getAsFile();
        }
    }

    private final class VariantModule implements ModuleFile, Consumer<GradleModuleMetadataWriter> {
        private final GradleModuleMetadata.Builder builder = GradleModuleMetadata.builder();
        private final NativeLibraryComponent library;
        private final NativeVariant variant;

        VariantModule(NativeLibraryComponent library, NativeVariant variant) {
            this.library = library;
            this.variant = variant;
        }

        @Override
        public void accept(GradleModuleMetadataWriter writer) {
            builder.formatVersion("1.1");
            builder.component(ofComponent(library.getGroupId().get(),
                    getModuleName(),
                    library.getVersion().get()));
            builder.createdBy(ofGradle("4.3", "abc123"));
            registerLocalVariants(variant);

            try {
                writer.write(builder.build());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        private void registerLocalVariants(NativeVariant variant) {
            builder.localVariant(api(variant))
                    .localVariant(link(variant))
                    .localVariant(runtime(variant));
        }

        private Consumer<GradleModuleMetadata.LocalVariant.Builder> api(NativeVariant variant) {
            return named("api-" + variant.getName())
                    .andThen(file(variant.getIncludeRoot()))
                    .andThen(attributes(ImmutableMap.<Attribute<?>, Object>builder()
                            .put(Usage.USAGE_ATTRIBUTE, Usage.C_PLUS_PLUS_API)
                            .putAll(variant.getAttributes().get())
                            .build()));
        }

        private Consumer<GradleModuleMetadata.LocalVariant.Builder> link(NativeVariant variant) {
            Consumer<GradleModuleMetadata.LocalVariant.Builder> result = named("link-" + variant.getName())
                    .andThen(attributes(ImmutableMap.<Attribute<?>, Object>builder()
                            .put(Usage.USAGE_ATTRIBUTE, Usage.NATIVE_LINK)
                            .putAll(variant.getAttributes().get())
                            .build()));
            if (variant instanceof SharedLibraryVariant) {
                return result.andThen(file(((SharedLibraryVariant) variant).getImportLibraryFile()));
            } else if (variant instanceof StaticLibraryVariant) {
                return result.andThen(file(((StaticLibraryVariant) variant).getLibraryFile()));
            } else {
                return result;
            }
        }

        private Consumer<GradleModuleMetadata.LocalVariant.Builder> runtime(NativeVariant variant) {
            Consumer<GradleModuleMetadata.LocalVariant.Builder> result = named("runtime-" + variant.getName())
                    .andThen(attributes(ImmutableMap.<Attribute<?>, Object>builder()
                            .put(Usage.USAGE_ATTRIBUTE, Usage.NATIVE_RUNTIME)
                            .putAll(variant.getAttributes().get())
                            .build()));
            if (variant instanceof SharedLibraryVariant) {
                return result.andThen(file(((SharedLibraryVariant) variant).getRuntimeLibraryFile()));
            } else {
                return result;
            }
        }

        private Consumer<GradleModuleMetadata.LocalVariant.Builder> file(Provider<? extends FileSystemLocation> target) {
            return builder -> {
                if (target.isPresent()) {
                    builder.file(artifactFile(getFile().getParentFile(), target.get().getAsFile()));
                }
            };
        }

        private Consumer<GradleModuleMetadata.File.Builder> artifactFile(File moduleDirectory, File target) {
            return builder -> {
                try {
                    builder.name(target.getName());
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

        private Consumer<GradleModuleMetadata.LocalVariant.Builder> attributes(Map<Attribute<?>, Object> attributes) {
            return builder -> {
                attributes.forEach((k, v) -> builder.attribute(GradleModuleMetadata.Attribute.ofAttribute(k.getName(), v)));
            };
        }

        private Consumer<GradleModuleMetadata.LocalVariant.Builder> named(String name) {
            return builder -> builder.name(name);
        }

        private String getModuleName() {
            return library.getArtifactId().get() + "_" + variant.getName();
        }

        @Override
        public File getFile() {
            return cachingDirectory.get().file(group(library) + "/" + getModuleName() + "/" + library.getVersion().get() + "/" + getModuleName() + "-" + library.getVersion().get() + ".module").getAsFile();
        }
    }

    private static String relativize(File moduleDirectory, File target) {
        StringBuilder pathBackToRoot = new StringBuilder();

        long n = absolutePathWithoutLeadingPathSeparator(moduleDirectory).split("/").length;
        for (int i = 0; i < n; i++) {
            pathBackToRoot.append("../");
        }

        return pathBackToRoot + absolutePathWithoutLeadingPathSeparator(target);
    }

    // Account for *nix absolute path starting with a forward slash
    private static String absolutePathWithoutLeadingPathSeparator(File target) {
        String pathToTarget = target.getAbsolutePath().replace('\\', '/');
        if (pathToTarget.startsWith("/")) {
            pathToTarget = pathToTarget.substring(1);
        }
        return pathToTarget;
    }

    private static String group(NativeLibraryComponent library) {
        return library.getGroupId().get().replace('.', '/');
    }

    @Override
    public URI call() throws Exception {
        initialize();
        return cachingDirectory.get().getAsFile().toURI();
    }
}

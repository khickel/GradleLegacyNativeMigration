package glm.prebuilt

import dev.gradleplugins.grava.publish.metadata.GradleModuleMetadata
import groovy.transform.CompileStatic
import org.gradle.api.attributes.Attribute
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

import java.util.concurrent.Callable
import java.util.function.Consumer

import static dev.gradleplugins.grava.publish.metadata.GradleModuleMetadata.Attribute.ofAttribute
import static dev.gradleplugins.grava.publish.metadata.GradleModuleMetadata.Component.ofComponent
import static dev.gradleplugins.grava.publish.metadata.GradleModuleMetadata.CreatedBy.ofGradle
import static dev.gradleplugins.grava.publish.metadata.GradleModuleMetadata.withWriter

@CompileStatic
final class GradleMetadataCache implements Callable<URI> {
    private final Set<NativeLibraryComponent> libraries
    private final Provider<Directory> cachingDirectory
    private boolean initialized = false
    private final Object lock = new Object()

    GradleMetadataCache(Set<NativeLibraryComponent> libraries, Provider<Directory> cachingDirectory) {
        this.cachingDirectory = cachingDirectory
        this.libraries = libraries
    }

    private void initialize() {
        if (!initialized) {
            synchronized (lock) {
                if (!initialized) {
                    build()
                    initialized = true
                }
            }
        }
    }

    private List<NativeVariant> getVariants() {
        List<NativeVariant> variants = []
        libraries.each {variants.addAll(it.variants) }
        return variants
    }

    private void build() {
        try {
            libraries.each { NativeLibraryComponent library ->
                writeMetadata(library)
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace()
        }
    }

    private void writeMetadata(NativeLibraryComponent library) {
        def moduleFile = moduleFile(library)
        withWriter(moduleFile) {
            def moduleDirectory = moduleFile.parentFile
            def builder = GradleModuleMetadata.builder()
            builder.formatVersion("1.1")
            builder.component(ofComponent(library.groupId.get(), library.artifactId.get(), library.version.get()))
            builder.createdBy(ofGradle("4.3", "abc123"))
            library.variants.each {
                builder.localVariant(api(moduleDirectory, it))
                        .localVariant(link(moduleDirectory, it))
                        .localVariant(runtime(moduleDirectory, it))
            }

            it.write(builder.build())
        }
    }

    private Consumer<GradleModuleMetadata.LocalVariant.Builder> api(File moduleDirectory, NativeVariant variant) {
        return { GradleModuleMetadata.LocalVariant.Builder builder ->
            builder.name("apiShared${variant.name.capitalize()}")
            builder.attribute(ofAttribute('org.gradle.usage', 'cplusplus-api'))
            variant.attributes.get().forEach { Attribute<?> k, Object v -> builder.attribute(ofAttribute(k.name, v)) }
            variant.includeRoot.map { Directory it -> builder.file(artifactFile(moduleDirectory, it.asFile)) }.orNull
        } as Consumer
    }

    private Consumer<GradleModuleMetadata.LocalVariant.Builder> link(File moduleDirectory, NativeVariant variant) {
        return { GradleModuleMetadata.LocalVariant.Builder builder ->
            if (variant instanceof SharedLibraryVariant) {
                builder.name("linkShared${variant.name.capitalize()}")
                variant.importLibraryFile.map { RegularFile it -> builder.file(artifactFile(moduleDirectory, it.asFile)) }.orNull
            } else if (variant instanceof StaticLibraryVariant) {
                builder.name("linkStatic${variant.name.capitalize()}")
                variant.libraryFile.map { RegularFile it -> builder.file(artifactFile(moduleDirectory, it.asFile)) }.orNull
            } else {
                builder.name("link${variant.name.capitalize()}")
            }
            builder.attribute(ofAttribute('org.gradle.usage', 'native-link'))
            variant.attributes.get().forEach { Attribute<?> k, Object v -> builder.attribute(ofAttribute(k.name, v)) }
        } as Consumer
    }

    private Consumer<GradleModuleMetadata.LocalVariant.Builder> runtime(File moduleDirectory, NativeVariant variant) {
        return { GradleModuleMetadata.LocalVariant.Builder builder ->
            if (variant instanceof SharedLibraryVariant) {
                builder.name("runtimeShared${variant.name.capitalize()}")
                variant.runtimeLibraryFile.map { RegularFile it -> builder.file(artifactFile(moduleDirectory, it.asFile)) }.orNull
            } else if (variant instanceof StaticLibraryVariant) {
                builder.name("runtimeStatic${variant.name.capitalize()}")
            } else {
                builder.name("runtime${variant.name.capitalize()}")
            }
            builder.attribute(ofAttribute('org.gradle.usage', 'native-runtime'))
            variant.attributes.get().forEach { Attribute<?> k, Object v -> builder.attribute(ofAttribute(k.name, v)) }
        } as Consumer
    }

    private Consumer<GradleModuleMetadata.File.Builder> artifactFile(File moduleDirectory, File target) {
        return { GradleModuleMetadata.File.Builder builder ->
            builder.name(target.name)
            builder.url(relativize(moduleDirectory, target))
            builder.size(0)
            builder.sha1('TODO')
            builder.md5('TODO')
        } as Consumer
    }

    private static String relativize(File moduleDirectory, File target) {
        def pathBackToRoot = []

        def n = moduleDirectory.absolutePath.replace('\\', '/').split('/').length
        n.times {
            pathBackToRoot << '..'
        }
        return pathBackToRoot.join('/') + '/' + target.absolutePath.replace('\\', '/')
    }

    private File moduleFile(NativeLibraryComponent library) {
        def result = cachingDirectory.get().file("${library.groupId.get().replace('.', '/')}/${library.artifactId.get()}/${library.version.get()}/${library.artifactId.get()}-${library.version.get()}.module").asFile
        result.parentFile.mkdirs()
        return result
    }

    @Override
    URI call() throws Exception {
        initialize()
        return cachingDirectory.get().asFile.toURI()
    }
}

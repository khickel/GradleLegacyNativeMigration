package glm

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.file.CopySpec
import org.gradle.api.model.ObjectFactory
import org.gradle.util.ConfigureUtil

import javax.inject.Inject

@CompileStatic
abstract class InstallerSpec {
    private final CopySpec spec
    private final ObjectFactory objects

    @Inject
    InstallerSpec(CopySpec spec, ObjectFactory objects) {
        this.objects = objects
        this.spec = spec
    }

    InstallerSpec from(String... sourcePaths) {
        spec.from(sourcePaths)
        return this
    }

    InstallerSpec from(String sourcePath, Action<? super CopySpec> action) {
        spec.from(sourcePath, action)
        return this
    }

    InstallerSpec into(String destinationPath, Action<? super CopySpec> action) {
        spec.into(destinationPath, action)
        return this
    }

    protected static class Spec implements CopySpec {
        @Delegate private final CopySpec delegate
        private final InstallationManifestBaseDirectory baseDirectory
        private final ObjectFactory objects

        @Inject
        Spec(InstallationManifestBaseDirectory baseDirectory, CopySpec delegate, ObjectFactory objects) {
            this.objects = objects
            this.baseDirectory = baseDirectory
            this.delegate = delegate
        }

        @Override
        CopySpec from(Object... sourcePaths) {
            return delegate.from(sourcePaths.collect { baseDirectory.file(it.toString()) })
        }

        @Override
        CopySpec from(Object sourcePath, Closure c) {
            return delegate.from(baseDirectory.file(sourcePath.toString()), c)
        }

        @Override
        CopySpec from(Object sourcePath, Action<? super CopySpec> configureAction) {
            return delegate.from(baseDirectory.file(sourcePath.toString()), configureAction)
        }

        @Override
        CopySpec into(Object destPath, Closure closure) {
            return into(destPath, ConfigureUtil.configureUsing(closure))
        }

        @Override
        CopySpec into(Object destPath, Action<? super CopySpec> action) {
            return delegate.into(destPath, new Action<CopySpec>() {
                @Override
                void execute(CopySpec spec) {
                    action.execute(objects.newInstance(Spec, baseDirectory, spec))
                }
            });
        }
    }
}

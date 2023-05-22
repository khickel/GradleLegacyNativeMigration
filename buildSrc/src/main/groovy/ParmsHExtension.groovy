import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

interface ParmsHExtension {
    Property<String> getProgramDescription()
    Property<String> getDefaultPath()
    Property<String> getServiceName()
    Property<String> getRunProgram()
    Property<String> getProductName()
    Property<String> getProductVersion()

    RegularFileProperty getParmsHeaderFile()
}

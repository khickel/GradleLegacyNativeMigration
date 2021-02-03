import javax.inject.Inject

abstract class SharedLibraryVariant extends glm.prebuilt.SharedLibraryVariant {
    @Inject
    SharedLibraryVariant(String name) {
        super(name)
    }
}

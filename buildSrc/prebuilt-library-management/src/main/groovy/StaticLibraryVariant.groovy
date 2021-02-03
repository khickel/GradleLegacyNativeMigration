import javax.inject.Inject

abstract class StaticLibraryVariant extends glm.prebuilt.StaticLibraryVariant{
    @Inject
    StaticLibraryVariant(String name) {
        super(name)
    }
}

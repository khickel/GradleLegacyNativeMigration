# GradleLegacyNativeMigration
 Example of migrating a legacy native body of source to Gradle

This is an attempt to create an example of migrating an existing C/C++ project from makefiles to Gradle, and dealing with all the non-Gradle-way things that are required.

The goal is to be able to swap to a new build system without having to refactor everything else at the same time, once that is done, then it becomes easir to move the project to a newer style layout (if desired).

There are still rough edges, some things that don't work, and certainly some things that could be improved.

There are two .bat files in scripts that I use to avoid having to create gradlew.bat stubs in every child directory, someone else might find them to be useful.

ISSUES:

* There seems to be something wrong where after a clean checkout or gradle clean, I have to run "gradlew debug" or "gradlew release" 3-4 times before it stops executing tasks.
** I need to add an example showing the issue that happens because Gradle uses a case-sensitive compare for the include file names, even on windows.  There are issues where the Microsoft Windows SDK uses camel case in an include statement, but the actual file on disk is all in lower case.
* The afterEvaluate call in build.gradle causes the deprecation error below, I'm not sure how to best resolve that.

Using method Project.afterEvaluate(Closure) when the project is already evaluated has been deprecated. This will fail with an error in Gradle 7.0. The configuration given is ignored because the project has already been evaluated. To apply this configuration, remove afterEvaluate.

* I need to add an optional target that demonstrates the issue where the build fails because the native Gradle plugin forces /TP on the C/C++ compiler command line.

* Add a gradle file that doesn't produce a binary, but has a text file it wants to include in someones installer.

* Add example that creates a .h file for the application. Ask how to properly make the compile task depend on the creation of the .h file (it seems to work, but is it reliable).

* Add example and text for a manual test whjere stageDebug doesn't recopy the target file if it was manually deleted, unless it had to rebuild the input file.


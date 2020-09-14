# GradleLegacyNativeMigration
 Example of migrating a legacy native body of source to Gradle

This is an attempt to create an example of migrating an existing C/C++ project from makefiles to Gradle, and dealing with all the non-Gradle-way things that are required.

The goal is to be able to swap to a new build system without having to refactor everything else at the same time, once that is done, then it becomes easier to move the project to a newer style layout (if desired).

There are still rough edges, some things that don't work, and certainly some things that could be improved.

There are two .bat files in scripts that I use to avoid having to create gradlew.bat stubs in every child directory, someone else might find them to be useful.

ISSUES:

* After a clean checkout or gradle clean, I have to run "gradlew debug" or "gradlew release" several times before it stops executing tasks, because Gradle
uses a case-sensitive compare for the include file names, even on windows.
There are issues where the Microsoft Windows SDK uses camel case in an include statement, but the actual file on disk is all in lower case.
To see this, remove the .gradle directory, then run "gradlew debug", then run "gradle debug --info", in the output you will see messages like the one below. In a more complex project, you may have to run gradle 7 or 8 times before it stops building things.
Note that sometimes the second time it won't say that it executed any tasks, but if you look at the info output, you'll see that it though it was not up to date.
>Task ':subsystem_b:server_1:compileDebugCpp' is not up-to-date because:
>  Input property 'headerDependencies' file D:\BMCTools\Gradle\GradleLegacyNativeMigration\static_lib\lib_one.h has been removed.
>  Input property 'headerDependencies' file D:\BMCTools\Gradle\GradleLegacyNativeMigration\static_lib\lib_One.h has been added.

* The afterEvaluate call in build.gradle causes the deprecation error below, I'm not sure how to best resolve that.
>Using method Project.afterEvaluate(Closure) when the project is already evaluated has been deprecated. This will fail with an error in Gradle 7.0. The configuration given is ignored because the project has already been evaluated. To apply this configuration, remove afterEvaluate.

* I need to add an optional target that demonstrates the issue where the build fails because the native Gradle plugin forces /TP on the C/C++ compiler command line.

* Add a gradle file that doesn't produce a binary, but has a text file it wants to include in another project's installer.

* Add example for a manual test where stageDebug doesn't recopy the target file if it was manually deleted, unless it had to rebuild the input file.


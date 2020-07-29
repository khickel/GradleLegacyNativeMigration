# GradleLegacyNativeMigration
 Example of migrating a legacy native body of source to Gradle

This is an attempt to create an example of migrating an existing C/C++ project from makefiles to Gradle, and dealing with all the non-Gradle-way things that are required.

The goal is to be able to swap to a new build system without having to refactor everything else at the same time, once that is done, then it becomes easir to move the project to a newer style layout (if desired).

There are still rough edges, some things that don't work, and certainly some things that could be improved.


ISSUES:

* There seems to be something wrong where after a clean checkout or gradle clean, I have to run "gradlew debug" or "gradlew release" 3-4 times before it stops executing tasks.
* The afterEvaluate call in build.gradle causes the deprecation error below, I'm not sure how to best resolve that.

Using method Project.afterEvaluate(Closure) when the project is already evaluated has been deprecated. This will fail with an error in Gradle 7.0. The configuration given is ignored because the project has already been evaluated. To apply this configuration, remove afterEvaluate.


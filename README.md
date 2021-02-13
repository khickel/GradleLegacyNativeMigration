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
  Note that sometimes the second time it won't say that it executed any tasks, but if you look at the info output, you'll see that gradle thought that the subproject  was not up to date.
```>Task ':subsystem_b:server_1:compileDebugCpp' is not up-to-date because:

>  Input property 'headerDependencies' file \GradleLegacyNativeMigration\static_lib\lib_one.h has been removed.

>  Input property 'headerDependencies' file \GradleLegacyNativeMigration\static_lib\lib_One.h has been added.

```
This is related to the recently closed ticket https://github.com/gradle/gradle/issues/7930


* The afterEvaluate call in build.gradle causes the deprecation error below, I'm not sure how to best resolve that.
>Using method Project.afterEvaluate(Closure) when the project is already evaluated has been deprecated. This will fail with an error in Gradle 7.0. The configuration given is ignored because the project has already been evaluated. To apply this configuration, remove afterEvaluate.

* In order to make code analysis in Visual Studio work you can comment int the line with '/analyze' in commonNativeUtils.gradle, then do a
  rebuild of the solution, it would be better to somehow make that automatic or controlled by a gradle parm.

* Gradle adds /TP to every visual studio C or C++ compilation command, this seems to be incorrect or  at least undesireable.
  By default, visual studio determines it based on the file extension, but once /TP or /TC is specified on the
  command line, there is no way to revert back to the default behavior.
  For my project, when there is a new Gradle release, I download the source,
  then edit the flie listed below, replacing the line in  'return "/TP";' with 'return "";'.
  If /TP was not specified then any user that relied on that behavior could
  simply add /TP from their .gradle file.
> src\platform-native\org\gradle\nativeplatform\toolchain\internal\msvcpp\CppCompiler.java

* Code signing note. These examples use a generated self signing cert to sign the code. The cert was created with this script: https://stackoverflow.com/questions/53771022/how-to-create-and-install-x-509-self-signed-certificates-in-windows-10-without-u
  It should go without saying, but this is not a secure certificate or CA, and they should not be used for anything else. Further, this project does not claim to demonstrate a secure way to sign anything, for any purpose.

* TODO When we add .c files to the solution the way that we are, they don't end up under Source Files for the project in the Solution Explorer, try to fix that.

* TODO Add example for a manual test where stageDebug doesn't recopy the target file if it was manually deleted, unless it had to rebuild the input file. I haven't yet been able to recreate this in this stripped down example.


* TODO:KHICKEL seting env vars, include download link for unzipsfx.exe, say how to get openssl.
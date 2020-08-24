@echo off
setlocal

:: Wrapper file for gradle wrapper, the idea is that you put this .bat somewhere on your
:: path, and also have find_file_in_parent.bat on your path.
:: Then if you run gradlew.bat from a sub-directory of a gradle workspace,
:: it will find gradlew.bat in the parent dir tree and execute it for you.
:: This way you don't have to create a stub gradlew.bat file in every directory
:: under the one that contains the real gradle wrapper.

for /F "delims=" %%i in ('find_file_in_parent.bat gradlew.bat') do set GW_CMD=%%i
if NOT DEFINED GW_CMD goto :not_found

echo Running gradlew with this command: %GW_CMD% %*
%GW_CMD% %*
goto :EOF

:not_found
echo Unable to find the file gradlew.bat in any parent directory, make sure that it exists.
exit /B 1
goto :EOF

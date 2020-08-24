@echo off

setlocal
:: Adapted from https://stackoverflow.com/questions/33799607/how-to-traverse-up-directory-structure-using-batch-file
:: Look in the current dir (%CD%) for the specified file, if not found, look in the parent.
:: repeat until the file is found or we hit the root.
:: Output the full path to the file, or "not found".


REM If this batch file was re-executed from itself: goto right part
if DEFINED FIND_FILE_LOOP goto loop

set FIND_FILE_TARGET=%1

:: Check to see if the target file is in the current directory
if EXIST %FIND_FILE_TARGET% goto :found_it

:: The target file does not exist in the current directory, loop upwards through the dir tree.
set FIND_FILE_LOOP=1
cmd /C "%~F0" %*
exit /b

:loop
setlocal EnableDelayedExpansion

REM -- NOTE: Infinite loop, breaks out when root directory is reached --
REM -- or makefile is found                                           --
for /L %%n in () do (
   if exist %FIND_FILE_TARGET% (
      call :found_it
      exit
   ) else (
      if "!cd:~3,1!" equ "" ( 
        echo not found
        exit
      )

      REM -- Go to parent directory --
      cd ..
   )
)
exit

:found_it
echo %CD%\%FIND_FILE_TARGET%
goto :EOF

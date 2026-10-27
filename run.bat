@echo off
setlocal
cd /d "%~dp0"
title Memory Hacker

rem Use JAVA_HOME if it points to a JDK
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "PATH=%JAVA_HOME%\bin;%PATH%"

where javac >nul 2>nul
if errorlevel 1 goto nojdk

if not exist bin mkdir bin
echo Compiling...
javac -encoding UTF-8 -sourcepath src -cp "lib\sqlite-jdbc.jar" -d bin src\Main.java
if errorlevel 1 goto fail

echo Starting Memory Hacker...
java -cp "bin;lib\sqlite-jdbc.jar" Main
goto end

:nojdk
rem No JDK compiler found - try the prebuilt jar with plain Java
where java >nul 2>nul
if errorlevel 1 goto nojava
if not exist MemoryHacker.jar goto nojava
echo JDK compiler not found, running prebuilt MemoryHacker.jar instead...
java -jar MemoryHacker.jar
goto end

:nojava
echo.
echo ERROR: Java was not found.
echo Install JDK 8 or newer and make sure "java" and "javac" work in Command Prompt.
echo Test with:  java -version   and   javac -version
goto end

:fail
echo.
echo ERROR: Compilation failed. See the messages above.

:end
echo.
pause

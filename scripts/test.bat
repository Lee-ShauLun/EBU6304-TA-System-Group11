@echo off
setlocal enabledelayedexpansion

set "ROOT_DIR=%~dp0.."
for %%I in ("%ROOT_DIR%") do set "ROOT_DIR=%%~fI"
set "OUT_DIR=%ROOT_DIR%\out-test"

where javac >nul 2>nul
if errorlevel 1 (
    echo Java compiler not found. Install JDK 17+ and make sure javac is available in PATH.
    exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
    echo Java runtime not found. Install JDK 17+ and make sure java is available in PATH.
    exit /b 1
)

if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
mkdir "%OUT_DIR%"

dir /s /b "%ROOT_DIR%\src\main\java\*.java" > "%OUT_DIR%\sources.list"
dir /s /b "%ROOT_DIR%\src\test\java\*.java" >> "%OUT_DIR%\sources.list"

javac -encoding UTF-8 -d "%OUT_DIR%" @"%OUT_DIR%\sources.list"
if errorlevel 1 exit /b 1

java -cp "%OUT_DIR%" cn.edu.bupt.tarecruitment.TestRunner

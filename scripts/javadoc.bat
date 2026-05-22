@echo off
setlocal enabledelayedexpansion

set "ROOT_DIR=%~dp0.."
for %%I in ("%ROOT_DIR%") do set "ROOT_DIR=%%~fI"
set "DOC_DIR=%ROOT_DIR%\docs\javadocs"
set "TMP_DIR=%ROOT_DIR%\out-javadoc"

where javadoc >nul 2>nul
if errorlevel 1 (
    echo Javadoc tool not found. Install JDK 17+ and make sure javadoc is available in PATH.
    exit /b 1
)

if exist "%TMP_DIR%" rmdir /s /q "%TMP_DIR%"
mkdir "%TMP_DIR%"
if exist "%DOC_DIR%" rmdir /s /q "%DOC_DIR%"
mkdir "%DOC_DIR%"

dir /s /b "%ROOT_DIR%\src\main\java\*.java" > "%TMP_DIR%\sources.list"

javadoc -quiet -Xdoclint:none -encoding UTF-8 -charset UTF-8 -d "%DOC_DIR%" @"%TMP_DIR%\sources.list"
if errorlevel 1 exit /b 1

echo JavaDocs generated at %DOC_DIR%

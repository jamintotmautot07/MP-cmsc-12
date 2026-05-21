@echo off
REM Build script (Windows batch)
REM Run from tools/

cd /d "%~dp0.."

echo Current directory: %cd%
echo.

setlocal EnableDelayedExpansion

if not exist tools mkdir tools
if exist tools\sources.txt del /f /q tools\sources.txt

set "pwd=%cd%"
if "%pwd:~-1%"=="\" set "pwd=%pwd:~0,-1%"

echo Generating tools\sources.txt...

(for /f "delims=" %%F in ('dir /b /s src\*.java') do @call :addfile "%%~fF") > tools\sources.txt

goto :compile

:addfile
set "f=%~1"
set "rel=!f:%pwd%\=!"
set "rel=!rel:\=/!"
echo !rel!
goto :eof

:compile

if exist bin rd /s /q bin
mkdir bin

echo Compiling...
javac -d bin @tools\sources.txt

if errorlevel 1 (
 echo Compilation failed.
 exit /b 1
)

if not exist ..\Installer\Jar mkdir ..\Installer\Jar

echo Creating jar...

jar cfm ..\Installer\Jar\HawakKoAngBit.jar ^
tools\manifest.txt ^
-C bin . ^
-C . res

if errorlevel 1 (
 echo JAR failed.
 exit /b 1
)

echo.
echo Checking jar contents...
jar tf ..\Installer\Jar\HawakKoAngBit.jar

echo Done.
endlocal
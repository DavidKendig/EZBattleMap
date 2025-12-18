@echo off
echo Compiling Java source files...
if not exist "bin" mkdir bin

javac -d bin src\com\ezbattlemap\dualscreen\*.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful!
    echo.
    echo To run the application, use: run.bat
) else (
    echo Compilation failed!
)

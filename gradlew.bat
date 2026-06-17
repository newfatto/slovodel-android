@echo off
setlocal

set APP_HOME=%~dp0
set WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

if exist "%WRAPPER_JAR%" (
  java -jar "%WRAPPER_JAR%" %*
  exit /b %ERRORLEVEL%
)

where gradle >nul 2>nul
if %ERRORLEVEL% == 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)

echo Gradle is not installed and gradle-wrapper.jar is not available.
echo Use GitHub Actions or install Gradle 9.4.1, then run gradle wrapper --gradle-version 9.4.1.
exit /b 1

@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------

@IF "%DEBUG%" == "" @ECHO OFF
@SETLOCAL

set ERROR_CODE=0

@REM Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto OkJHome

for %%i in (java.exe) do set "JAVACMD=%%~$PATH:i"
goto checkJCmd

:OkJHome
set "JAVACMD=%JAVA_HOME%\bin\java.exe"

if not exist "%JAVACMD%" (
    if exist "%USERPROFILE%\.antigravity-ide\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin\java.exe" (
        set "JAVA_HOME=%USERPROFILE%\.antigravity-ide\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64"
        set "JAVACMD=%USERPROFILE%\.antigravity-ide\extensions\redhat.java-1.58.0-win32-x64\jre\25.0.4.1-win32-x86_64\bin\java.exe"
    )
)

:checkJCmd
if exist "%JAVACMD%" goto chkMHome

echo The JAVA_HOME environment variable is not defined correctly >&2
echo This environment variable is needed to run this program >&2
goto error

:chkMHome
set "BASE_DIR=%~dp0"
if "%BASE_DIR:~-1%"=="\" set "BASE_DIR=%BASE_DIR:~0,-1%"
if "%BASE_DIR%" == "" set "BASE_DIR=."

set "MAVEN_PROJECTBASEDIR=%BASE_DIR%"
set "WRAPPER_JAR=%BASE_DIR%\.mvn\wrapper\maven-wrapper.jar"

if not exist "%WRAPPER_JAR%" (
    powershell -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar' -OutFile '%WRAPPER_JAR%'"
)

set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

"%JAVACMD%" -classpath "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" %WRAPPER_LAUNCHER% %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
exit /B %ERROR_CODE%

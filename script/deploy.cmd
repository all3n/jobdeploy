@echo off
setlocal

set "SCRIPT=%~f0"

if exist %SCRIPT% (
    set "SCRIPT=%~dpnx0"
) else (
    set "SCRIPT=%~f0"
)

set "BIN=%~dp0"
for %%I in ("%BIN%\..\") do set "APPDIR=%%~fI"

set "LOCAL_CONF=%APPDIR%\bin\deploy.local.bat"
if exist "%LOCAL_CONF%" (
    call "%LOCAL_CONF%"
)

set "JAVA=java"
set "WGET_OPTIONS="

set "DEPLOY_OPTIONS="

set "LOGDIR=%APPDIR%\logs"
for %%I in ("%APPDIR%\jobdeploy-*.jar") do set "JAR_FILE=%%~fI"

if not exist "%LOGDIR%" (
    mkdir "%LOGDIR%"
    attrib +R "%LOGDIR%"
)

set "LOGFILE=%LOGDIR%\%USERNAME%-deploy.log"

set "LOADER_PATH=%APPDIR%\conf;%APPDIR%\libs"

if "%~1"=="--update" (
    set "APP_TMPDIR=%TEMP%\jobdeploy-%RANDOM%"
    mkdir "%APP_TMPDIR%"
    set "ARCHIVE_TGZ_URL="
    for /F "tokens=4 delims=^" %%A in ('curl --silent "https://api.github.com/repos/all3n/jobdeploy/releases/latest" ^| findstr /C:"browser_download_url"') do set "ARCHIVE_TGZ_URL=%%~A"
    if defined ARCHIVE_TGZ_URL (
        curl -s -o "%APP_TMPDIR%\jobdeploy.tar.gz" "%ARCHIVE_TGZ_URL%"
        del /Q "%APPDIR%\libs"
        del /Q "%APPDIR%\*.jar"
        tar -zxf "%APP_TMPDIR%\jobdeploy.tar.gz" -C "%APPDIR%"
        echo update job success
    )
) else (
    set "APP_EXT=%APPDIR%\exts"
    set "EXT_PATHS=%APP_EXT%"
    if defined DEPLOY_PLUGIN (
        set "EXT_PATHS=%EXT_PATHS%,%DEPLOY_PLUGIN%"
    )
    set "DEPLOY_OPTIONS=-Dloader.path=%EXT_PATHS%"
    "%JAVA%" %DEPLOY_OPTIONS% -Dlog.file="%LOGFILE%" -jar "%JAR_FILE%" %*
)

endlocal

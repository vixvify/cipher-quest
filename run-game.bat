@echo off
echo Starting CipherQuest...
pushd "%~dp0"
if not exist terminal\node_modules\just-bash\package.json call npm ci --prefix terminal --ignore-scripts --omit=optional
if errorlevel 1 exit /b %errorlevel%
call mvn javafx:run
set "cipherquest_status=%errorlevel%"
popd
exit /b %cipherquest_status%

@echo off
setlocal
echo Starting CipherQuest...
pushd "%~dp0.." || exit /b 1
call :launch
set "cipherquest_status=%errorlevel%"
popd
exit /b %cipherquest_status%

:launch
if exist terminal\node_modules\just-bash\package.json goto run_game
call npm ci --prefix terminal --ignore-scripts --omit=optional
if errorlevel 1 exit /b %errorlevel%

:run_game
call mvn javafx:run
exit /b %errorlevel%

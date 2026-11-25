Write-Host "Starting CipherQuest..." -ForegroundColor Cyan
Push-Location -LiteralPath (Join-Path $PSScriptRoot '..') -ErrorAction Stop
try {
    if (!(Test-Path -LiteralPath 'terminal/node_modules/just-bash/package.json')) {
        npm.cmd ci --prefix terminal --ignore-scripts --omit=optional
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    }
    mvn javafx:run
    exit $LASTEXITCODE
} finally {
    Pop-Location
}

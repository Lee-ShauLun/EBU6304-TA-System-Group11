@echo off
setlocal

set "ROOT_DIR=%~dp0.."
for %%I in ("%ROOT_DIR%") do set "ROOT_DIR=%%~fI"

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$ErrorActionPreference='Stop';" ^
  "$root='%ROOT_DIR%';" ^
  "$zip=Join-Path $root 'Software_group11.zip';" ^
  "$stage=Join-Path $env:TEMP ('Software_group11_' + [guid]::NewGuid().ToString('N'));" ^
  "$dest=Join-Path $stage 'Software_group11';" ^
  "Remove-Item -LiteralPath $zip -Force -ErrorAction SilentlyContinue;" ^
  "New-Item -ItemType Directory -Path $dest | Out-Null;" ^
  "robocopy $root $dest /E /XD .git .tools .codex-home-sync out out-test out-javadoc target data scripts\data /XF .DS_Store *.log Software_group11.zip | Out-Null;" ^
  "if ($LASTEXITCODE -gt 7) { throw 'robocopy failed with exit code ' + $LASTEXITCODE }" ^
  "Compress-Archive -Path $dest -DestinationPath $zip -Force;" ^
  "Remove-Item -Recurse -Force -LiteralPath $stage;" ^
  "Write-Output ('Created ' + $zip);"

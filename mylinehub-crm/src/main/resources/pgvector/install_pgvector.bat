@echo off
echo [pgvector-install] Starting Windows pgvector OS-level installation...

REM Example check if DLL exists (adjust path as needed)
where pgvector.dll >nul 2>&1
if %ERRORLEVEL%==0 (
    echo [pgvector-install] ✅ pgvector DLL already installed.
    exit /b 0
)

echo [pgvector-install] Please manually install pgvector on Windows.
echo [pgvector-install] You might download binaries or build from source.
exit /b 1

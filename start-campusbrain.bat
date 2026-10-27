@echo off
title CampusBrain - Pragati University Platform
cd /d "%~dp0"
cls
echo =====================================================================
echo       CAMPUS BRAIN - PRAGATI UNIVERSITY ACADEMIC PLATFORM
echo =====================================================================
echo.
echo [1/2] Starting CampusBrain backend server on port 8080...
start /b "CampusBrain-Backend" java -jar "%~dp0target\campusbrain-1.0.0.jar"

echo [2/2] Waiting for server to initialize...
timeout /t 10 /nobreak >nul

echo.
echo Server is running!
echo   - Local Portal: http://localhost:8080
echo   - Upload Page:  http://localhost:8080/upload
echo.
echo Opening public tunnel (accessible from any phone or computer)...
echo.
ssh -R 80:localhost:8080 -o StrictHostKeyChecking=no -o ServerAliveInterval=30 nokey@localhost.run
pause

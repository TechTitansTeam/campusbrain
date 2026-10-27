@echo off
title Stop CampusBrain
echo Stopping CampusBrain server...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do taskkill /f /pid %%a 2>nul
echo CampusBrain server stopped.
pause

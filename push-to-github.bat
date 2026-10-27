@echo off
title Push CampusBrain to GitHub
cd /d "%~dp0"
cls
echo =====================================================================
echo              PUSH CAMPUS BRAIN TO GITHUB (FOR 24/7 HOSTING)
echo =====================================================================
echo.
echo Step 1: Open https://github.com/new in your browser.
echo Step 2: Name the repository "campusbrain" (leave README unchecked).
echo Step 3: Click "Create repository" and copy the URL.
echo.
set /p REPO_URL="Paste your GitHub Repository URL here: "

if "%REPO_URL%"=="" (
    echo.
    echo Error: No URL provided.
    pause
    exit /b
)

echo.
echo [1/3] Setting branch to main...
git branch -M main

echo [2/3] Linking to repository...
git remote remove origin 2>nul
git remote add origin %REPO_URL%

echo [3/3] Pushing project to GitHub...
echo (A GitHub login window may pop up in your browser to authorize)
echo.
git push -u origin main

if %ERRORLEVEL% equ 0 (
    echo.
    echo =====================================================================
    echo   SUCCESS! Your code is now on GitHub!
    echo.
    echo   Final Step for 24/7 Live Hosting:
    echo   1. Go to https://render.com
    echo   2. Click "New +" -> "Web Service"
    echo   3. Select your "campusbrain" repository
    echo   4. Click "Deploy" (Render auto-detects the Dockerfile)
    echo =====================================================================
) else (
    echo.
    echo Something went wrong during push. Please check your URL and internet connection.
)

pause

@echo off
title Credit Risk Predictor - Starting...
color 0A

echo.
echo  ============================================
echo    Credit Card Risk Predictor - Launcher
echo  ============================================
echo.

:: ---- Step 1: Kill old processes on ports ----
echo [1/4] Cleaning up old processes...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8080 " 2^>nul') do (
    taskkill /PID %%a /F >nul 2>&1
)
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":5000 " 2^>nul') do (
    taskkill /PID %%a /F >nul 2>&1
)
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":3000 " 2^>nul') do (
    taskkill /PID %%a /F >nul 2>&1
)
echo     Done. All old processes cleared.
echo.

:: ---- Step 2: Start Flask ML Service ----
echo [2/4] Starting ML Service (Flask) on port 5000...
start "Flask ML Service" cmd /k "cd /d "%~dp0ml" && python app.py"
echo     Flask starting in new window...
echo.

:: ---- Step 3: Start Spring Boot Backend ----
echo [3/4] Starting Backend (Spring Boot) on port 8080...
start "Spring Boot Backend" cmd /k "cd /d "%~dp0" && mvn spring-boot:run"
echo     Spring Boot starting in new window...
echo.

:: ---- Step 4: Wait using ping then start Frontend ----
echo [4/4] Waiting 25 seconds for backend to boot...
ping -n 26 127.0.0.1 >nul

echo     Starting Frontend Server on port 3000...
start "Frontend Server" cmd /k "cd /d "%~dp0frontend" && python -m http.server 3000"
echo.

:: ---- Open Browser ----
echo  Opening browser...
ping -n 4 127.0.0.1 >nul
start "" "http://localhost:3000/index.html"

echo.
echo  ============================================
echo    All services launched!
echo.
echo    Frontend  : http://localhost:3000
echo    Backend   : http://localhost:8080
echo    ML Model  : http://localhost:5000
echo.
echo    Wait for Spring Boot window to show:
echo    "Started Application" before predicting.
echo  ============================================
echo.
pause

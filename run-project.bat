@echo off
cd /d "%~dp0"

where docker >nul 2>nul
if errorlevel 1 (
    echo Docker is not installed or not in PATH.
    echo Please install Docker Desktop and try again.
    pause
    exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
    echo Java is not installed or not in PATH.
    echo Please install JDK 17+ and try again.
    pause
    exit /b 1
)

where mvn >nul 2>nul
if errorlevel 1 (
    echo Maven is not installed or not in PATH.
    echo Please install Maven and try again.
    pause
    exit /b 1
)

where npm >nul 2>nul
if errorlevel 1 (
    echo Node.js/npm is not installed or not in PATH.
    echo Please install Node.js and try again.
    pause
    exit /b 1
)

echo Starting PostgreSQL container...
docker compose up -d

if not exist "%~dp0frontend\node_modules" (
    echo Installing frontend dependencies...
    cd /d "%~dp0frontend"
    call npm install
) 

if not exist "%~dp0backend\target" (
    echo Building backend dependencies...
)

start "Event Venue Backend" cmd /k "cd /d \"%~dp0backend\" && mvn spring-boot:run"

start "Event Venue Frontend" cmd /k "cd /d \"%~dp0frontend\" && npm run dev -- --host 0.0.0.0"

echo.
echo Project is starting...
echo Backend: http://localhost:8081
echo Frontend: http://localhost:5173
echo Admin login: username=admin password=admin
echo.
echo Press any key to close this window.
pause >nul

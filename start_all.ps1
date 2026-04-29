# start_all.ps1 - Windows Startup Script for Credit Risk Predictor

$PROJECT_ROOT = Get-Location

echo "🚀 Starting Credit Risk Predictor Ecosystem on Windows..."

# Function to kill process on port
function Kill-Port($port) {
    $process = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
    if ($process) {
        echo "Cleaning up process on port $port (PID: $process)..."
        Stop-Process -Id $process -Force -ErrorAction SilentlyContinue
    }
}

# Clean up old processes
Kill-Port 5000
Kill-Port 8080
Kill-Port 3000
Start-Sleep -Seconds 1

# 1. Start Flask ML Service
echo "Starting ML Service (Flask) on port 5000..."
$ML_DIR = Join-Path $PROJECT_ROOT "ml"
Start-Process -FilePath "python" -ArgumentList "app.py" -WorkingDirectory $ML_DIR -WindowStyle Hidden -RedirectStandardOutput (Join-Path $ML_DIR "flask.log") -RedirectStandardError (Join-Path $ML_DIR "flask_error.log")

# 2. Start Spring Boot Backend
echo "Starting Backend (Spring Boot) on port 8080..."
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WorkingDirectory $PROJECT_ROOT -WindowStyle Hidden -RedirectStandardOutput (Join-Path $PROJECT_ROOT "spring.log") -RedirectStandardError (Join-Path $PROJECT_ROOT "spring_error.log")

# 3. Start Frontend via HTTP Server
echo "Starting Frontend Server on port 3000..."
$FRONTEND_DIR = Join-Path $PROJECT_ROOT "frontend"
Start-Process -FilePath "python" -ArgumentList "-m", "http.server", "3000" -WorkingDirectory $FRONTEND_DIR -WindowStyle Hidden

# 4. Wait for services to be ready
echo "Waiting for services to initialize (15 seconds)..."
Start-Sleep -Seconds 15

# 5. Open Frontend in Browser
echo "Opening Frontend at http://localhost:3000 ..."
Start-Process "http://localhost:3000/index.html"

echo ""
echo "✅ All services are running!"
echo "   Frontend:   http://localhost:3000/index.html"
echo "   Backend:    http://localhost:8080"
echo "   ML Service: http://localhost:5000"
echo ""
echo "Logs: ml/flask.log | spring.log"

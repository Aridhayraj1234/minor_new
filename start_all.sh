#!/bin/bash

# Get the absolute path of the project root
PROJECT_ROOT="/home/ayush-sher/Documents/credit-card-predictor(1)"

echo "🚀 Starting Credit Risk Predictor Ecosystem..."

# Kill any existing processes on ports 5000, 8080, 3000
echo "Cleaning up old processes..."
fuser -k 5000/tcp 2>/dev/null || true
fuser -k 8080/tcp 2>/dev/null || true
fuser -k 3000/tcp 2>/dev/null || true
sleep 1

# 1. Start Flask ML Service
echo "Starting ML Service (Flask) on port 5000..."
cd "$PROJECT_ROOT/ml"
if [ -d "venv" ]; then
    source venv/bin/activate
else
    echo "Warning: Python venv not found. Attempting to run with system python..."
fi
nohup python3 app.py > flask.log 2>&1 &
ML_PID=$!
echo "ML Service started (PID: $ML_PID)"

# 2. Start Spring Boot Backend
echo "Starting Backend (Spring Boot) on port 8080..."
cd "$PROJECT_ROOT"
nohup mvn spring-boot:run > spring.log 2>&1 &
BACKEND_PID=$!
echo "Backend started (PID: $BACKEND_PID)"

# 3. Start Frontend via HTTP Server (avoids file:// CORS issues)
echo "Starting Frontend Server on port 3000..."
cd "$PROJECT_ROOT/frontend"
nohup python3 -m http.server 3000 > /dev/null 2>&1 &
FRONTEND_PID=$!
echo "Frontend server started (PID: $FRONTEND_PID)"

# 4. Wait for services to be ready
echo "Waiting for services to initialize (15 seconds)..."
sleep 15

# 5. Open Frontend in Browser
echo "Opening Frontend at http://localhost:3000 ..."
if command -v xdg-open > /dev/null; then
    xdg-open "http://localhost:3000/index.html"
elif command -v open > /dev/null; then
    open "http://localhost:3000/index.html"
else
    echo "Please open this URL in your browser: http://localhost:3000/index.html"
fi

echo ""
echo "✅ All services are running!"
echo "   Frontend:   http://localhost:3000/index.html"
echo "   Backend:    http://localhost:8080"
echo "   ML Service: http://localhost:5000"
echo ""
echo "Use 'kill $ML_PID $BACKEND_PID $FRONTEND_PID' to stop all services."
echo "Logs: ml/flask.log | spring.log"

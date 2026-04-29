# Complete Setup Guide - Credit Card Predictor Web Application

## STEP 1: Install Required Software (One-time setup)

### For Linux/Ubuntu:
```bash
# Update package manager
sudo apt update && sudo apt upgrade -y

# Install Java 11+ (needed for Spring Boot)
sudo apt install -y openjdk-11-jdk

# Install Maven (build tool for Java)
sudo apt install -y maven

# Install Python 3.8+ (for Flask and ML)
sudo apt install -y python3 python3-pip python3-venv

# Verify installations
java -version
mvn -version
python3 --version
```

### For macOS:
```bash
# Install Homebrew (if not installed)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java
brew install openjdk@11

# Install Maven
brew install maven

# Install Python
brew install python3

# Verify installations
java -version
mvn -version
python3 --version
```

### For Windows:
```
- Download & install Java 11: https://www.oracle.com/java/technologies/downloads/
- Download & install Maven: https://maven.apache.org/download.cgi
- Download & install Python 3.9+: https://www.python.org/downloads/
- Add to PATH and restart terminal
```

---

## STEP 2: Navigate to Project & Setup Python Virtual Environment

```bash
# Navigate to project directory
cd /home/ayush-sher/Downloads/credit-card-predictor(1)/project

# Go to ML folder
cd ml

# Create Python virtual environment
python3 -m venv venv

# Activate virtual environment
# On Linux/macOS:
source venv/bin/activate

# On Windows:
# venv\Scripts\activate

# Install Python packages
pip install -r requirements.txt

# Download/prepare dataset
# Ensure data.csv is in: /home/ayush-sher/Desktop/workspace/ml_projects/minor_project/archive/data.csv
# (or update the path in train.py)

# Train ML model
python train.py
```

**Expected Output:**
```
logistic_regression accuracy: 0.7017
decision_tree accuracy: 0.5879
random_forest accuracy: 0.6750
Best model: logistic_regression with accuracy 0.7017
Saved model.pkl
```

---

## STEP 3: Start Flask API Server

```bash
# Make sure you're in the ml folder and virtual environment is activated
cd /home/ayush-sher/Downloads/credit-card-predictor(1)/project/ml
source venv/bin/activate  # (or venv\Scripts\activate on Windows)

# Run Flask server
python app.py
```

**Expected Output:**
```
 * Running on http://0.0.0.0:5000
 * WARNING: This is a development server
```

**✅ Flask Server Ready on: http://localhost:5000**

---

## STEP 4: Start Spring Boot Backend (New Terminal)

```bash
# Open a NEW terminal window and navigate to backend
cd /home/ayush-sher/Downloads/credit-card-predictor(1)/project/backend

# Build the project
mvn clean package -DskipTests

# Run Spring Boot
mvn spring-boot:run
```

**Expected Output:**
```
2026-03-22 16:42:00.000  INFO 12345 --- [  restartedMain] com.example.credit.Application
Started Application in 5.230 seconds
Tomcat started on port(s): 8080 (http)
```

**✅ Backend Ready on: http://localhost:8080**

---

## STEP 5: Open Frontend (New Terminal or Browser)

```bash
# Open frontend in browser
# On Linux:
xdg-open "file:///home/ayush-sher/Downloads/credit-card-predictor(1)/project/frontend/index.html"

# On macOS:
open "file:///home/ayush-sher/Downloads/credit-card-predictor(1)/project/frontend/index.html"

# On Windows:
start "file:///home/ayush-sher/Downloads/credit-card-predictor(1)/project/frontend/index.html"

# OR manually open browser and paste this URL:
# file:///home/ayush-sher/Downloads/credit-card-predictor(1)/project/frontend/index.html
```

**✅ Frontend Ready in Browser**

---

## QUICK START SCRIPT (All at Once)

### For Linux/macOS - Create a file `run.sh`:

```bash
#!/bin/bash

# Terminal 1: Flask
echo "Starting Flask..."
cd ~/Downloads/credit-card-predictor(1)/project/ml
source venv/bin/activate
python app.py &
FLASK_PID=$!

# Wait for Flask to start
sleep 3

# Terminal 2: Spring Boot
echo "Starting Spring Boot..."
cd ~/Downloads/credit-card-predictor(1)/project/backend
mvn spring-boot:run &
SPRING_PID=$!

# Wait for Spring Boot to start
sleep 10

# Terminal 3: Open Frontend
echo "Opening Frontend..."
xdg-open "file://$(pwd)/../frontend/index.html"

# Handle cleanup on exit
trap "kill $FLASK_PID $SPRING_PID" EXIT
wait
```

**Run it:**
```bash
chmod +x run.sh
./run.sh
```

---

## TESTING THE APPLICATION

### Test Flask API:
```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{"age":25,"income":50000,"credit_score":750,"loan_amount":5000}'

# Expected response:
# {"prediction":1,"probability":0.7119...}
```

### Test Spring Boot API:
```bash
curl -X POST http://localhost:8080/api/predict \
  -H "Content-Type: application/json" \
  -d '{"age":25,"income":50000,"credit_score":750,"loan_amount":5000}'

# Expected response:
# {"prediction":1,"probability":0.7119...}
```

### Get Prediction History:
```bash
curl http://localhost:8080/api/history

# Expected response:
# [{"id":1,"age":25,"income":50000,"creditScore":750,"loanAmount":5000,"prediction":1,"probability":0.7119...}]
```

---

## QUICK REFERENCE - All Running Ports

| Service | Port | URL |
|---------|------|-----|
| Flask ML API | 5000 | http://localhost:5000 |
| Spring Boot Backend | 8080 | http://localhost:8080 |
| Frontend | (Browser) | file:///...../frontend/index.html |
| H2 Database | (Embedded) | In-memory (auto-managed) |

---

## COMMON ISSUES & FIXES

### Issue: "Port already in use"
```bash
# Kill existing process on port 5000 (Flask)
lsof -ti:5000 | xargs kill -9

# Kill existing process on port 8080 (Spring Boot)
lsof -ti:8080 | xargs kill -9
```

### Issue: "Dataset not found"
- Ensure `data.csv` exists at: `/home/ayush-sher/Desktop/workspace/ml_projects/minor_project/archive/data.csv`
- Or update path in `ml/train.py` line 12

### Issue: "Maven not found"
```bash
# Add Maven to PATH
export PATH=$PATH:/path/to/maven/bin
```

### Issue: "Python packages missing"
```bash
cd ~/Downloads/credit-card-predictor(1)/project/ml
source venv/bin/activate
pip install -r requirements.txt --upgrade
```

---

## FILE STRUCTURE

```
├── frontend/
│   ├── index.html        (Web form & UI)
│   ├── style.css         (Styling)
│   └── script.js         (Form logic)
├── src/                  (Spring Boot source)
│   └── main/java/com/example/credit/
├── ml/
│   ├── venv/             (Python virtual env)
│   ├── train.py          (Model training)
│   ├── app.py            (Flask API)
│   ├── requirements.txt   (Python packages)
│   └── model.pkl         (Trained model)
├── database/
│   └── database.sql      (Schema reference)
├── pom.xml               (Maven config - now at root)
└── start_all.sh          (Automation script)
```

---

## INPUT VALIDATION (Strict Requirements)

When using the web form, remember:
- **Age**: Must be between 18-100
- **Income**: Must be at least $10,000
- **Credit Score**: Must be between 300-850
- **Loan Amount**: Must be at least $1,000

---

## FINAL CHECKLIST

✅ Java installed
✅ Maven installed
✅ Python 3.8+ installed
✅ Virtual environment created
✅ Python packages installed
✅ ML model trained (model.pkl created)
✅ Flask running on port 5000
✅ Spring Boot running on port 8080
✅ Frontend opened in browser
✅ Form submission working
✅ Predictions displayed correctly
✅ History table populated

**You're all set! 🚀**

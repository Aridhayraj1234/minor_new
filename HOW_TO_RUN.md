# 🚀 Credit Risk Predictor: How to Run

Follow these steps to download tools and run the application on any device.

## 📥 1. Download & Install Tools

### Windows
1. Install **Java 17**: [Download Here](https://adoptium.net/temurin/releases/?version=17)
2. Install **Maven**: [Download Here](https://maven.apache.org/download.cgi)
3. Install **Python 3.12**: [Download Here](https://www.python.org/downloads/)
4. Add all to your "System Environment Variables" PATH.

### macOS
```bash
brew install java17 maven python@3.12
```

### Linux (Ubuntu/Debian)
```bash
sudo apt update && sudo apt install -y openjdk-17-jdk maven python3 python3-pip python3-venv
```

---

## 🏗️ 2. One-Time Project Setup
```bash
# Setup ML
cd ml
python3 -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
python3 train.py

# Setup Backend
cd ..
mvn clean install -DskipTests
```

---

## 🏃 3. Run the Application

### Option A: Automation Script (Linux/macOS)
```bash
./start_all.sh
```

### Option B: Manual Startup (All Devices)
Open 3 separate terminals:

1. **Terminal 1 (ML)**:
   ```bash
   cd ml && source venv/bin/activate && python3 app.py
   ```
2. **Terminal 2 (Backend)**:
   ```bash
   mvn spring-boot:run
   ```
3. **Terminal 3 (Frontend)**:
   ```bash
   cd frontend && python3 -m http.server 3000
   ```

---

## 🌐 4. Access the App
- **URL**: [http://localhost:3000](http://localhost:3000)
- **Login**: `demo@demo.com` / `demo1234`

---

## 🛠️ Troubleshooting
- **Ports already in use?** Restart your computer or kill processes on 8080, 5000, and 3000.
- **CORS Error?** Make sure you are using `http://localhost:3000` and not opening `index.html` as a file.

const form = document.getElementById("predict-form");
const resultDiv = document.getElementById("result");
const loadHistoryBtn = document.getElementById("load-history");
const historyBody = document.querySelector("#history-table tbody");
const apiBase = "http://localhost:8080";

// Check Auth
const token = localStorage.getItem("token");
const role = localStorage.getItem("role");
if (!token || (role !== "USER" && role !== "CUSTOMER" && role !== "ADMIN")) {
    window.location.href = "login.html";
}

document.getElementById("user-name").innerText = localStorage.getItem("name") || "Customer";

document.getElementById("logout-btn").addEventListener("click", () => {
    localStorage.clear();
    window.location.href = "login.html";
});

// Health Check
async function checkHealth() {
    const statusDiv = document.createElement("div");
    statusDiv.id = "connection-status";
    statusDiv.style.cssText = "font-size: 0.8em; margin-bottom: 10px; color: #666; text-align: center;";
    document.querySelector(".header").appendChild(statusDiv);

    try {
        const res = await fetch(`${apiBase}/api/health`);
        if (!res.ok) throw new Error("Backend unstable");
        const data = await res.json();
        const mlStatus = typeof data.ml_service === 'object' ? data.ml_service.status : 'DOWN';
        statusDiv.innerHTML = `Backend: <span style="color:green">●</span> | ML Service: <span style="color:${mlStatus === 'UP' ? 'green' : 'red'}">●</span> | LLM: <span style="color:orange">${data.llm_model}</span>`;
    } catch (err) {
        statusDiv.innerHTML = `System Status: <span style="color:red">● OFFLINE</span> (Backend not reachable)`;
    }
}
checkHealth();

form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const age = Number(document.getElementById("age").value);
    const gender = document.getElementById("gender").value;
    const education = document.getElementById("education").value;
    const income = Number(document.getElementById("income").value);
    const debt = Number(document.getElementById("debt").value);
    const credit_score = Number(document.getElementById("credit_score").value);
    const loan_amount = Number(document.getElementById("loan_amount").value);
    const loan_term = Number(document.getElementById("loan_term").value);
    const num_credit_cards = Number(document.getElementById("num_credit_cards").value);
    const employment_status = document.getElementById("employment_status").value;
    const payment_history = document.getElementById("payment_history").value;
    const residence_type = document.getElementById("residence_type").value;
    const marital_status = document.getElementById("marital_status").value;
    
    const data = {
        age, gender, education, income, debt, 
        creditScore: credit_score, 
        loanAmount: loan_amount, 
        loanTerm: loan_term,
        numCreditCards: num_credit_cards,
        employmentStatus: employment_status, 
        paymentHistory: payment_history,
        residenceType: residence_type,
        maritalStatus: marital_status
    };
    
    try {
        const response = await fetch(`${apiBase}/api/predict`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`
            },
            body: JSON.stringify(data),
        });
        
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                localStorage.clear();
                window.location.href = "login.html";
                return;
            }
            throw new Error(await response.text());
        }
        
        const json = await response.json();
        
        let riskColor = json.riskLevel === 'LOW' ? '#2ecc71' : (json.riskLevel === 'MEDIUM' ? '#f1c40f' : '#e74c3c');
        
        resultDiv.classList.remove("hidden");
        resultDiv.style.background = 'rgba(255, 255, 255, 0.95)';
        resultDiv.style.color = '#333';
        resultDiv.style.borderTop = `6px solid ${riskColor}`;
        
        resultDiv.innerHTML = `
            <div style="text-align: center;">
                <h3 style="font-size: 1.8em; margin-bottom: 5px; color: ${riskColor}">Risk: ${json.riskLevel}</h3>
                <strong style="font-size: 1.2em; color: #666;">Confidence Score: ${(json.probabilityValue * 100).toFixed(2)}%</strong>
            </div>
            <div class="llm-explanation">
                <strong>AI Analysis:</strong><br/>
                ${json.explanation}
            </div>
        `;
        loadHistory();
    } catch (err) {
        resultDiv.classList.remove("hidden");
        resultDiv.style.background = "#ffcccc";
        resultDiv.style.color = "#333";
        resultDiv.innerHTML = `<strong>Error:</strong> ${err.message}`;
    }
});

loadHistoryBtn.addEventListener("click", loadHistory);

async function loadHistory() {
    try {
        const res = await fetch(`${apiBase}/user/history`, {
            headers: {
                "Authorization": `Bearer ${token}`
            }
        });
        
        if (!res.ok) {
            if (res.status === 401 || res.status === 403) {
                localStorage.clear();
                window.location.href = "login.html";
                return;
            }
            throw new Error("Failed to load history");
        }
        
        const data = await res.json();
        historyBody.innerHTML = "";
        
        data.forEach(row => {
            const tr = document.createElement("tr");
            const timestamp = new Date(row.createdAt).toLocaleString();
            let riskColor = row.predictionResult === 'LOW' ? 'green' : (row.predictionResult === 'MEDIUM' ? 'orange' : 'red');
            
            tr.innerHTML = `
                <td>${timestamp}</td>
                <td>$${row.loanAmount.toLocaleString()}</td>
                <td>${row.creditScore}</td>
                <td style="color: ${riskColor}; font-weight: bold;">${row.predictionResult}</td>
                <td>
                    <button onclick="alert('${(row.llmExplanation || "No explanation available").replace(/'/g, "\\'")}')" style="padding: 5px; cursor: pointer; border-radius: 5px;">View AI Logic</button>
                </td>
            `;
            historyBody.appendChild(tr);
        });
    } catch (err) {
        console.error("Error loading history:", err);
    }
}

// Load history on mount
loadHistory();

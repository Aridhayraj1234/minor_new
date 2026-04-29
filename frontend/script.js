const form = document.getElementById("predict-form");
const resultDiv = document.getElementById("result");
const loadHistoryBtn = document.getElementById("load-history");
const historyBody = document.querySelector("#history-table tbody");
const apiBase = "http://localhost:8080";

// Redirect to login if no token
const token = localStorage.getItem("token");
if (!token) {
    window.location.href = "login.html";
}

form.addEventListener("submit", async (e) => {
    e.preventDefault();
    const age = Number(document.getElementById("age").value);
    const income = Number(document.getElementById("income").value);
    const credit_score = Number(document.getElementById("credit_score").value);
    const loan_amount = Number(document.getElementById("loan_amount").value);
    
    // Validation
    if (age < 18 || age > 100) {
        alert("Age must be between 18 and 100");
        return;
    }
    if (income < 10000) {
        alert("Income must be at least $10,000");
        return;
    }
    if (credit_score < 100 || credit_score > 900) {
        alert("Credit Score must be between 100 and 900");
        return;
    }
    if (loan_amount < 1000) {
        alert("Loan amount must be at least $1,000");
        return;
    }
    
    const data = {
        age: age,
        income: income,
        creditScore: credit_score,
        loanAmount: loan_amount,
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
            const errText = await response.text();
            throw new Error(errText || `Server error: ${response.status}`);
        }

        const json = await response.json();
        
        let riskColor = json.riskLevel === 'LOW' ? '#2ecc71' : (json.riskLevel === 'MEDIUM' ? '#f1c40f' : '#e74c3c');
        
        resultDiv.classList.remove("hidden");
        resultDiv.style.borderTop = `6px solid ${riskColor}`;
        resultDiv.innerHTML = `
            <div style="text-align: center;">
                <h3 style="font-size: 1.8em; margin-bottom: 15px; color: ${riskColor}">Risk: ${json.riskLevel}</h3>
                <strong style="font-size: 1.2em;">Confidence Score: ${(json.probabilityValue * 100).toFixed(2)}%</strong>
            </div>
            <div class="llm-explanation" style="margin-top:15px; padding:15px; background:rgba(255,255,255,0.1); border-radius:10px; border-left: 4px solid #ffbe0b; font-style:italic; font-size:0.9em;">
                <strong>AI Analysis:</strong><br/>
                ${json.explanation}
            </div>
        `;
        loadHistory();
    } catch (err) {
        resultDiv.classList.remove("hidden");
        resultDiv.style.background = "linear-gradient(135deg, #f093fb 0%, #f5576c 100%)";
        resultDiv.innerHTML = `<strong>Error:</strong> ${err.message}`;
    }
});

loadHistoryBtn.addEventListener("click", loadHistory);

async function loadHistory() {
    try {
        const res = await fetch(`${apiBase}/user/history`, {
            headers: { "Authorization": `Bearer ${token}` }
        });

        if (!res.ok) {
            console.warn("Could not load history:", res.status);
            return;
        }

        const data = await res.json();
        historyBody.innerHTML = "";
        
        data.forEach(row => {
            const tr = document.createElement("tr");
            const timestamp = new Date(row.createdAt).toLocaleString();
            let riskColor = row.predictionResult === 'LOW' ? 'green' : (row.predictionResult === 'MEDIUM' ? 'orange' : 'red');
            
            tr.innerHTML = `
                <td>${timestamp}</td>
                <td>${row.age}</td>
                <td>$${row.income.toLocaleString()}</td>
                <td>${row.creditScore}</td>
                <td>$${row.loanAmount.toLocaleString()}</td>
                <td style="color: ${riskColor}; font-weight: bold;">${row.predictionResult}</td>
                <td>${(row.riskScore * 100).toFixed(2)}%</td>
                <td>${timestamp}</td>
            `;
            historyBody.appendChild(tr);
        });
    } catch (err) {
        console.error("Error loading history:", err);
    }
}

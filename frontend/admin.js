const usersBody = document.querySelector("#users-table tbody");
const appsBody = document.querySelector("#apps-table tbody");
const apiBase = "http://localhost:8080/admin";
const chatApiBase = "http://localhost:8080/api";

// State
let allApplications = [];

// Check Auth
const token = localStorage.getItem("token");
const role = localStorage.getItem("role");
if (!token || role !== "ADMIN") {
    window.location.href = "login.html";
}

document.getElementById("admin-name").innerText = localStorage.getItem("name") || "Admin";

document.getElementById("logout-btn").addEventListener("click", () => {
    localStorage.clear();
    window.location.href = "login.html";
});

// Sidebar Navigation
document.querySelectorAll(".sidebar-nav a").forEach(link => {
    link.addEventListener("click", (e) => {
        document.querySelectorAll(".sidebar-nav a").forEach(l => l.classList.remove("active"));
        link.classList.add("active");
    });
});

async function makeAdmin(userId) {
    await fetch(`${apiBase}/users/${userId}/role`, {
        method: "PUT",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ role: "ADMIN" })
    });
    loadAdminData();
}

async function makeUser(userId) {
    await fetch(`${apiBase}/users/${userId}/role`, {
        method: "PUT",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ role: "USER" })
    });
    loadAdminData();
}

async function deleteUser(userId) {
    if (!confirm("Delete this user?")) return;
    await fetch(`${apiBase}/users/${userId}`, {
        method: "DELETE",
        headers: { "Authorization": `Bearer ${token}` }
    });
    loadAdminData();
}

function renderApplications(apps) {
    appsBody.innerHTML = "";
    apps.forEach(app => {
        const tr = document.createElement("tr");
        const timestamp = new Date(app.createdAt).toLocaleString();
        let riskColor = app.predictionResult === 'LOW' ? 'green' : (app.predictionResult === 'MEDIUM' ? 'orange' : 'red');
        
        tr.innerHTML = `
            <td>${app.userId || 'N/A'}</td>
            <td>${timestamp}</td>
            <td>$${(app.loanAmount || 0).toLocaleString()}</td>
            <td>${app.creditScore || 'N/A'}</td>
            <td style="color: ${riskColor}; font-weight: bold;">${app.predictionResult || 'UNKNOWN'}</td>
            <td>
                <button class="btn-report" onclick="showReport('${app.id}')">AI Report</button>
            </td>
        `;
        appsBody.appendChild(tr);
    });
}

function showReport(appId) {
    const app = allApplications.find(a => a.id == appId);
    if (!app) return;
    
    document.getElementById("report-text").innerText = app.llmExplanation || "No analysis available.";
    document.getElementById("report-modal").classList.add("active");
}

document.getElementById("close-modal").addEventListener("click", () => {
    document.getElementById("report-modal").classList.remove("active");
});

document.getElementById("print-report").addEventListener("click", () => {
    const reportText = document.getElementById("report-text").innerText;
    const blob = new Blob([reportText], { type: 'text/plain' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Risk_Report_${new Date().getTime()}.txt`;
    a.click();
});

async function loadAdminData() {
    try {
        // Load Users
        const usersRes = await fetch(`${apiBase}/users`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!usersRes.ok) throw new Error("Failed to load users");
        const usersData = await usersRes.json();
        
        usersBody.innerHTML = "";
        usersData.forEach(user => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${user.id}</td>
                <td>${user.name}</td>
                <td>${user.email}</td>
                <td><strong style="color: ${user.role === 'ADMIN' ? '#ff006e' : '#3a86ff'}">${user.role}</strong></td>
                <td>
                    <button onclick="makeAdmin(${user.id})">Admin</button>
                    <button onclick="makeUser(${user.id})">User</button>
                    <button onclick="deleteUser(${user.id})" style="color:red">Del</button>
                </td>
            `;
            usersBody.appendChild(tr);
        });

        // Load Applications
        const appsRes = await fetch(`${apiBase}/applications`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!appsRes.ok) throw new Error("Failed to load applications");
        allApplications = await appsRes.json();
        renderApplications(allApplications);

        // Load Usage Summary
        const usageRes = await fetch(`${apiBase}/usage`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (usageRes.ok) {
            const usage = await usageRes.json();
            document.getElementById("stat-total-users").innerText = usage.totalUsers || 0;
            document.getElementById("stat-total-apps").innerText = usage.totalPredictions || 0;
            document.getElementById("stat-high-risk").innerText = usage.highRiskCount || 0;
        }
    } catch (err) {
        console.error("Error loading admin data:", err);
    }
}

// History Search
document.getElementById("history-search").addEventListener("input", (e) => {
    const term = e.target.value.toLowerCase();
    const filtered = allApplications.filter(app => {
        const userId = (app.userId || '').toString();
        const amount = (app.loanAmount || '').toString();
        return userId.includes(term) || amount.includes(term);
    });
    renderApplications(filtered);
});

// Chatbot Logic
const chatToggle = document.getElementById("chat-toggle");
const chatWindow = document.getElementById("chat-window");
const chatClose = document.getElementById("chat-close");
const chatSend = document.getElementById("chat-send");
const chatInput = document.getElementById("chat-input");
const chatMessages = document.getElementById("chat-messages");

chatToggle.addEventListener("click", () => chatWindow.classList.toggle("active"));
chatClose.addEventListener("click", () => chatWindow.classList.remove("active"));

async function sendMessage() {
    const msg = chatInput.value.trim();
    if (!msg) return;

    // Add user message
    const userDiv = document.createElement("div");
    userDiv.className = "message user";
    userDiv.innerText = msg;
    chatMessages.appendChild(userDiv);
    chatInput.value = "";
    chatMessages.scrollTop = chatMessages.scrollHeight;

    try {
        const res = await fetch(`${chatApiBase}/chat`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ message: msg })
        });
        const data = await res.json();
        
        // Add bot message
        const botDiv = document.createElement("div");
        botDiv.className = "message bot";
        botDiv.innerText = data.response || "Sorry, I'm having trouble connecting.";
        chatMessages.appendChild(botDiv);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    } catch (err) {
        const errorDiv = document.createElement("div");
        errorDiv.className = "message bot";
        errorDiv.innerText = "Connection error.";
        chatMessages.appendChild(errorDiv);
    }
}

chatSend.addEventListener("click", sendMessage);
chatInput.addEventListener("keypress", (e) => {
    if (e.key === "Enter") sendMessage();
});

window.makeAdmin = makeAdmin;
window.makeUser = makeUser;
window.deleteUser = deleteUser;

// Load data on mount
loadAdminData();

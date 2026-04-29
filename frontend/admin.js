const usersBody = document.querySelector("#users-table tbody");
const appsBody = document.querySelector("#apps-table tbody");
const apiBase = "http://localhost:8080/admin";

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
                    <button onclick="makeAdmin(${user.id})">Make Admin</button>
                    <button onclick="makeUser(${user.id})">Make User</button>
                    <button onclick="deleteUser(${user.id})">Delete</button>
                </td>
            `;
            usersBody.appendChild(tr);
        });

        // Load Applications
        const appsRes = await fetch(`${apiBase}/applications`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (!appsRes.ok) throw new Error("Failed to load applications");
        const appsData = await appsRes.json();
        
        appsBody.innerHTML = "";
        appsData.forEach(app => {
            const tr = document.createElement("tr");
            const timestamp = new Date(app.createdAt).toLocaleString();
            let riskColor = app.predictionResult === 'LOW' ? 'green' : (app.predictionResult === 'MEDIUM' ? 'orange' : 'red');
            
            tr.innerHTML = `
                <td>${app.user ? app.user.id : 'N/A'}</td>
                <td>${timestamp}</td>
                <td>$${app.loanAmount.toLocaleString()}</td>
                <td>${app.creditScore}</td>
                <td style="color: ${riskColor}; font-weight: bold;">${app.predictionResult}</td>
                <td>
                    <button onclick="alert('${app.llmExplanation ? app.llmExplanation.replace(/'/g, "\\'") : 'No explanation available'}')" style="padding: 5px; cursor: pointer; border-radius: 5px;">View AI Logic</button>
                </td>
            `;
            appsBody.appendChild(tr);
        });

        const usageRes = await fetch(`${apiBase}/usage`, {
            headers: { "Authorization": `Bearer ${token}` }
        });
        if (usageRes.ok) {
            const usage = await usageRes.json();
            const usageEl = document.getElementById("usage-summary");
            if (usageEl) {
                usageEl.innerText = JSON.stringify(usage, null, 2);
            }
        }
    } catch (err) {
        console.error("Error loading admin data:", err);
        if (err.message.includes("401") || err.message.includes("403")) {
            localStorage.clear();
            window.location.href = "login.html";
        }
    }
}

window.makeAdmin = makeAdmin;
window.makeUser = makeUser;
window.deleteUser = deleteUser;

// Load data on mount
loadAdminData();

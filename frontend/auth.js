const apiBase = "http://localhost:8080/auth";

if (document.getElementById("login-form")) {
    document.getElementById('login-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('login-email').value;
        const password = document.getElementById('login-password').value;
        const btn = e.target.querySelector('button');
        btn.innerText = 'Logging in...';

        try {
            const response = await fetch(`${apiBase}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            if (!response.ok) {
                throw new Error(await response.text());
            }

            const data = await response.json();
            localStorage.setItem('token', data.token);
            localStorage.setItem('role', data.role);
            localStorage.setItem('name', data.name);

            if (data.role === 'ADMIN') {
                window.location.href = 'admin-dashboard.html';
            } else {
                window.location.href = 'user-dashboard.html';
            }
        } catch (err) {
            const errDiv = document.getElementById('login-error');
            errDiv.innerText = err.message;
            errDiv.classList.remove('hidden');
            btn.innerText = 'Login';
        }
    });
}

if (document.getElementById("register-form")) {
    document.getElementById('register-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = document.getElementById('register-name').value;
        const email = document.getElementById('register-email').value;
        const password = document.getElementById('register-password').value;
        const role = document.getElementById('register-role').value;
        const btn = e.target.querySelector('button');
        btn.innerText = 'Registering...';

        try {
            const response = await fetch(`${apiBase}/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, email, password, role })
            });

            const msgDiv = document.getElementById('register-msg');
            const text = await response.text();

            if (!response.ok) {
                throw new Error(text);
            }

            msgDiv.innerText = `${text}. You can login now.`;
            msgDiv.className = 'success-message';
            msgDiv.classList.remove('hidden');
            e.target.reset();
            btn.innerText = 'Register';
        } catch (err) {
            const msgDiv = document.getElementById('register-msg');
            msgDiv.innerText = err.message;
            msgDiv.className = 'error-message';
            msgDiv.classList.remove('hidden');
            btn.innerText = 'Register';
        }
    });
}

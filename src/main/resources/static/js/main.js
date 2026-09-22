//Logout functionality
const logoutBtn = document.getElementById('logoutBtn');

if (logoutBtn) {
    logoutBtn.addEventListener('click', async () => {
        try {
            const response = await fetch('/api/auth/logout', {
                method: 'POST',
                credentials: 'same-origin'
            });
            if (!response.ok) throw new Error('Logout failed. Please try again.');
            window.location.href = '/login?logout';
        } catch (error) {
            alert(error.message);
        }
    });
}

//login functionality
const loginForm = document.getElementById('loginForm');

if (loginForm) {
    loginForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        try {
            await sendAuthRequest('/api/auth/login', loginForm);
            window.location.href = '/dashboard';
        } catch (error) {
            alert(error.message);
        }
    });
}

// register functionality
const registerForm = document.getElementById('registerForm');

if (registerForm) {
    registerForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        try {
            await sendAuthRequest('/api/auth/register', registerForm);
            window.location.href = '/dashboard';
        } catch (error) {
            alert(error.message);
        }
    });
}

//send authentication request
async function sendAuthRequest(url, form) {
    const payload = Object.fromEntries(new FormData(form));
    const response = await fetch(url, {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(payload),
        credentials: 'same-origin'
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.message || 'Authentication failed');
    return data;
}
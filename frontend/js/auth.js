async function login(email, password) {
    try {
        const data = await fetchAPI('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password })
        });
        localStorage.setItem('token', data.token);
        localStorage.setItem('userEmail', data.email);
        localStorage.setItem('userName', data.fullName);
        return true;
    } catch (error) {
        alert(error.message || 'Login failed');
        return false;
    }
}

async function register(fullName, email, password) {
    try {
        await fetchAPI('/auth/register', {
            method: 'POST',
            body: JSON.stringify({ fullName, email, password })
        });
        return true;
    } catch (error) {
        alert(error.message || 'Registration failed');
        return false;
    }
}

function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userName');
    window.location.href = 'index.html';
}

function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token && window.location.pathname.includes('dashboard.html')) {
        window.location.href = 'login.html';
    } else if (token && (window.location.pathname.includes('login.html') || window.location.pathname.includes('register.html') || window.location.pathname.endsWith('/') || window.location.pathname.endsWith('index.html'))) {
        window.location.href = 'dashboard.html';
    }
}

// Run auth check on load
document.addEventListener('DOMContentLoaded', checkAuth);

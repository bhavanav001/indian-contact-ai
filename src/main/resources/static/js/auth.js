// Handles both login.html and register.html — checks which form is on
// the page and wires up the matching handler.

function showAlert(el, message) {
  el.textContent = message;
  el.classList.add('show');
}

function hideAlert(el) {
  el.classList.remove('show');
  el.textContent = '';
}

function saveSession(data) {
  localStorage.setItem('token', data.token);
  localStorage.setItem('user', JSON.stringify({
    id: data.id,
    name: data.name,
    email: data.email
  }));
}

document.addEventListener('DOMContentLoaded', () => {

  // Already logged in? Skip straight to the dashboard.
  if (isLoggedIn()) {
    window.location.href = 'dashboard.html';
    return;
  }

  const loginForm = document.getElementById('login-form');
  const registerForm = document.getElementById('register-form');
  const alertEl = document.getElementById('alert');

  if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      hideAlert(alertEl);
      const submitBtn = document.getElementById('submit-btn');

      const email = document.getElementById('email').value.trim();
      const password = document.getElementById('password').value;

      submitBtn.disabled = true;
      submitBtn.textContent = 'Logging in...';

      try {
        const data = await apiFetch('/auth/login', {
          method: 'POST',
          body: { email, password }
        });
        saveSession(data);
        window.location.href = 'dashboard.html';
      } catch (err) {
        showAlert(alertEl, err.message);
        submitBtn.disabled = false;
        submitBtn.textContent = 'Login';
      }
    });
  }

  if (registerForm) {
    const successEl = document.getElementById('success-alert');

    registerForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      hideAlert(alertEl);
      hideAlert(successEl);
      const submitBtn = document.getElementById('submit-btn');

      const name = document.getElementById('name').value.trim();
      const email = document.getElementById('email').value.trim();
      const password = document.getElementById('password').value;

      submitBtn.disabled = true;
      submitBtn.textContent = 'Creating account...';

      try {
        const data = await apiFetch('/auth/register', {
          method: 'POST',
          body: { name, email, password }
        });
        saveSession(data);
        showAlert(successEl, 'Account created! Redirecting to your dashboard...');
        setTimeout(() => { window.location.href = 'dashboard.html'; }, 900);
      } catch (err) {
        showAlert(alertEl, err.message);
        submitBtn.disabled = false;
        submitBtn.textContent = 'Register';
      }
    });
  }
});
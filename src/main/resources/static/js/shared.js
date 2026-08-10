// Injects the navbar and footer into every page and wires up logout.
// Usage: put <div id="navbar-root"></div> and <div id="footer-root"></div>
// in your HTML, then call renderNavbar('dashboard') with the current page id.

function renderNavbar(activePage) {
  const root = document.getElementById('navbar-root');
  if (!root) return;

  const loggedIn = isLoggedIn();
  const user = getUser();
  const initial = user && user.name ? user.name.charAt(0).toUpperCase() : 'U';

  const navLink = (id, label, href) =>
    `<a href="${href}" class="${activePage === id ? 'active' : ''}">${label}</a>`;

  const rightSide = loggedIn
    ? `<div class="navbar-user">
         <a href="profile.html" class="flex" style="align-items:center; gap:8px;">
           <span class="avatar-circle">${initial}</span>
           <span>${user ? user.name : 'Account'}</span>
         </a>
       </div>
       <button class="btn btn-ghost" onclick="logout()">Logout</button>`
    : `<a href="login.html" class="btn btn-ghost">Login</a>
       <a href="register.html" class="btn btn-primary">Register</a>`;

  root.innerHTML = `
    <nav class="navbar">
      <div class="navbar-inner">
        <a href="index.html" class="navbar-brand">
          <span class="logo-mark">IC</span>
          Indian Contract AI
        </a>
        <div class="navbar-center">
          ${navLink('home', 'Home', 'index.html')}
          ${navLink('dashboard', 'Dashboard', 'dashboard.html')}
          ${navLink('contracts', 'My Contracts', 'my-contracts.html')}
          ${navLink('about', 'About', 'index.html#about')}
        </div>
        <div class="navbar-right">${rightSide}</div>
      </div>
    </nav>
  `;
}

function renderFooter() {
  const root = document.getElementById('footer-root');
  if (!root) return;
  root.innerHTML = `
    <footer class="footer">
      <div class="footer-inner">
        <div class="navbar-brand" style="color:#fff;">
          <span class="logo-mark">IC</span> Indian Contract AI
        </div>
        <div class="footer-links">
          <a href="about.html">About</a>
          <a href="privacy.html">Privacy Policy</a>
          <a href="contact.html">Contact</a>
          <a href="terms.html">Terms of Service</a>
        </div>
      </div>
      <div class="footer-copy">© ${new Date().getFullYear()} Indian Contract AI. Built for Indian regulatory compliance.</div>
    </footer>
  `;
}
// Shared helpers used across dashboard.html, my-contracts.html, and
// contract-details.html — kept here since dashboard loads first in the flow.

function formatDate(isoString) {
  if (!isoString) return '—';
  const d = new Date(isoString);
  return d.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
}

function riskClass(score) {
  if (score === null || score === undefined) return 'risk-safe';
  if (score <= 25) return 'risk-safe';
  if (score <= 50) return 'risk-low';
  if (score <= 75) return 'risk-high';
  return 'risk-critical';
}

function riskLabel(score) {
  if (score === null || score === undefined) return '—';
  return Math.round(score);
}

function statusBadge(status) {
  const map = {
    uploaded:   '<span class="badge badge-gray">Uploaded</span>',
    processing: '<span class="badge badge-yellow">Processing</span>',
    done:       '<span class="badge badge-green">Completed</span>',
    error:      '<span class="badge badge-red">Error</span>'
  };
  return map[status] || `<span class="badge badge-gray">${status}</span>`;
}

function renderRiskCell(score) {
  if (score === null || score === undefined) {
    return '<span style="color:var(--text-muted);">—</span>';
  }
  return `<span class="risk-pill ${riskClass(score)}">${riskLabel(score)}</span>`;
}

async function loadDashboard() {
  const loadingEl = document.getElementById('loading-state');
  const emptyEl = document.getElementById('empty-state');
  const tableWrap = document.getElementById('table-wrap');
  const tbody = document.getElementById('contracts-tbody');

  const user = getUser();
  document.getElementById('welcome-msg').textContent = `Hello, ${user ? user.name : ''}`;

  try {
    const contracts = await apiFetch('/contracts/history');

    loadingEl.style.display = 'none';

    // Stats
    const total = contracts.length;
    const processing = contracts.filter(c => c.status === 'processing' || c.status === 'uploaded').length;
    const completed = contracts.filter(c => c.status === 'done').length;

    const withRisk = contracts.filter(c => c.legalRisk !== null && c.legalRisk !== undefined);
    const avgRisk = withRisk.length
      ? Math.round(withRisk.reduce((sum, c) => sum + ((Number(c.legalRisk) + Number(c.regRisk)) / 2), 0) / withRisk.length)
      : null;

    document.getElementById('stat-total').textContent = total;
    document.getElementById('stat-processing').textContent = processing;
    document.getElementById('stat-completed').textContent = completed;
    document.getElementById('stat-avg-risk').textContent = avgRisk === null ? '—' : avgRisk;

    if (total === 0) {
      emptyEl.style.display = 'block';
      return;
    }

    // Recent 5 only on the dashboard (full list lives on my-contracts.html)
    const recent = contracts.slice(0, 5);
    tbody.innerHTML = recent.map(c => `
      <tr>
        <td style="font-weight:600;">${escapeHtml(c.filename)}</td>
        <td>${formatDate(c.createdAt)}</td>
        <td>${statusBadge(c.status)}</td>
        <td>${renderRiskCell(c.legalRisk)}</td>
        <td>${renderRiskCell(c.regRisk)}</td>
        <td><a href="contract-details.html?id=${c.id}" class="btn btn-secondary">View Details</a></td>
      </tr>
    `).join('');

    tableWrap.style.display = 'block';

  } catch (err) {
    loadingEl.innerHTML = `<div style="color: var(--red); font-size: 14px;">Could not load contracts: ${err.message}</div>`;
  }
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}

//document.addEventListener('DOMContentLoaded', () => {
//  requireAuth();
//  renderNavbar('dashboard');
//  renderFooter();
//  loadDashboard();
//});
document.addEventListener('DOMContentLoaded', () => {
  // dashboard.js is also loaded on contract-details.html (for shared helpers
  // like escapeHtml/formatDate) — only run the dashboard page logic when
  // we're actually on dashboard.html.
  if (!document.getElementById('welcome-msg')) return;

  requireAuth();
  renderNavbar('dashboard');
  renderFooter();
  loadDashboard();
});
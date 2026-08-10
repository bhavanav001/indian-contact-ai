let allContracts = [];
let activeFilter = 'all';
let searchTerm = '';

// "High risk" / "low risk" buckets use the same 0-25/26-50/51-75/76-100
// bands as the rest of the app, based on the higher of legal/reg risk.
function overallRisk(c) {
  const legal = c.legalRisk !== null && c.legalRisk !== undefined ? Number(c.legalRisk) : null;
  const reg = c.regRisk !== null && c.regRisk !== undefined ? Number(c.regRisk) : null;
  if (legal === null && reg === null) return null;
  return Math.max(legal ?? 0, reg ?? 0);
}

function matchesFilter(c) {
  if (activeFilter === 'all') return true;
  if (activeFilter === 'completed') return c.status === 'done';
  if (activeFilter === 'processing') return c.status === 'processing' || c.status === 'uploaded';
  const risk = overallRisk(c);
  if (risk === null) return false;
  if (activeFilter === 'high-risk') return risk > 50;
  if (activeFilter === 'low-risk') return risk <= 50;
  return true;
}

function matchesSearch(c) {
  if (!searchTerm) return true;
  return c.filename.toLowerCase().includes(searchTerm.toLowerCase());
}

function renderContractCard(c) {
  return `
    <div class="card card-padded card-hover">
      <div class="flex-between mb-8" style="align-items:flex-start;">
        <div style="font-weight:700; font-size:15px; word-break:break-word;">${escapeHtml(c.filename)}</div>
        ${statusBadge(c.status)}
      </div>
      <div style="font-size:13px; color:var(--text-muted); margin-bottom:14px;">
        Uploaded ${formatDate(c.createdAt)}
      </div>
      <div class="flex gap-16 mb-16" style="font-size:13.5px;">
        <div>Legal: ${renderRiskCell(c.legalRisk)}</div>
        <div>Regulatory: ${renderRiskCell(c.regRisk)}</div>
      </div>
      <div class="flex gap-8">
        <a href="contract-details.html?id=${c.id}" class="btn btn-secondary" style="flex:1;">View</a>
        <a href="contract-details.html?id=${c.id}&tab=chat" class="btn btn-primary" style="flex:1;">Continue Chat</a>
      </div>
    </div>
  `;
}

function renderGrid() {
  const grid = document.getElementById('contracts-grid');
  const emptyEl = document.getElementById('empty-state');

  const filtered = allContracts.filter(c => matchesFilter(c) && matchesSearch(c));

  if (!filtered.length) {
    grid.innerHTML = '';
    emptyEl.style.display = 'block';
    return;
  }

  emptyEl.style.display = 'none';
  grid.innerHTML = filtered.map(renderContractCard).join('');
}

async function loadContracts() {
  const loadingEl = document.getElementById('loading-state');
  try {
    allContracts = await apiFetch('/contracts/history');
    loadingEl.style.display = 'none';
    renderGrid();
  } catch (err) {
    loadingEl.innerHTML = `<div style="color: var(--red); font-size: 14px;">Could not load contracts: ${err.message}</div>`;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  requireAuth();
  renderNavbar('contracts');
  renderFooter();

  document.getElementById('search-input').addEventListener('input', (e) => {
    searchTerm = e.target.value;
    renderGrid();
  });

  document.querySelectorAll('.filter-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      activeFilter = btn.dataset.filter;
      renderGrid();
    });
  });

  loadContracts();
});
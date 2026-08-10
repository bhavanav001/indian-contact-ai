const RING_CIRCUMFERENCE = 326.7; // 2 * PI * 52 (matches the SVG circle radius)
let currentContractId = null;
let pollTimer = null;
function getTabFromUrl() {
  const params = new URLSearchParams(window.location.search);
  return params.get('tab'); // null if not present
}
function getContractIdFromUrl() {
  const params = new URLSearchParams(window.location.search);
  return params.get('id');
}

function setRing(circleId, textId, score) {
  const circle = document.getElementById(circleId);
  const text = document.getElementById(textId);
  const value = score === null || score === undefined ? 0 : Number(score);

  const offset = RING_CIRCUMFERENCE - (value / 100) * RING_CIRCUMFERENCE;
  circle.style.stroke = riskStrokeColor(value);
  circle.setAttribute('stroke-dashoffset', offset);
  text.textContent = Math.round(value);
}

function riskStrokeColor(score) {
  if (score <= 25) return '#1E8E5A';
  if (score <= 50) return '#B7791F';
  if (score <= 75) return '#C1652E';
  return '#C0392B';
}
// ─────────────────────────────────────────────
// COMPLIANCE TAB — grouped by (lawName + sectionNum)
// ─────────────────────────────────────────────

// Defensive: works whether the API sends clause.clauseNumber (nested),
// clauseNumber (flat), or clauseId (flat id, no number). Falls back to
// null gracefully if none exist so this never crashes the render.

function severityBadge(severity) {
  const map = {
    LOW:      '<span class="badge badge-yellow">Low</span>',
    MEDIUM:   '<span class="badge badge-yellow">Medium</span>',
    HIGH:     '<span class="badge badge-red">High Risk</span>',
    CRITICAL: '<span class="badge badge-red">Critical</span>'
  };
  return map[severity] || `<span class="badge badge-gray">${severity}</span>`;
}


function getClauseNumber(f) {
  if (f.clause && f.clause.clauseNumber != null) return f.clause.clauseNumber;
  if (f.clauseNumber != null) return f.clauseNumber;
  if (f.clauseId != null) return f.clauseId;
  return null;
}

function renderCompliance(flags) {
  const list = document.getElementById('compliance-list');

  if (!flags.length) {
    list.innerHTML = `
      <div class="card empty-state">
        <div class="empty-state-icon">✅</div>
        <span class="badge badge-green" style="font-size:13px; padding:6px 14px;">Compliant</span>
        <div style="margin-top:10px;">No compliance issues were flagged against Indian law for this contract.</div>
      </div>`;
    return;
  }

  // STEP 1 — Group by (lawName + sectionNum) instead of lawName alone,
  // so "Section 9" and "Section 31" under GST don't get merged together.
  const groups = {};
  flags.forEach(f => {
    const key = `${f.lawName || 'Other'}|||${f.sectionNum || ''}`;
    if (!groups[key]) {
      groups[key] = {
        lawName: f.lawName || 'Other',
        sectionNum: f.sectionNum || '',
        severity: f.severity,
        violation: f.violation,
        clauseNumbers: []
      };
    }
    const num = getClauseNumber(f);
    if (num !== null) groups[key].clauseNumbers.push(num);
  });

  // STEP 2 — Re-group by lawName for the section headers (unchanged from before)
  const byLaw = {};
  Object.values(groups).forEach(g => {
    if (!byLaw[g.lawName]) byLaw[g.lawName] = [];
    byLaw[g.lawName].push(g);
  });

  let groupIndex = 0;

  list.innerHTML = Object.entries(byLaw).map(([lawName, items]) => `
    <div class="mb-24">
      <h4 style="font-size:13px; text-transform:uppercase; color:var(--text-muted); letter-spacing:.03em; margin-bottom:10px;">
        ${escapeHtml(lawName)} · ${items.length} distinct issue${items.length > 1 ? 's' : ''}
      </h4>
      ${items.map(g => {
        const id = `compliance-group-${groupIndex++}`;
        const count = g.clauseNumbers.length;
        return `
        <div class="card card-padded mb-16">
          <div class="flex-between mb-8" style="align-items:flex-start; cursor:pointer;" onclick="toggleComplianceGroup('${id}')">
            <div style="font-weight:700; font-size:14.5px;">${escapeHtml(g.sectionNum)}</div>
            <div style="display:flex; align-items:center; gap:8px;">
              ${count > 0 ? `<span class="badge badge-gray">${count} clause${count > 1 ? 's' : ''} affected</span>` : ''}
              ${severityBadge(g.severity)}
            </div>
          </div>
          <div style="font-size:14px; color:var(--text); line-height:1.55;">${escapeHtml(g.violation)}</div>
          ${count > 0 ? `
            <div id="${id}" class="compliance-clause-list" style="display:none; margin-top:10px;">
              ${g.clauseNumbers.map(n => `<span class="badge badge-gray" style="margin-right:6px; margin-bottom:6px; display:inline-block;">Clause ${n}</span>`).join('')}
            </div>
            <div style="margin-top:8px; font-size:12.5px; color:var(--text-muted); cursor:pointer;" onclick="toggleComplianceGroup('${id}')">
              <span id="${id}-toggle-label">Show affected clauses ▾</span>
            </div>
          ` : ''}
        </div>
      `;}).join('')}
    </div>
  `).join('');
}

function toggleComplianceGroup(id) {
  const el = document.getElementById(id);
  const label = document.getElementById(id + '-toggle-label');
  if (!el) return;
  const isHidden = el.style.display === 'none';
  el.style.display = isHidden ? 'block' : 'none';
  if (label) label.textContent = isHidden ? 'Hide affected clauses ▴' : 'Show affected clauses ▾';
}
// ─────────────────────────────────────────────
// OBLIGATIONS TAB
// ─────────────────────────────────────────────

function priorityBadge(priority) {
  const map = {
    LOW:    '<span class="badge badge-gray">Low priority</span>',
    MEDIUM: '<span class="badge badge-yellow">Medium priority</span>',
    HIGH:   '<span class="badge badge-red">High priority</span>'
  };
  return map[priority] || '';
}

function renderObligations(obligations) {
  const list = document.getElementById('obligations-list');

  if (!obligations.length) {
    list.innerHTML = `<div class="card empty-state"><div class="empty-state-icon">🗂️</div>No obligations were extracted from this contract.</div>`;
    return;
  }

  list.innerHTML = obligations.map(o => `
    <div class="card obligation-card" id="obligation-${o.id}">
      <div class="obligation-check ${o.isFulfilled ? 'done' : ''}">${o.isFulfilled ? '✓' : ''}</div>
      <div class="obligation-main">
        <div class="obligation-action">${escapeHtml(o.party || 'Party')} must ${escapeHtml(o.action || '')}</div>
        <div class="obligation-meta">Due: ${escapeHtml(o.deadline || 'Not specified')} &nbsp;·&nbsp; ${priorityBadge(o.priority)}</div>
      </div>
      ${o.isFulfilled
        ? ''
        : `<button class="btn btn-secondary" onclick="fulfillObligation(${o.id})">Mark as Fulfilled</button>`
      }
    </div>
  `).join('');
}

async function fulfillObligation(obligationId) {
  try {
    await apiFetch(`/contracts/${currentContractId}/obligations/${obligationId}/fulfill`, {
      method: 'PATCH'
    });
    const card = document.getElementById(`obligation-${obligationId}`);
    card.querySelector('.obligation-check').classList.add('done');
    card.querySelector('.obligation-check').textContent = '✓';
    const btn = card.querySelector('button');
    if (btn) btn.remove();
  } catch (err) {
    alert('Could not update obligation: ' + err.message);
  }
}

// ─────────────────────────────────────────────
// AI CHAT TAB
// ─────────────────────────────────────────────

const SUGGESTED_QUESTIONS = [
  'Summarize this contract',
  'What are my responsibilities?',
  'Are there risky clauses?',
  'Does this contract contain penalties?',
  'Explain Clause 5 in simple language'
];

async function initChatTab(contractId) {
  const chatCard = document.getElementById('chat-card');
  chatCard.innerHTML = `
    <div class="chat-wrap">
      <div class="chat-messages" id="chat-messages"></div>
      <div class="chat-suggestions" id="chat-suggestions">
        ${SUGGESTED_QUESTIONS.map(q => `<span class="chat-chip" onclick="sendChatMessage('${q.replace(/'/g, "\\'")}')">${q}</span>`).join('')}
      </div>
      <div class="chat-input-row">
        <input type="text" id="chat-input" class="form-input" placeholder="Ask anything about this contract...">
        <button class="btn btn-primary" id="chat-send-btn" onclick="sendChatMessage()">Send</button>
      </div>
    </div>
  `;

  document.getElementById('chat-input').addEventListener('keydown', (e) => {
    if (e.key === 'Enter') sendChatMessage();
  });

  try {
    const history = await apiFetch(`/contracts/${contractId}/chat`);
    const messagesEl = document.getElementById('chat-messages');
    if (!history.length) {
      messagesEl.innerHTML = `<div style="text-align:center; color:var(--text-muted); font-size:13.5px; margin-top:20px;">Ask a question below to start chatting about this contract.</div>`;
    } else {
      messagesEl.innerHTML = history.map(m => chatBubbleHtml(m.role, m.message)).join('');
      messagesEl.scrollTop = messagesEl.scrollHeight;
    }
  } catch (err) {
    document.getElementById('chat-messages').innerHTML =
      `<div style="text-align:center; color:var(--red); font-size:13.5px;">Could not load chat history: ${err.message}</div>`;
  }
}

function chatBubbleHtml(role, message) {
  const cls = role === 'user' ? 'user' : 'assistant';
  return `<div class="chat-bubble ${cls}">${escapeHtml(message)}</div>`;
}

async function sendChatMessage(presetText) {
  const input = document.getElementById('chat-input');
  const text = (presetText !== undefined ? presetText : input.value).trim();
  if (!text) return;

  const messagesEl = document.getElementById('chat-messages');
  const sendBtn = document.getElementById('chat-send-btn');

  // Clear the empty-state hint the first time a message is sent
  if (messagesEl.children.length === 1 && messagesEl.textContent.includes('Ask a question')) {
    messagesEl.innerHTML = '';
  }

  messagesEl.insertAdjacentHTML('beforeend', chatBubbleHtml('user', text));
  input.value = '';
  sendBtn.disabled = true;
  messagesEl.scrollTop = messagesEl.scrollHeight;

  const typingId = 'typing-' + Date.now();
  messagesEl.insertAdjacentHTML('beforeend',
    `<div class="chat-bubble assistant" id="${typingId}" style="color:var(--text-muted);">Thinking...</div>`);
  messagesEl.scrollTop = messagesEl.scrollHeight;

  try {
    const data = await apiFetch(`/contracts/${currentContractId}/chat`, {
      method: 'POST',
      body: { message: text }
    });
    document.getElementById(typingId).outerHTML = chatBubbleHtml('assistant', data.response);
  } catch (err) {
    document.getElementById(typingId).outerHTML =
      `<div class="chat-bubble assistant" style="color:var(--red);">Error: ${escapeHtml(err.message)}</div>`;
  } finally {
    sendBtn.disabled = false;
    messagesEl.scrollTop = messagesEl.scrollHeight;
  }
}
function switchTab(tabName) {
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.tab === tabName);
  });
  document.querySelectorAll('.tab-panel').forEach(panel => {
    panel.classList.toggle('active', panel.id === `tab-${tabName}`);
  });
}

function toggleClause(id) {
  const body = document.getElementById(`clause-body-${id}`);
  const chevron = document.getElementById(`chevron-${id}`);
  body.classList.toggle('open');
  chevron.classList.toggle('open');
}

function renderClauses(clauses) {
  const list = document.getElementById('clauses-list');

  if (!clauses.length) {
    list.innerHTML = `<div class="empty-state"><div class="empty-state-icon">📋</div>No clauses were extracted from this contract.</div>`;
    return;
  }

  list.innerHTML = clauses.map(c => `
    <div class="card clause-card">
      <div class="clause-header" onclick="toggleClause(${c.id})">
        <div class="clause-header-left">
          <div class="clause-num">${c.clauseNumber ?? '–'}</div>
          <div>
            <div class="clause-type-label">${formatClauseType(c.clauseType)}</div>
            <div class="clause-snippet">${escapeHtml((c.clauseText || '').slice(0, 90))}${(c.clauseText || '').length > 90 ? '…' : ''}</div>
          </div>
        </div>
        <div class="flex gap-16" style="align-items:center;">
          <div class="clause-scores">
            <span class="risk-pill ${riskClass(c.legalScore)}">L ${riskLabel(c.legalScore)}</span>
            <span class="risk-pill ${riskClass(c.regScore)}">R ${riskLabel(c.regScore)}</span>
          </div>
          <span class="chevron" id="chevron-${c.id}">▾</span>
        </div>
      </div>
      <div class="clause-body" id="clause-body-${c.id}">
        <h4>Clause Text</h4>
        <p>${escapeHtml(c.clauseText)}</p>
        <h4>Plain-English Explanation</h4>
        <p>${escapeHtml(c.llmExplanation || 'Not available')}</p>
        <h4>Suggested Rewrite</h4>
        <p>${escapeHtml(c.suggestion || 'No change needed')}</p>
      </div>
    </div>
  `).join('');
}

function formatClauseType(type) {
  if (!type) return 'Other';
  return type.replace(/_/g, ' ').replace(/\b\w/g, ch => ch.toUpperCase());
}

async function loadOverviewAndClauses(contract) {
  document.getElementById('ov-filename').textContent = contract.filename;
  document.getElementById('ov-date').textContent = formatDate(contract.createdAt);
  document.getElementById('ov-status').textContent = contract.status;
  document.getElementById('ov-clauses').textContent = contract.totalClauses ?? 0;
  document.getElementById('ov-summary-clauses').textContent = contract.totalClauses ?? 0;

  setRing('legal-ring', 'legal-ring-text', contract.legalRisk);
  setRing('reg-ring', 'reg-ring-text', contract.regRisk);
  document.getElementById('total-clauses-num').textContent = contract.totalClauses ?? 0;

  try {
    const risksData = await apiFetch(`/contracts/${contract.id}/risks`);
    renderClauses(risksData.clauses || []);
  } catch (err) {
    document.getElementById('clauses-list').innerHTML =
      `<div class="empty-state">Could not load clauses: ${err.message}</div>`;
  }

  try {
    const flags = await apiFetch(`/contracts/${contract.id}/compliance`);
    document.getElementById('ov-summary-flags').textContent = flags.length;
    renderCompliance(flags);          // defined in the next message
  }  catch (err) { console.error('COMPLIANCE ERROR:', err); }

  try {
    const obligations = await apiFetch(`/contracts/${contract.id}/obligations`);
    renderObligations(obligations);   // defined in the next message
  } catch (err) { /* obligations tab shows its own error */ }

  initChatTab(contract.id);

  // If the URL asked for a specific tab (e.g. "Continue Chat" from My Contracts), open it.
  const requestedTab = getTabFromUrl();
  if (requestedTab) {
    switchTab(requestedTab);
  }
}

async function pollContract() {
  try {
    const contract = await apiFetch(`/contracts/${currentContractId}`);

    document.getElementById('contract-name').textContent = contract.filename;
    document.getElementById('contract-date').textContent = `Uploaded ${formatDate(contract.createdAt)}`;
    document.getElementById('contract-status-badge').innerHTML = statusBadge(contract.status);

    if (contract.status === 'done') {
      clearInterval(pollTimer);
      document.getElementById('processing-state').style.display = 'none';
      document.getElementById('error-state').style.display = 'none';
      document.getElementById('details-content').style.display = 'block';
      loadOverviewAndClauses(contract);
    } else if (contract.status === 'error') {
      clearInterval(pollTimer);
      document.getElementById('processing-state').style.display = 'none';
      document.getElementById('error-state').style.display = 'block';
    } else {
      document.getElementById('processing-state').style.display = 'block';
      document.getElementById('error-state').style.display = 'none';
    }
  } catch (err) {
    clearInterval(pollTimer);
    document.getElementById('contract-name').textContent = 'Could not load contract';
  }
}

document.addEventListener('DOMContentLoaded', () => {
  requireAuth();
  renderNavbar('contracts');
  renderFooter();

  currentContractId = getContractIdFromUrl();
  if (!currentContractId) {
    document.getElementById('contract-name').textContent = 'No contract selected';
    return;
  }

  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => switchTab(btn.dataset.tab));
  });

  pollContract();
  pollTimer = setInterval(pollContract, 4000); // poll every 4s while processing
});
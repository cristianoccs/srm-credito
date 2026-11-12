const state = { page: 0, size: 10, totalPages: 1, latestSimulation: null, simulationTimer: null };

const $ = (selector) => document.querySelector(selector);
const formatBRL = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const formatUSD = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' });
const formatMoney = (value, currency = 'BRL') => value == null ? '—' : (currency === 'USD' ? formatUSD : formatBRL).format(Number(value));
const formatDate = (value) => value ? new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value)) : '—';
const today = () => new Date().toISOString().slice(0, 10);

function setStatus(message, type = '') {
  const status = $('#simulationStatus');
  status.textContent = message;
  status.className = `form-status ${type}`.trim();
}

function showError(message) {
  const error = $('#simulationError');
  error.textContent = message;
  error.hidden = !message;
}

async function parseResponse(response) {
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    const detail = payload.message || payload.error || 'Não foi possível concluir a operação.';
    throw new Error(detail);
  }
  return payload;
}

function selectedCurrency() {
  return document.querySelector('input[name="paymentCurrency"]:checked').value;
}

function simulationPayload() {
  return {
    faceValue: Number($('#faceValue').value),
    termMonths: Number($('#termMonths').value),
    type: $('#receivableType').value,
    paymentCurrency: selectedCurrency()
  };
}

function renderSimulation(result) {
  state.latestSimulation = result;
  const currency = result.paymentCurrency || 'BRL';
  $('#resultCurrency').textContent = currency;
  $('#resultFinal').textContent = formatMoney(result.finalAmount, currency);
  $('#resultFace').textContent = formatMoney(result.faceValueBrl, 'BRL');
  $('#resultPresent').textContent = formatMoney(result.presentValueBrl, 'BRL');
  $('#resultDiscount').textContent = formatMoney(result.discountBrl, 'BRL');
  $('#metricNet').textContent = formatMoney(result.finalAmount, currency);
  $('#metricCurrency').textContent = `Valor líquido estimado em ${currency}`;
  $('#metricDiscount').textContent = formatMoney(result.discountBrl, 'BRL');
  $('#resultBadge').textContent = 'Pronto';
  $('#resultBadge').className = 'badge badge-success';
  $('#resultHint').textContent = currency === 'USD' ? 'Conversão aplicada após o arredondamento do valor presente em BRL.' : 'Estimativa baseada no tipo e prazo informados.';
  const exchangeRow = $('#exchangeRow');
  exchangeRow.hidden = !result.exchangeRate || currency === 'BRL';
  $('#resultExchange').textContent = result.exchangeRate ? `1 USD = ${Number(result.exchangeRate).toFixed(4)} BRL` : '—';
}

async function simulate({ silent = false } = {}) {
  const payload = simulationPayload();
  if (!payload.faceValue || payload.faceValue <= 0 || !payload.termMonths || payload.termMonths < 1) {
    if (!silent) setStatus('Informe valor e prazo válidos.');
    return;
  }
  setStatus('Calculando com o motor financeiro…', 'loading');
  showError('');
  try {
    const response = await fetch('/api/pricing/simulations', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
    renderSimulation(await parseResponse(response));
    setStatus('Simulação atualizada agora.');
  } catch (error) {
    setStatus('Não foi possível simular.');
    showError(error.message);
  }
}

function scheduleSimulation() {
  clearTimeout(state.simulationTimer);
  state.simulationTimer = setTimeout(() => simulate({ silent: true }), 420);
}

function filters() {
  const params = new URLSearchParams({ page: state.page, size: state.size });
  const assignor = $('#assignorFilter').value.trim();
  const currency = $('#currencyFilter').value;
  if (assignor) params.set('assignor', assignor);
  if (currency) params.set('currency', currency);
  if ($('#fromFilter').value) params.set('from', $('#fromFilter').value);
  if ($('#toFilter').value) params.set('to', $('#toFilter').value);
  return params;
}

function renderStatement(page) {
  const body = $('#statementBody');
  const items = page.content || [];
  $('#metricSettlements').textContent = page.totalElements ?? items.length;
  $('#metricPage').textContent = page.totalElements ? `${page.totalElements} transações registradas` : 'Nenhuma transação registrada';
  $('#statementSummary').textContent = page.totalElements ? `Exibindo ${items.length} de ${page.totalElements} registros` : 'Nenhum registro para exibir';
  state.totalPages = Math.max(page.totalPages || 1, 1);
  $('#pageNumber').textContent = `${(page.number || 0) + 1} / ${state.totalPages}`;
  $('#previousPage').disabled = !page.first;
  $('#nextPage').disabled = !page.last;
  if (!items.length) {
    body.innerHTML = '<tr><td colspan="6" class="empty-state"><span class="empty-icon">◌</span><strong>Nenhuma liquidação encontrada</strong><small>As transações confirmadas aparecerão aqui.</small></td></tr>';
    return;
  }
  body.innerHTML = items.map((item) => `<tr><td>${escapeHtml(item.assignor)}</td><td><span title="${item.receivableId}">${item.receivableId.slice(0, 8)}…</span></td><td><strong>${formatMoney(item.presentValueBrl, 'BRL')}</strong></td><td><strong>${formatMoney(item.finalAmount, item.paymentCurrency)}</strong></td><td><span class="currency-tag">${item.paymentCurrency}</span></td><td>${formatDate(item.settledAt)}</td></tr>`).join('');
}

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>'"]/g, (character) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#039;', '"': '&quot;' })[character]);
}

async function loadStatement() {
  const body = $('#statementBody');
  body.innerHTML = '<tr><td colspan="6" class="empty-state"><span class="empty-icon">↻</span><strong>Carregando extrato…</strong><small>Consultando transações auditáveis.</small></td></tr>';
  try {
    const response = await fetch(`/api/settlements?${filters()}`);
    renderStatement(await parseResponse(response));
  } catch (error) {
    body.innerHTML = `<tr><td colspan="6" class="empty-state"><span class="empty-icon">!</span><strong>Falha ao carregar o extrato</strong><small>${escapeHtml(error.message)}</small></td></tr>`;
  }
}

function bindEvents() {
  $('#simulationForm').addEventListener('submit', (event) => { event.preventDefault(); simulate(); });
  ['#faceValue', '#termMonths', '#receivableType'].forEach((selector) => $(selector).addEventListener('input', scheduleSimulation));
  document.querySelectorAll('input[name="paymentCurrency"]').forEach((input) => input.addEventListener('change', (event) => { document.querySelectorAll('.currency-option').forEach((option) => option.classList.toggle('selected', option.contains(event.target))); scheduleSimulation(); }));
  $('#filterStatement').addEventListener('click', () => { state.page = 0; loadStatement(); });
  $('#refreshStatement').addEventListener('click', loadStatement);
  $('#previousPage').addEventListener('click', () => { if (state.page > 0) { state.page -= 1; loadStatement(); } });
  $('#nextPage').addEventListener('click', () => { if (state.page < state.totalPages - 1) { state.page += 1; loadStatement(); } });
  $('#assignorFilter').addEventListener('keydown', (event) => { if (event.key === 'Enter') { state.page = 0; loadStatement(); } });
}

function boot() {
  $('#currentDate').textContent = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'full' }).format(new Date());
  bindEvents();
  simulate({ silent: true });
  loadStatement();
}

document.addEventListener('DOMContentLoaded', boot);

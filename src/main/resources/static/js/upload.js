const MAX_SIZE_BYTES = 20 * 1024 * 1024; // 20MB — matches application.properties
let selectedFile = null;

function showUploadAlert(message) {
  const el = document.getElementById('alert');
  el.textContent = message;
  el.classList.add('show');
}

function hideUploadAlert() {
  document.getElementById('alert').classList.remove('show');
}

function validateFile(file) {
  const lower = file.name.toLowerCase();
  if (!lower.endsWith('.pdf') && !lower.endsWith('.docx')) {
    return 'Only PDF and DOCX files are supported.';
  }
  if (file.size > MAX_SIZE_BYTES) {
    return 'File is too large. Maximum size is 20MB.';
  }
  return null;
}

function setSelectedFile(file) {
  hideUploadAlert();
  const error = validateFile(file);
  if (error) {
    showUploadAlert(error);
    selectedFile = null;
    document.getElementById('analyze-btn').disabled = true;
    document.getElementById('file-chip-wrap').innerHTML = '';
    return;
  }

  selectedFile = file;
  document.getElementById('analyze-btn').disabled = false;
  const sizeKb = (file.size / 1024).toFixed(0);
  document.getElementById('file-chip-wrap').innerHTML = `
    <div class="file-chip">📎 ${file.name} · ${sizeKb} KB</div>
  `;
}

async function submitUpload() {
  if (!selectedFile) return;

  document.getElementById('upload-step').style.display = 'none';
  document.getElementById('loading-step').style.display = 'block';

  const formData = new FormData();
  formData.append('file', selectedFile);

  try {
    const data = await apiFetch('/contracts/upload', {
      method: 'POST',
      body: formData,
      isFormData: true
    });
    // Backend runs analysis in the background; contract-details.html
    // polls GET /api/contracts/{id} until status is 'done'.
    window.location.href = `contract-details.html?id=${data.id}`;
  } catch (err) {
    document.getElementById('upload-step').style.display = 'block';
    document.getElementById('loading-step').style.display = 'none';
    showUploadAlert(err.message);
  }
}

document.addEventListener('DOMContentLoaded', () => {
  requireAuth();
  renderNavbar('dashboard');
  renderFooter();

  const dropzone = document.getElementById('dropzone');
  const fileInput = document.getElementById('file-input');
  const analyzeBtn = document.getElementById('analyze-btn');

  dropzone.addEventListener('click', () => fileInput.click());

  fileInput.addEventListener('change', () => {
    if (fileInput.files.length) setSelectedFile(fileInput.files[0]);
  });

  ['dragenter', 'dragover'].forEach(evt => {
    dropzone.addEventListener(evt, (e) => {
      e.preventDefault();
      dropzone.classList.add('dragover');
    });
  });

  ['dragleave', 'drop'].forEach(evt => {
    dropzone.addEventListener(evt, (e) => {
      e.preventDefault();
      dropzone.classList.remove('dragover');
    });
  });

  dropzone.addEventListener('drop', (e) => {
    if (e.dataTransfer.files.length) setSelectedFile(e.dataTransfer.files[0]);
  });

  analyzeBtn.addEventListener('click', submitUpload);
});
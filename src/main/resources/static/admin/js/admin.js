/**
 * CampusBrain – Admin Management Console
 * Complete Admin operations: Upload, Manage, Edit, Replace, Publish/Unpublish, Delete
 */

(function () {
  'use strict';

  // Direct Access: No admin login requirement for uploading or managing files

  // Application State
  const state = {
    user: { username: 'admin', fullName: 'Academic Administrator', email: 'admin@campusbrain.edu' },
    stats: null,
    materials: [],
    filterSearch: '',
    filterCategory: 'ALL',
    filterSubject: '',
    filterStatus: 'ALL',
    activeTab: 'uploadTab', // 'uploadTab' or 'manageTab'
    selectedUploadFile: null,
    selectedReplaceFile: null,
    editingMaterialId: null,
    replacingMaterialId: null,
    deletingMaterialId: null
  };

  // Supported Extensions Map
  const FORMAT_MAP = {
    pdf: 'PDF Document (.pdf)',
    doc: 'Word Document (.doc)',
    docx: 'Word Document (.docx)',
    ppt: 'PowerPoint Presentation (.ppt)',
    pptx: 'PowerPoint Presentation (.pptx)',
    xls: 'Excel Spreadsheet (.xls)',
    xlsx: 'Excel Spreadsheet (.xlsx)',
    txt: 'Text File (.txt)'
  };

  // DOM Elements
  const adminNameDisplay = document.getElementById('adminNameDisplay');
  const adminEmailDisplay = document.getElementById('adminEmailDisplay');
  const adminAvatar = document.getElementById('adminAvatar');
  const logoutBtn = document.getElementById('logoutBtn');

  // Stats Elements
  const statTotal = document.getElementById('statTotal');
  const statPublished = document.getElementById('statPublished');
  const statDraft = document.getElementById('statDraft');
  const statNotes = document.getElementById('statNotes');
  const statLab = document.getElementById('statLab');
  const statAssign = document.getElementById('statAssign');
  const statPapers = document.getElementById('statPapers');

  // Navigation Items
  const navUpload = document.getElementById('navUpload');
  const navManage = document.getElementById('navManage');
  const uploadSection = document.getElementById('uploadSection');
  const manageSection = document.getElementById('manageSection');
  const topbarTitle = document.getElementById('topbarTitle');

  // Upload Form Elements
  const uploadForm = document.getElementById('uploadForm');
  const fileDropzone = document.getElementById('fileDropzone');
  const fileInput = document.getElementById('fileInput');
  const detectedFileCard = document.getElementById('detectedFileCard');
  const detectedFileName = document.getElementById('detectedFileName');
  const detectedFileDetails = document.getElementById('detectedFileDetails');
  const detectedFileIcon = document.getElementById('detectedFileIcon');
  const removeSelectedFileBtn = document.getElementById('removeSelectedFileBtn');
  const uploadSubmitBtn = document.getElementById('uploadSubmitBtn');
  const uploadSpinner = document.getElementById('uploadSpinner');
  const uploadBtnText = document.getElementById('uploadBtnText');

  // Manage Elements
  const tableSearchInput = document.getElementById('tableSearchInput');
  const tableCategoryFilter = document.getElementById('tableCategoryFilter');
  const tableStatusFilter = document.getElementById('tableStatusFilter');
  const tableSubjectFilter = document.getElementById('tableSubjectFilter');
  const materialsTableBody = document.getElementById('materialsTableBody');
  const tableCountBadge = document.getElementById('tableCountBadge');

  // Modals
  const editModal = document.getElementById('editModal');
  const editForm = document.getElementById('editForm');
  const closeEditModalBtn = document.getElementById('closeEditModalBtn');
  const cancelEditBtn = document.getElementById('cancelEditBtn');

  const replaceModal = document.getElementById('replaceModal');
  const replaceFileInput = document.getElementById('replaceFileInput');
  const replaceDropzone = document.getElementById('replaceDropzone');
  const replaceDetectedCard = document.getElementById('replaceDetectedCard');
  const replaceDetectedFileName = document.getElementById('replaceDetectedFileName');
  const replaceDetectedDetails = document.getElementById('replaceDetectedDetails');
  const closeReplaceModalBtn = document.getElementById('closeReplaceModalBtn');
  const cancelReplaceBtn = document.getElementById('cancelReplaceBtn');
  const submitReplaceBtn = document.getElementById('submitReplaceBtn');

  const deleteModal = document.getElementById('deleteModal');
  const deleteMaterialTitle = document.getElementById('deleteMaterialTitle');
  const closeDeleteModalBtn = document.getElementById('closeDeleteModalBtn');
  const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');
  const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');

  const toastContainer = document.getElementById('adminToastContainer');

  /**
   * Helper: API Fetch (Direct Access)
   */
  async function authFetch(url, options = {}) {
    options.headers = options.headers || {};
    const savedToken = localStorage.getItem('campusbrain_admin_token');
    if (savedToken) {
      options.headers['Authorization'] = `Bearer ${savedToken}`;
    }
    return await fetch(url, options);
  }

  /**
   * Initialize Admin App
   */
  async function init() {
    setupEventListeners();
    await verifyProfile();
    await refreshStats();
    await loadMaterials();
    connectRealtimeSync();
  }

  /**
   * Verify Admin Profile
   */
  async function verifyProfile() {
    try {
      const res = await authFetch('/api/admin/auth/me');
      if (res.ok) {
        const user = await res.json();
        state.user = user;
      }
    } catch (err) {
      // Use fallback
    }
    if (adminNameDisplay) adminNameDisplay.textContent = state.user.fullName || 'Academic Administrator';
    if (adminEmailDisplay) adminEmailDisplay.textContent = state.user.email || 'admin@campusbrain.edu';
    if (adminAvatar) adminAvatar.textContent = 'A';
  }

  /**
   * Setup Event Listeners
   */
  function setupEventListeners() {
    // Back to Student Portal
    if (logoutBtn) {
      logoutBtn.addEventListener('click', () => {
        window.location.href = '/';
      });
    }

    // Sidebar navigation
    if (navUpload) {
      navUpload.addEventListener('click', () => switchTab('uploadTab'));
    }
    if (navManage) {
      navManage.addEventListener('click', () => switchTab('manageTab'));
    }

    // Drag and drop for upload
    setupDropzone(fileDropzone, fileInput, handleFileSelected);
    if (removeSelectedFileBtn) {
      removeSelectedFileBtn.addEventListener('click', clearSelectedFile);
    }

    // Upload form submission
    if (uploadForm) {
      uploadForm.addEventListener('submit', handleUploadSubmit);
    }

    // Table Filters
    if (tableSearchInput) {
      tableSearchInput.addEventListener('input', (e) => {
        state.filterSearch = e.target.value.trim().toLowerCase();
        renderMaterialsTable();
      });
    }

    if (tableCategoryFilter) {
      tableCategoryFilter.addEventListener('change', (e) => {
        state.filterCategory = e.target.value;
        renderMaterialsTable();
      });
    }

    if (tableStatusFilter) {
      tableStatusFilter.addEventListener('change', (e) => {
        state.filterStatus = e.target.value;
        renderMaterialsTable();
      });
    }

    if (tableSubjectFilter) {
      tableSubjectFilter.addEventListener('input', (e) => {
        state.filterSubject = e.target.value.trim().toLowerCase();
        renderMaterialsTable();
      });
    }

    // Modals close buttons
    if (closeEditModalBtn) closeEditModalBtn.addEventListener('click', () => closeAllModals());
    if (cancelEditBtn) cancelEditBtn.addEventListener('click', () => closeAllModals());
    if (editForm) editForm.addEventListener('submit', handleEditSubmit);

    // Replace modal
    setupDropzone(replaceDropzone, replaceFileInput, handleReplaceFileSelected);
    if (closeReplaceModalBtn) closeReplaceModalBtn.addEventListener('click', () => closeAllModals());
    if (cancelReplaceBtn) cancelReplaceBtn.addEventListener('click', () => closeAllModals());
    if (submitReplaceBtn) submitReplaceBtn.addEventListener('click', handleReplaceSubmit);

    // Delete modal
    if (closeDeleteModalBtn) closeDeleteModalBtn.addEventListener('click', () => closeAllModals());
    if (cancelDeleteBtn) cancelDeleteBtn.addEventListener('click', () => closeAllModals());
    if (confirmDeleteBtn) confirmDeleteBtn.addEventListener('click', handleDeleteConfirm);

    // Close on backdrop click & ESC
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') closeAllModals();
    });
  }

  function switchTab(tab) {
    state.activeTab = tab;
    if (tab === 'uploadTab') {
      navUpload.classList.add('active');
      navManage.classList.remove('active');
      uploadSection.classList.add('active');
      manageSection.classList.remove('active');
      if (topbarTitle) topbarTitle.textContent = 'Upload New Material';
    } else {
      navUpload.classList.remove('active');
      navManage.classList.add('active');
      uploadSection.classList.remove('active');
      manageSection.classList.add('active');
      if (topbarTitle) topbarTitle.textContent = 'Manage Academic Materials';
      loadMaterials();
    }
  }

  /**
   * File Dropzone Setup with Auto Format Detection
   */
  function setupDropzone(dropzoneEl, inputEl, callback) {
    if (!dropzoneEl || !inputEl) return;

    ['dragenter', 'dragover'].forEach(eventName => {
      dropzoneEl.addEventListener(eventName, (e) => {
        e.preventDefault();
        e.stopPropagation();
        dropzoneEl.classList.add('dragover');
      });
    });

    ['dragleave', 'drop'].forEach(eventName => {
      dropzoneEl.addEventListener(eventName, (e) => {
        e.preventDefault();
        e.stopPropagation();
        dropzoneEl.classList.remove('dragover');
      });
    });

    dropzoneEl.addEventListener('drop', (e) => {
      const files = e.dataTransfer.files;
      if (files && files.length > 0) {
        callback(files[0]);
      }
    });

    inputEl.addEventListener('change', (e) => {
      if (e.target.files && e.target.files.length > 0) {
        callback(e.target.files[0]);
      }
    });
  }

  /**
   * Handle File Selection and Automatic Format Detection
   */
  function handleFileSelected(file) {
    const ext = getFileExtension(file.name).toLowerCase();
    if (!FORMAT_MAP[ext]) {
      showToast('Unsupported file format. Please upload PDF, DOC, DOCX, PPT, PPTX, XLS, XLSX, or TXT.', 'error');
      clearSelectedFile();
      return;
    }

    state.selectedUploadFile = file;

    // Show Detected File Info Card
    if (detectedFileCard) {
      detectedFileCard.style.display = 'flex';
      detectedFileName.textContent = file.name;
      const formattedSize = formatFileSize(file.size);
      const formatDesc = FORMAT_MAP[ext];
      detectedFileDetails.textContent = `${formatDesc} • ${formattedSize}`;
      detectedFileIcon.textContent = ext.toUpperCase();
    }
    if (fileDropzone) {
      fileDropzone.style.display = 'none';
    }
  }

  function clearSelectedFile() {
    state.selectedUploadFile = null;
    if (fileInput) fileInput.value = '';
    if (detectedFileCard) detectedFileCard.style.display = 'none';
    if (fileDropzone) fileDropzone.style.display = 'block';
  }

  function handleReplaceFileSelected(file) {
    const ext = getFileExtension(file.name).toLowerCase();
    if (!FORMAT_MAP[ext]) {
      showToast('Unsupported replacement format. Allowed: PDF, DOC, DOCX, PPT, PPTX, XLS, XLSX, TXT.', 'error');
      return;
    }

    state.selectedReplaceFile = file;
    if (replaceDetectedCard) {
      replaceDetectedCard.style.display = 'block';
      replaceDetectedFileName.textContent = file.name;
      replaceDetectedDetails.textContent = `${FORMAT_MAP[ext]} • ${formatFileSize(file.size)}`;
    }
  }

  /**
   * Upload Material Form Submit
   */
  async function handleUploadSubmit(e) {
    e.preventDefault();

    if (!state.selectedUploadFile) {
      showToast('Please select an academic document to upload.', 'error');
      return;
    }

    const title = document.getElementById('uploadTitle').value.trim();
    const description = document.getElementById('uploadDescription').value.trim();
    const academicYear = document.getElementById('uploadYear').value;
    const semester = document.getElementById('uploadSemester').value;
    const subject = document.getElementById('uploadSubject').value.trim();
    const category = document.getElementById('uploadCategory').value;
    const published = document.getElementById('uploadPublished').checked;

    if (!title || !academicYear || !semester || !subject || !category) {
      showToast('Please fill in all required fields.', 'error');
      return;
    }

    const formData = new FormData();
    formData.append('title', title);
    formData.append('description', description);
    formData.append('academicYear', academicYear);
    formData.append('semester', semester);
    formData.append('subject', subject);
    formData.append('category', category);
    formData.append('published', published);
    formData.append('file', state.selectedUploadFile);

    setUploadLoading(true);

    try {
      const res = await authFetch('/api/admin/materials/upload', {
        method: 'POST',
        body: formData
      });

      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.message || data.error || 'Upload failed');
      }

      showToast(`Material "${data.title}" uploaded successfully!`, 'success');

      // Reset Form
      uploadForm.reset();
      clearSelectedFile();
      document.getElementById('uploadPublished').checked = true;

      await refreshStats();
      await loadMaterials();
    } catch (err) {
      showToast(err.message || 'Error uploading file', 'error');
    } finally {
      setUploadLoading(false);
    }
  }

  function setUploadLoading(loading) {
    if (uploadSubmitBtn) uploadSubmitBtn.disabled = loading;
    if (uploadSpinner) uploadSpinner.style.display = loading ? 'block' : 'none';
    if (uploadBtnText) uploadBtnText.textContent = loading ? 'Uploading & Processing File...' : 'Upload Material';
  }

  /**
   * Refresh Stats Cards
   */
  async function refreshStats() {
    try {
      const res = await authFetch('/api/admin/stats');
      if (!res.ok) return;
      const stats = await res.json();
      state.stats = stats;

      if (statTotal) statTotal.textContent = stats.totalMaterials;
      if (statPublished) statPublished.textContent = stats.totalPublished;
      if (statDraft) statDraft.textContent = stats.totalUnpublished;
      if (statNotes) statNotes.textContent = stats.totalNotes;
      if (statLab) statLab.textContent = stats.totalLabManuals;
      if (statAssign) statAssign.textContent = stats.totalAssignments;
      if (statPapers) statPapers.textContent = stats.totalQuestionPapers;
    } catch (err) {
      console.warn('Could not refresh stats:', err);
    }
  }

  /**
   * Load All Materials for Manage Table
   */
  async function loadMaterials() {
    try {
      const res = await authFetch('/api/admin/materials?publishedOnly=false');
      if (!res.ok) return;
      const data = await res.json();
      state.materials = data;
      renderMaterialsTable();
    } catch (err) {
      console.error('Failed to load materials table:', err);
    }
  }

  /**
   * Render Materials Table with Search and Filtering
   */
  function renderMaterialsTable() {
    if (!materialsTableBody) return;

    let filtered = state.materials.slice();

    // Search filter
    if (state.filterSearch) {
      const q = state.filterSearch;
      filtered = filtered.filter(m => 
        (m.title && m.title.toLowerCase().includes(q)) ||
        (m.subject && m.subject.toLowerCase().includes(q)) ||
        (m.description && m.description.toLowerCase().includes(q)) ||
        (m.originalFilename && m.originalFilename.toLowerCase().includes(q))
      );
    }

    // Category filter
    if (state.filterCategory && state.filterCategory !== 'ALL') {
      filtered = filtered.filter(m => m.category === state.filterCategory);
    }

    // Subject filter
    if (state.filterSubject) {
      filtered = filtered.filter(m => m.subject && m.subject.toLowerCase().includes(state.filterSubject));
    }

    // Status filter
    if (state.filterStatus === 'PUBLISHED') {
      filtered = filtered.filter(m => m.published === true);
    } else if (state.filterStatus === 'DRAFT') {
      filtered = filtered.filter(m => m.published === false);
    }

    if (tableCountBadge) {
      tableCountBadge.textContent = `${filtered.length} Materials`;
    }

    if (filtered.length === 0) {
      materialsTableBody.innerHTML = `
        <tr>
          <td colspan="6" style="text-align: center; padding: 48px 20px; color: #9CA3AF;">
            No materials found matching your filters.
          </td>
        </tr>
      `;
      return;
    }

    materialsTableBody.innerHTML = filtered.map(mat => {
      const isPub = mat.published;
      const statusPillClass = isPub ? 'status-published' : 'status-draft';
      const statusText = isPub ? 'Published' : 'Draft / Hidden';
      const togglePublishTitle = isPub ? 'Click to Unpublish' : 'Click to Publish';

      return `
        <tr data-id="${mat.id}">
          <td>
            <span class="table-material-title">${escapeHtml(mat.title)}</span>
            <span class="table-material-sub">${escapeHtml(mat.originalFilename)}</span>
          </td>
          <td>
            <span class="badge-category ${getCategoryClass(mat.category)}" style="font-size: 0.72rem;">
              ${escapeHtml(mat.categoryDisplayName || mat.category)}
            </span>
          </td>
          <td>
            <div style="display:flex; flex-direction:column;">
              <span style="color: #38BDF8; font-weight: 500;">${escapeHtml(mat.subject)}</span>
              <span style="font-size: 0.75rem; color: #9CA3AF;">${escapeHtml(mat.academicYear)} • ${escapeHtml(mat.semester)}</span>
            </div>
          </td>
          <td>
            <div style="display:flex; flex-direction:column;">
              <span style="font-weight: 600; text-transform: uppercase;">.${escapeHtml(mat.fileExtension)}</span>
              <span style="font-size: 0.75rem; color: #9CA3AF;">${escapeHtml(mat.formattedFileSize)}</span>
            </div>
          </td>
          <td>
            <button class="status-pill ${statusPillClass} btn-toggle-publish" data-id="${mat.id}" data-published="${isPub}" title="${togglePublishTitle}" style="cursor: pointer; border: none; font-family: inherit;">
              ${statusText}
            </button>
          </td>
          <td>
            <div class="action-buttons">
              <!-- View / Download -->
              <a href="/api/admin/materials/${mat.id}/view" target="_blank" class="action-btn" title="View / Preview File">
                <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                </svg>
              </a>
              <a href="/api/admin/materials/${mat.id}/download" download="${escapeHtml(mat.originalFilename)}" class="action-btn" title="Download File">
                <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
                </svg>
              </a>
              <!-- Edit Details -->
              <button class="action-btn btn-edit" data-id="${mat.id}" title="Edit Metadata">
                <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                </svg>
              </button>
              <!-- Replace File -->
              <button class="action-btn btn-replace" data-id="${mat.id}" title="Replace Uploaded File">
                <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path>
                </svg>
              </button>
              <!-- Delete -->
              <button class="action-btn btn-delete" data-id="${mat.id}" title="Delete Material">
                <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                </svg>
              </button>
            </div>
          </td>
        </tr>
      `;
    }).join('');

    // Attach Action Handlers
    materialsTableBody.querySelectorAll('.btn-toggle-publish').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.dataset.id;
        const currentPub = btn.dataset.published === 'true';
        togglePublishStatus(id, !currentPub);
      });
    });

    materialsTableBody.querySelectorAll('.btn-edit').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.dataset.id;
        openEditModal(id);
      });
    });

    materialsTableBody.querySelectorAll('.btn-replace').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.dataset.id;
        openReplaceModal(id);
      });
    });

    materialsTableBody.querySelectorAll('.btn-delete').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.dataset.id;
        openDeleteModal(id);
      });
    });
  }

  /**
   * Toggle Publish Status
   */
  async function togglePublishStatus(id, newStatus) {
    try {
      const res = await authFetch(`/api/admin/materials/${id}/publish?published=${newStatus}`, {
        method: 'PATCH'
      });
      if (!res.ok) throw new Error('Failed to update publication status');
      const updated = await res.json();
      showToast(`Material "${updated.title}" is now ${newStatus ? 'Published' : 'Unpublished (Draft)'}`, 'success');
      await refreshStats();
      await loadMaterials();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  /**
   * Edit Material Modal
   */
  function openEditModal(id) {
    const mat = state.materials.find(m => m.id == id);
    if (!mat) return;

    state.editingMaterialId = id;
    document.getElementById('editTitle').value = mat.title || '';
    document.getElementById('editDescription').value = mat.description || '';
    document.getElementById('editYear').value = mat.academicYear || '3rd Year';
    document.getElementById('editSemester').value = mat.semester || 'Semester 5';
    document.getElementById('editSubject').value = mat.subject || '';
    document.getElementById('editCategory').value = mat.category || 'NOTES';
    document.getElementById('editPublished').checked = mat.published;

    editModal.classList.add('active');
  }

  async function handleEditSubmit(e) {
    e.preventDefault();
    if (!state.editingMaterialId) return;

    const payload = {
      title: document.getElementById('editTitle').value.trim(),
      description: document.getElementById('editDescription').value.trim(),
      academicYear: document.getElementById('editYear').value,
      semester: document.getElementById('editSemester').value,
      subject: document.getElementById('editSubject').value.trim(),
      category: document.getElementById('editCategory').value,
      published: document.getElementById('editPublished').checked
    };

    try {
      const res = await authFetch(`/api/admin/materials/${state.editingMaterialId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      if (!res.ok) throw new Error('Failed to update material');

      showToast('Material details updated successfully!', 'success');
      closeAllModals();
      await refreshStats();
      await loadMaterials();
    } catch (err) {
      showToast(err.message, 'error');
    }
  }

  /**
   * Replace File Modal
   */
  function openReplaceModal(id) {
    const mat = state.materials.find(m => m.id == id);
    if (!mat) return;

    state.replacingMaterialId = id;
    state.selectedReplaceFile = null;
    if (replaceFileInput) replaceFileInput.value = '';
    if (replaceDetectedCard) replaceDetectedCard.style.display = 'none';

    document.getElementById('replaceMaterialTitle').textContent = mat.title;
    document.getElementById('replaceCurrentFilename').textContent = mat.originalFilename;

    replaceModal.classList.add('active');
  }

  async function handleReplaceSubmit() {
    if (!state.replacingMaterialId || !state.selectedReplaceFile) {
      showToast('Please choose a replacement file.', 'error');
      return;
    }

    const formData = new FormData();
    formData.append('file', state.selectedReplaceFile);

    submitReplaceBtn.disabled = true;
    submitReplaceBtn.textContent = 'Replacing File...';

    try {
      const res = await authFetch(`/api/admin/materials/${state.replacingMaterialId}/replace-file`, {
        method: 'POST',
        body: formData
      });
      if (!res.ok) throw new Error('Failed to replace file');

      showToast('File replaced successfully!', 'success');
      closeAllModals();
      await refreshStats();
      await loadMaterials();
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      submitReplaceBtn.disabled = false;
      submitReplaceBtn.textContent = 'Upload & Replace File';
    }
  }

  /**
   * Delete Modal
   */
  function openDeleteModal(id) {
    const mat = state.materials.find(m => m.id == id);
    if (!mat) return;

    state.deletingMaterialId = id;
    if (deleteMaterialTitle) deleteMaterialTitle.textContent = mat.title;
    deleteModal.classList.add('active');
  }

  async function handleDeleteConfirm() {
    if (!state.deletingMaterialId) return;

    confirmDeleteBtn.disabled = true;
    confirmDeleteBtn.textContent = 'Deleting...';

    try {
      const res = await authFetch(`/api/admin/materials/${state.deletingMaterialId}`, {
        method: 'DELETE'
      });
      if (!res.ok) throw new Error('Failed to delete material');

      showToast('Material permanently deleted.', 'info');
      closeAllModals();
      await refreshStats();
      await loadMaterials();
    } catch (err) {
      showToast(err.message, 'error');
    } finally {
      confirmDeleteBtn.disabled = false;
      confirmDeleteBtn.textContent = 'Yes, Delete Permanently';
    }
  }

  function closeAllModals() {
    state.editingMaterialId = null;
    state.replacingMaterialId = null;
    state.deletingMaterialId = null;
    state.selectedReplaceFile = null;

    if (editModal) editModal.classList.remove('active');
    if (replaceModal) replaceModal.classList.remove('active');
    if (deleteModal) deleteModal.classList.remove('active');
  }

  /**
   * Realtime Sync SSE Connection
   */
  function connectRealtimeSync() {
    if (window.EventSource) {
      try {
        const es = new EventSource('/api/materials/sync');
        es.addEventListener('CONNECTED', () => {
          console.log('[Admin SSE] Live stream connected');
        });

        const syncEvents = ['MATERIAL_PUBLISHED', 'MATERIAL_UNPUBLISHED', 'MATERIAL_UPDATED', 'MATERIAL_DELETED', 'MATERIAL_CREATED'];
        syncEvents.forEach(evt => {
          es.addEventListener(evt, () => {
            refreshStats();
            loadMaterials();
          });
        });
      } catch (err) {
        console.warn('Admin SSE error:', err);
      }
    }
  }

  /**
   * Toast Helper
   */
  function showToast(message, type = 'info') {
    if (!toastContainer) return;
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<span>${escapeHtml(message)}</span>`;
    toastContainer.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transition = 'opacity 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 4000);
  }

  /**
   * Utilities
   */
  function getFileExtension(filename) {
    if (!filename) return '';
    const lastDot = filename.lastIndexOf('.');
    return lastDot >= 0 ? filename.substring(lastDot + 1) : '';
  }

  function formatFileSize(bytes) {
    if (!bytes || bytes <= 0) return '0 B';
    const units = ['B', 'KB', 'MB', 'GB'];
    const i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));
    return Math.round(bytes / Math.pow(1024, i), 2) + ' ' + units[i];
  }

  function getCategoryClass(cat) {
    switch (cat) {
      case 'NOTES': return 'badge-notes';
      case 'LAB_MANUAL': return 'badge-lab';
      case 'ASSIGNMENT': return 'badge-assignment';
      case 'PREVIOUS_QUESTION_PAPER': return 'badge-paper';
      default: return 'badge-notes';
    }
  }

  function escapeHtml(str) {
    if (!str) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  // Launch on ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();

/**
 * CampusBrain – Smart Campus Management Platform for CSE
 * Student Portal Logic & Real-time Live Sync
 */

(function () {
  'use strict';

  // Application State
  const state = {
    search: '',
    category: 'ALL',
    academicYear: 'ALL',
    semester: 'ALL',
    subject: '',
    materials: [],
    loading: false
  };

  // DOM Elements
  const searchInput = document.getElementById('searchInput');
  const searchClearBtn = document.getElementById('searchClearBtn');
  const yearSelect = document.getElementById('yearSelect');
  const semesterSelect = document.getElementById('semesterSelect');
  const subjectInput = document.getElementById('subjectInput');
  const categorySelect = document.getElementById('categorySelect');
  const resetFiltersBtn = document.getElementById('resetFiltersBtn');
  const activeChipsContainer = document.getElementById('activeChipsContainer');
  const resourcesGrid = document.getElementById('resourcesGrid');
  const materialsCountBadge = document.getElementById('materialsCountBadge');
  const emptyState = document.getElementById('emptyState');
  const liveSyncPill = document.getElementById('liveSyncPill');
  const liveSyncText = document.getElementById('liveSyncText');

  // Category Cards
  const categoryCards = document.querySelectorAll('.category-card');

  // Preview Modal Elements
  const previewModal = document.getElementById('previewModal');
  const modalCloseBtn = document.getElementById('modalCloseBtn');
  const modalCloseFooterBtn = document.getElementById('modalCloseFooterBtn');
  const modalTitle = document.getElementById('modalTitle');
  const modalCategoryBadge = document.getElementById('modalCategoryBadge');
  const modalBody = document.getElementById('modalBody');
  const modalDownloadBtn = document.getElementById('modalDownloadBtn');
  const modalMetaInfo = document.getElementById('modalMetaInfo');

  // Toast Container
  const toastContainer = document.getElementById('toastContainer');

  let searchDebounceTimer = null;
  let eventSource = null;

  /**
   * Initialize Application
   */
  async function init() {
    setupEventListeners();
    await fetchCategoryCounts();
    await loadMaterials();
    connectRealtimeSync();
  }

  /**
   * Event Listeners Registration
   */
  function setupEventListeners() {
    // Search input with debounce
    if (searchInput) {
      searchInput.addEventListener('input', (e) => {
        const val = e.target.value.trim();
        state.search = val;
        if (searchClearBtn) {
          searchClearBtn.style.display = val ? 'flex' : 'none';
        }
        clearTimeout(searchDebounceTimer);
        searchDebounceTimer = setTimeout(() => {
          loadMaterials();
          renderActiveChips();
        }, 280);
      });
    }

    // Search clear button
    if (searchClearBtn) {
      searchClearBtn.addEventListener('click', () => {
        searchInput.value = '';
        state.search = '';
        searchClearBtn.style.display = 'none';
        loadMaterials();
        renderActiveChips();
      });
    }

    // Category Card clicks
    categoryCards.forEach(card => {
      card.addEventListener('click', (e) => {
        e.preventDefault();
        const selectedCat = card.dataset.category;
        if (state.category === selectedCat) {
          state.category = 'ALL';
        } else {
          state.category = selectedCat;
        }
        if (categorySelect) categorySelect.value = state.category;
        updateCategoryCardHighlights();
        loadMaterials();
        renderActiveChips();
      });
    });

    // Dropdown filters
    if (yearSelect) {
      yearSelect.addEventListener('change', (e) => {
        state.academicYear = e.target.value;
        loadMaterials();
        renderActiveChips();
      });
    }

    if (semesterSelect) {
      semesterSelect.addEventListener('change', (e) => {
        state.semester = e.target.value;
        loadMaterials();
        renderActiveChips();
      });
    }

    if (subjectInput) {
      let subjectDebounceTimer = null;
      subjectInput.addEventListener('input', (e) => {
        state.subject = e.target.value.trim();
        clearTimeout(subjectDebounceTimer);
        subjectDebounceTimer = setTimeout(() => {
          loadMaterials();
          renderActiveChips();
        }, 280);
      });
    }

    if (categorySelect) {
      categorySelect.addEventListener('change', (e) => {
        state.category = e.target.value;
        updateCategoryCardHighlights();
        loadMaterials();
        renderActiveChips();
      });
    }

    // Reset Filters button
    if (resetFiltersBtn) {
      resetFiltersBtn.addEventListener('click', () => {
        resetAllFilters();
      });
    }

    // Modal controls
    if (modalCloseBtn) modalCloseBtn.addEventListener('click', closeModal);
    if (modalCloseFooterBtn) modalCloseFooterBtn.addEventListener('click', closeModal);
    if (previewModal) {
      previewModal.addEventListener('click', (e) => {
        if (e.target === previewModal) closeModal();
      });
    }

    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape' && previewModal && previewModal.classList.contains('active')) {
        closeModal();
      }
    });
  }

  /**
   * Reset All Filters
   */
  function resetAllFilters() {
    state.search = '';
    state.category = 'ALL';
    state.academicYear = 'ALL';
    state.semester = 'ALL';
    state.subject = '';

    if (searchInput) searchInput.value = '';
    if (searchClearBtn) searchClearBtn.style.display = 'none';
    if (yearSelect) yearSelect.value = 'ALL';
    if (semesterSelect) semesterSelect.value = 'ALL';
    if (subjectInput) subjectInput.value = '';
    if (categorySelect) categorySelect.value = 'ALL';

    updateCategoryCardHighlights();
    loadMaterials();
    renderActiveChips();
  }

  /**
   * Update Active Visual Highlights on Category Cards
   */
  function updateCategoryCardHighlights() {
    categoryCards.forEach(card => {
      if (state.category !== 'ALL' && card.dataset.category === state.category) {
        card.classList.add('active');
      } else {
        card.classList.remove('active');
      }
    });
    if (categorySelect) categorySelect.value = state.category;
  }

  /**
   * Fetch Filter Options from Backend
   */
  async function fetchFilterOptions() {
    // Subject is typed directly by students - no predefined recommendation list
  }

  /**
   * Fetch Category Counts from Backend
   */
  async function fetchCategoryCounts() {
    try {
      const res = await fetch('/api/materials/categories');
      if (!res.ok) return;
      const counts = await res.json();

      // Update counters on category cards
      const countNotes = document.getElementById('countNotes');
      const countLab = document.getElementById('countLabManuals');
      const countAssign = document.getElementById('countAssignments');
      const countPapers = document.getElementById('countQuestionPapers');

      if (countNotes) countNotes.textContent = counts.NOTES != null ? counts.NOTES : '0';
      if (countLab) countLab.textContent = counts.LAB_MANUAL != null ? counts.LAB_MANUAL : '0';
      if (countAssign) countAssign.textContent = counts.ASSIGNMENT != null ? counts.ASSIGNMENT : '0';
      if (countPapers) countPapers.textContent = counts.PREVIOUS_QUESTION_PAPER != null ? counts.PREVIOUS_QUESTION_PAPER : '0';
    } catch (err) {
      console.warn('Could not fetch category counts:', err);
    }
  }

  /**
   * Fetch Academic Materials
   */
  async function loadMaterials() {
    state.loading = true;
    renderLoadingSkeletons();

    try {
      const params = new URLSearchParams();
      if (state.search) params.append('search', state.search);
      if (state.category && state.category !== 'ALL') params.append('category', state.category);
      if (state.academicYear && state.academicYear !== 'ALL') params.append('academicYear', state.academicYear);
      if (state.semester && state.semester !== 'ALL') params.append('semester', state.semester);
      if (state.subject && state.subject !== 'ALL' && state.subject.trim()) params.append('subject', state.subject.trim());

      const url = '/api/materials' + (params.toString() ? '?' + params.toString() : '');
      const res = await fetch(url);
      if (!res.ok) throw new Error('Failed to load materials');

      const data = await res.json();
      state.materials = data;
      renderMaterials(data);
    } catch (err) {
      console.error('Error loading materials:', err);
      resourcesGrid.innerHTML = `
        <div class="empty-state">
          <p class="empty-title">Unable to load resources</p>
          <p class="empty-desc">${escapeHtml(err.message || 'Please check connection.')}</p>
          <button class="btn btn-secondary" onclick="window.CampusBrainStudent.reload()">Try Again</button>
        </div>
      `;
    } finally {
      state.loading = false;
    }
  }

  /**
   * Render Loading Skeleton Cards
   */
  function renderLoadingSkeletons() {
    if (!resourcesGrid) return;
    resourcesGrid.innerHTML = Array.from({ length: 6 }).map(() => `
      <div class="skeleton-card">
        <div style="display:flex; justify-content:space-between;">
          <div class="skeleton-line" style="width: 25%;"></div>
          <div class="skeleton-line" style="width: 15%;"></div>
        </div>
        <div class="skeleton-line" style="width: 85%; height: 20px;"></div>
        <div class="skeleton-line" style="width: 60%;"></div>
        <div class="skeleton-line" style="width: 100%; height: 40px;"></div>
        <div style="display:flex; justify-content:space-between; margin-top: auto; padding-top: 14px;">
          <div class="skeleton-line" style="width: 30%;"></div>
          <div class="skeleton-line" style="width: 40%;"></div>
        </div>
      </div>
    `).join('');
  }

  /**
   * Render Materials List
   */
  function renderMaterials(materials) {
    if (materialsCountBadge) {
      materialsCountBadge.textContent = `${materials.length} ${materials.length === 1 ? 'Resource' : 'Resources'}`;
    }

    if (!materials || materials.length === 0) {
      const isFiltered = state.search || (state.category && state.category !== 'ALL') || 
                         (state.academicYear && state.academicYear !== 'ALL') || 
                         (state.semester && state.semester !== 'ALL') || 
                         (state.subject && state.subject !== 'ALL');

      resourcesGrid.innerHTML = `
        <div class="empty-state">
          <svg class="empty-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
          </svg>
          <p class="empty-title">${isFiltered ? 'No Matching Resources Found' : 'No Academic Resources Available Yet'}</p>
          <p class="empty-desc">${isFiltered ? 'No published materials matched your current search and filter criteria.' : 'No study materials have been uploaded to the repository yet. Search above to check for required files, or check back soon.'}</p>
          ${isFiltered ? '<button class="btn btn-secondary" id="emptyClearBtn">Clear All Filters</button>' : ''}
        </div>
      `;

      const emptyClearBtn = document.getElementById('emptyClearBtn');
      if (emptyClearBtn) {
        emptyClearBtn.addEventListener('click', resetAllFilters);
      }
      return;
    }

    resourcesGrid.innerHTML = materials.map(mat => {
      const catClass = getCategoryBadgeClass(mat.category);

      return `
        <div class="resource-card" data-id="${mat.id}">
          <div class="card-top">
            <span class="badge-category ${catClass}">
              ${escapeHtml(mat.categoryDisplayName || mat.category)}
            </span>
          </div>

          <h3 class="card-title" title="${escapeHtml(mat.title)}">
            ${escapeHtml(mat.title)}
          </h3>

          <div class="card-meta-chips">
            <span class="meta-chip subject">
              <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"></path>
              </svg>
              ${escapeHtml(mat.subject)}
            </span>
            <span class="meta-chip category">
              <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"></path>
              </svg>
              ${escapeHtml(mat.categoryDisplayName || mat.category)}
            </span>
            <span class="meta-chip">
              ${escapeHtml(mat.academicYear)}
            </span>
            <span class="meta-chip">
              ${escapeHtml(mat.semester)}
            </span>
          </div>

          <p class="card-description">
            ${escapeHtml(mat.description || 'No description provided for this academic resource.')}
          </p>

          <div class="card-footer">
            <div class="card-info">
              <span class="card-size">${escapeHtml(mat.formattedFileSize || '')}</span>
              <span class="card-date">${escapeHtml(mat.createdAt || '')}</span>
            </div>
            <div class="card-actions">
              <button class="btn btn-secondary btn-view" data-id="${mat.id}">
                <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                </svg>
                View
              </button>
              <a href="${mat.downloadUrl}" class="btn btn-primary" download="${escapeHtml(mat.originalFilename)}">
                <svg width="15" height="15" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
                </svg>
                Download
              </a>
            </div>
          </div>
        </div>
      `;
    }).join('');

    // Attach click handlers to View buttons
    resourcesGrid.querySelectorAll('.btn-view').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.dataset.id;
        const mat = state.materials.find(m => m.id == id);
        if (mat) openPreviewModal(mat);
      });
    });
  }

  /**
   * Active Chips Filter UI Rendering
   */
  function renderActiveChips() {
    if (!activeChipsContainer) return;
    const chips = [];

    if (state.search) {
      chips.push({ label: `Search: "${state.search}"`, key: 'search' });
    }
    if (state.category && state.category !== 'ALL') {
      const catNames = {
        NOTES: 'Notes',
        LAB_MANUAL: 'Lab Manuals',
        ASSIGNMENT: 'Assignments',
        PREVIOUS_QUESTION_PAPER: 'Question Papers'
      };
      chips.push({ label: `Category: ${catNames[state.category] || state.category}`, key: 'category' });
    }
    if (state.academicYear && state.academicYear !== 'ALL') {
      chips.push({ label: `Year: ${state.academicYear}`, key: 'academicYear' });
    }
    if (state.semester && state.semester !== 'ALL') {
      chips.push({ label: `Semester: ${state.semester}`, key: 'semester' });
    }
    if (state.subject && state.subject !== 'ALL' && state.subject.trim()) {
      chips.push({ label: `Subject: "${state.subject.trim()}"`, key: 'subject' });
    }

    if (chips.length === 0) {
      activeChipsContainer.innerHTML = '';
      return;
    }

    activeChipsContainer.innerHTML = chips.map(chip => `
      <div class="chip">
        <span>${escapeHtml(chip.label)}</span>
        <span class="chip-remove" data-key="${chip.key}" title="Remove filter">
          <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </span>
      </div>
    `).join('');

    // Attach remove handlers
    activeChipsContainer.querySelectorAll('.chip-remove').forEach(rm => {
      rm.addEventListener('click', () => {
        const key = rm.dataset.key;
        if (key === 'search') {
          state.search = '';
          if (searchInput) searchInput.value = '';
          if (searchClearBtn) searchClearBtn.style.display = 'none';
        } else if (key === 'category') {
          state.category = 'ALL';
          if (categorySelect) categorySelect.value = 'ALL';
          updateCategoryCardHighlights();
        } else if (key === 'academicYear') {
          state.academicYear = 'ALL';
          if (yearSelect) yearSelect.value = 'ALL';
        } else if (key === 'semester') {
          state.semester = 'ALL';
          if (semesterSelect) semesterSelect.value = 'ALL';
        } else if (key === 'subject') {
          state.subject = '';
          if (subjectInput) subjectInput.value = '';
        }

        loadMaterials();
        renderActiveChips();
      });
    });
  }

  /**
   * In-App Document Preview Modal
   */
  function openPreviewModal(mat) {
    if (!previewModal) return;

    modalTitle.textContent = mat.title;
    modalCategoryBadge.textContent = mat.categoryDisplayName || mat.category;
    modalCategoryBadge.className = `badge-category ${getCategoryBadgeClass(mat.category)}`;

    modalDownloadBtn.href = mat.downloadUrl;
    modalDownloadBtn.setAttribute('download', mat.originalFilename);

    modalMetaInfo.innerHTML = `
      <span><strong>Category:</strong> ${escapeHtml(mat.categoryDisplayName || mat.category)}</span> &bull; 
      <span><strong>Subject:</strong> ${escapeHtml(mat.subject)}</span> &bull; 
      <span><strong>Year:</strong> ${escapeHtml(mat.academicYear)}</span> &bull; 
      <span><strong>Semester:</strong> ${escapeHtml(mat.semester)}</span> &bull; 
      <span><strong>Size:</strong> ${escapeHtml(mat.formattedFileSize)}</span>
    `;

    const ext = (mat.fileExtension || (mat.originalFilename ? mat.originalFilename.split('.').pop() : '')).toLowerCase().replace('.', '').trim();

    if (ext === 'pdf') {
      // In-line PDF viewer
      modalBody.innerHTML = `
        <iframe class="preview-viewer-frame" src="${mat.viewUrl}" title="${escapeHtml(mat.title)}"></iframe>
      `;
    } else if (ext === 'docx') {
      // In-line Word Document (.docx) viewer using Mammoth.js
      modalBody.innerHTML = `
        <div class="docx-loading-box">
          <div class="spinner" style="width: 36px; height: 36px; border: 3px solid #E2E8F0; border-top-color: #2563EB; border-radius: 50%; animation: spin 0.8s linear infinite;"></div>
          <span>Rendering Word document pages...</span>
        </div>
      `;
      fetch(mat.viewUrl)
        .then(res => {
          if (!res.ok) throw new Error('Could not load document file');
          return res.arrayBuffer();
        })
        .then(buffer => {
          if (window.mammoth) {
            return window.mammoth.convertToHtml({ arrayBuffer: buffer });
          } else {
            throw new Error('Mammoth renderer not loaded');
          }
        })
        .then(result => {
          modalBody.innerHTML = `
            <div class="docx-rendered-container">
              <div style="border-bottom: 1px solid #E2E8F0; padding-bottom: 12px; margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
                <span style="font-weight: 600; color: #2563EB; font-size: 0.95rem;">📄 Word Document View &bull; ${escapeHtml(mat.originalFilename)}</span>
                <div style="display: flex; gap: 8px;">
                  <a href="${mat.downloadUrl}" class="btn btn-primary" style="font-size: 0.8rem; padding: 6px 12px;" download="${escapeHtml(mat.originalFilename)}">
                    Download .docx
                  </a>
                </div>
              </div>
              <div class="docx-body">
                ${result.value || '<p style="color: #94A3B8;">This document has no readable text content.</p>'}
              </div>
            </div>
          `;
        })
        .catch(err => {
          console.warn('In-browser docx render fallback to Google Docs:', err);
          const absoluteDocUrl = window.location.origin + mat.viewUrl;
          modalBody.innerHTML = `
            <iframe class="preview-viewer-frame" src="https://docs.google.com/viewer?url=${encodeURIComponent(absoluteDocUrl)}&embedded=true" title="${escapeHtml(mat.title)}"></iframe>
          `;
        });
    } else if (['pptx', 'ppt', 'xlsx', 'xls', 'doc'].includes(ext)) {
      // Cloud Document Viewer for Office formats
      const absoluteDocUrl = window.location.origin + mat.viewUrl;
      modalBody.innerHTML = `
        <iframe class="preview-viewer-frame" src="https://docs.google.com/viewer?url=${encodeURIComponent(absoluteDocUrl)}&embedded=true" title="${escapeHtml(mat.title)}"></iframe>
      `;
    } else if (ext === 'txt') {
      // Text viewer
      modalBody.innerHTML = `
        <div style="background-color: #0F172A; border-radius: 8px; padding: 20px; font-family: monospace; white-space: pre-wrap; color: #E2E8F0; max-height: 480px; overflow-y: auto;">
          Loading text preview...
        </div>
      `;
      fetch(mat.viewUrl)
        .then(r => r.text())
        .then(text => {
          modalBody.querySelector('div').textContent = text;
        })
        .catch(err => {
          modalBody.querySelector('div').textContent = 'Could not preview text: ' + err.message;
        });
    } else {
      // Fallback office preview box
      modalBody.innerHTML = `
        <div class="office-preview-box">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
          </svg>
          <h3 style="font-size: 1.2rem; font-weight: 700; margin-bottom: 8px;">${escapeHtml(mat.originalFilename)}</h3>
          <p style="font-size: 0.9rem; color: #9CA3AF; max-width: 500px; margin: 0 auto 20px auto;">
            This document is a <strong>${escapeHtml(mat.fileFormat)}</strong> (${escapeHtml(mat.formattedFileSize)}). You can view it directly by downloading the file to your computer.
          </p>
          <div style="display:flex; justify-content:center; gap: 12px;">
            <a href="${mat.downloadUrl}" class="btn btn-primary" download="${escapeHtml(mat.originalFilename)}">
              <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"></path>
              </svg>
              Download File
            </a>
            <a href="${mat.viewUrl}" target="_blank" class="btn btn-secondary">
              Open in New Window
            </a>
          </div>
        </div>
      `;
    }

    previewModal.classList.add('active');
    document.body.style.overflow = 'hidden';
  }

  function closeModal() {
    if (!previewModal) return;
    previewModal.classList.remove('active');
    document.body.style.overflow = '';
    modalBody.innerHTML = '';
  }

  /**
   * Real-time Server-Sent Events (SSE) Live Sync
   */
  function connectRealtimeSync() {
    if (window.EventSource) {
      try {
        eventSource = new EventSource('/api/materials/sync');

        eventSource.addEventListener('CONNECTED', (e) => {
          console.log('[CampusBrain SSE] Connected to live sync stream');
          setLiveStatus(true);
        });

        // Whenever admin publishes material
        eventSource.addEventListener('MATERIAL_PUBLISHED', (e) => {
          const event = JSON.parse(e.data);
          showToast(`New resource published: ${event.message}`, 'success');
          fetchCategoryCounts();
          loadMaterials();
        });

        // Whenever admin unpublishes material
        eventSource.addEventListener('MATERIAL_UNPUBLISHED', (e) => {
          const event = JSON.parse(e.data);
          showToast(`Resource updated: ${event.message}`, 'info');
          fetchCategoryCounts();
          loadMaterials();
        });

        // Whenever admin updates material details or replaces file
        eventSource.addEventListener('MATERIAL_UPDATED', (e) => {
          const event = JSON.parse(e.data);
          showToast(`Resource updated: ${event.message}`, 'info');
          fetchCategoryCounts();
          loadMaterials();
        });

        // Whenever admin deletes material
        eventSource.addEventListener('MATERIAL_DELETED', (e) => {
          const event = JSON.parse(e.data);
          showToast(`Resource removed: ${event.message}`, 'info');
          fetchCategoryCounts();
          loadMaterials();
        });

        eventSource.onerror = (err) => {
          console.warn('[CampusBrain SSE] Connection lost, retrying in 5s...', err);
          setLiveStatus(false);
          eventSource.close();
          setTimeout(connectRealtimeSync, 5000);
        };
      } catch (e) {
        console.warn('SSE not initialized:', e);
      }
    }
  }

  function setLiveStatus(connected) {
    if (liveSyncPill && liveSyncText) {
      if (connected) {
        liveSyncPill.style.display = 'flex';
        liveSyncText.textContent = 'Live Sync Active';
      } else {
        liveSyncText.textContent = 'Reconnecting...';
      }
    }
  }

  /**
   * Non-intrusive Toast Notification
   */
  function showToast(message, type = 'info') {
    if (!toastContainer) return;
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `
      <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
      </svg>
      <span>${escapeHtml(message)}</span>
    `;
    toastContainer.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateY(10px)';
      toast.style.transition = 'all 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 4500);
  }

  /**
   * Helper Functions
   */
  function getCategoryBadgeClass(category) {
    switch (category) {
      case 'NOTES': return 'badge-notes';
      case 'LAB_MANUAL': return 'badge-lab';
      case 'ASSIGNMENT': return 'badge-assignment';
      case 'PREVIOUS_QUESTION_PAPER': return 'badge-paper';
      default: return 'badge-notes';
    }
  }

  function getFormatBadgeClass(ext) {
    const clean = (ext || '').toLowerCase();
    switch (clean) {
      case 'pdf': return 'format-pdf';
      case 'doc':
      case 'docx': return 'format-docx';
      case 'ppt':
      case 'pptx': return 'format-pptx';
      case 'xls':
      case 'xlsx': return 'format-xlsx';
      case 'txt': return 'format-txt';
      default: return 'format-pdf';
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

  // Expose reload helper
  window.CampusBrainStudent = {
    reload: loadMaterials,
    reset: resetAllFilters
  };

  // Launch on DOM ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();

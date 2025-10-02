
document.addEventListener('DOMContentLoaded', function () {
  const fileInput = document.getElementById('imageFile');
  const detectBtn = document.getElementById('detectBtn');
  const contentEl = document.getElementById('noteContent');

  
  const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
  const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
  const csrfToken = csrfTokenMeta ? csrfTokenMeta.getAttribute('content') : null;
  const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute('content') : 'X-CSRF-TOKEN';

  detectBtn && detectBtn.addEventListener('click', async function () {
    if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
      alert('Please choose an image first');
      return;
    }
    const file = fileInput.files[0];
    const fd = new FormData();
    fd.append('file', file);

    detectBtn.disabled = true;
    const prevText = detectBtn.textContent;
    detectBtn.textContent = 'Detecting...';

    try {
      const headers = {};
      if (csrfToken) headers[csrfHeader] = csrfToken;

      const resp = await fetch('/notes/detect', {
        method: 'POST',
        body: fd,
        headers,
        credentials: 'same-origin'
      });

      if (!resp.ok) throw new Error('Server returned ' + resp.status);

      const detectedText = await resp.text();
      if (contentEl) {
        // append detected text to existing note content (preserve user input)
        const existing = contentEl.value ? contentEl.value + '\n' : '';
        contentEl.value = existing + detectedText;
      }
    } catch (err) {
      console.error(err);
      alert('Text detection failed. See console for details.');
    } finally {
      detectBtn.disabled = false;
      detectBtn.textContent = prevText;
    }
  });

  
  function parseTagsParam() {
    const params = new URLSearchParams(window.location.search);
    const raw = params.get('tags') || '';
    return raw.split(',').map(s => s.trim()).filter(Boolean);
  }
  function updateTagsParam(tags) {
    const params = new URLSearchParams(window.location.search);
    if (tags.length === 0) {
      params.delete('tags');
    } else {
      params.set('tags', tags.join(','));
    }
    
    window.location.search = params.toString() ? ('?' + params.toString()) : '';
  }

  document.querySelectorAll('.tag-btn').forEach(btn => {
    btn.addEventListener('click', function (e) {
      e.preventDefault();
      const tag = this.getAttribute('data-tag');
      if (!tag) return;
      const current = parseTagsParam();
      const idx = current.indexOf(tag);
      if (idx === -1) current.push(tag); else current.splice(idx, 1);
      updateTagsParam(current);
    });
  });
});
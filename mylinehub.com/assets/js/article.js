(function(){
  const root = document.querySelector('[data-mh-article]');
  if(!root) return;

  const slug = root.getAttribute('data-slug') || '';
  const canonical = root.getAttribute('data-canonical') || '';
  const base = root.getAttribute('data-base') || '/';

  // -------------------------------
  // activity logger (fire-and-forget)
  // -------------------------------
  function logEvent(event, data){
    try{
      fetch(base + 'articles/activity-submit.php', {
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body:JSON.stringify({
          event,
          path: location.pathname + location.search,
          slug,
          data: data || {}
        })
      }).catch(()=>{});
    }catch(e){}
  }

  logEvent('page_view', {ref: document.referrer || ''});

  // -------------------------------
  // progress bar + scroll depth
  // -------------------------------
  const bar = document.getElementById('mhProgress');
  const firedScroll = new Set();

  function updateProgress(){
    if(!bar) return;

    const docH = document.documentElement.scrollHeight;
    const winH = window.innerHeight;
    const max = docH - winH;
    const sc = Math.max(0, Math.min(window.scrollY, max));
    const pct = max > 0 ? (sc / max) * 100 : 0;

    bar.style.width = pct + '%';

    [
      {p:25,e:'scroll_25'},
      {p:50,e:'scroll_50'},
      {p:75,e:'scroll_75'},
      {p:90,e:'scroll_90'},
    ].forEach(m=>{
      if(pct >= m.p && !firedScroll.has(m.e)){
        firedScroll.add(m.e);
        logEvent(m.e, {pct: Math.round(pct)});
      }
    });
  }

  window.addEventListener('scroll', updateProgress, {passive:true});
  window.addEventListener('resize', updateProgress);
  updateProgress();

  setTimeout(()=>logEvent('time_10s'), 10000);
  setTimeout(()=>logEvent('time_30s'), 30000);
  setTimeout(()=>logEvent('time_60s'), 60000);

  // -------------------------------
  // TOC active + toc clicks
  // -------------------------------
  const toc = document.getElementById('mhToc');
  const tocLinks = toc ? Array.from(toc.querySelectorAll('a[data-id]')) : [];
  const headings = tocLinks.map(a => document.getElementById(a.dataset.id)).filter(Boolean);

  function setActive(id){
    tocLinks.forEach(a => a.classList.toggle('active', a.dataset.id === id));
  }

  if(headings.length){
    const obs = new IntersectionObserver((entries)=>{
      const visible = entries
        .filter(e => e.isIntersecting)
        .sort((a,b)=> (b.intersectionRatio || 0) - (a.intersectionRatio || 0))[0];

      if(visible?.target?.id) setActive(visible.target.id);
    }, { rootMargin:"-30% 0px -60% 0px", threshold:[0.1,0.2,0.5] });

    headings.forEach(h => obs.observe(h));
  }

  tocLinks.forEach(a=>{
    a.addEventListener('click', ()=>{
      logEvent('toc_click', {id: a.dataset.id || ''});
    });
  });

  // -------------------------------
  // toast
  // -------------------------------
  const toastEl = document.getElementById('mhToast');
  let toastTimer = null;

  function toast(msg){
    if(!toastEl) return;
    toastEl.textContent = msg;
    toastEl.style.display = 'block';
    clearTimeout(toastTimer);
    toastTimer = setTimeout(()=> toastEl.style.display='none', 2200);
  }

  // -------------------------------
  // share
  // -------------------------------
  function enc(s){ return encodeURIComponent(s); }

  document.getElementById('mhCopyLink')?.addEventListener('click', async () => {
    try{
      const link = canonical || window.location.href;
      await navigator.clipboard.writeText(link);
      toast('Link copied');
      logEvent('share_copy');
    }catch(e){
      toast('Copy failed');
    }
  });

  document.getElementById('mhShareWa')?.addEventListener('click', () => {
    logEvent('share_whatsapp');
    window.open('https://wa.me/?text=' + enc(canonical || location.href), '_blank', 'noopener');
  });

  document.getElementById('mhShareLi')?.addEventListener('click', () => {
    logEvent('share_linkedin');
    window.open('https://www.linkedin.com/sharing/share-offsite/?url=' + enc(canonical || location.href), '_blank', 'noopener');
  });

  // -------------------------------
  // modal open/close
  // -------------------------------
  const backdrop = document.getElementById('mhBackdrop');
  const modal = document.getElementById('mhModal');

  function openModal(){
    if(backdrop) backdrop.style.display = 'block';
    if(modal){
      modal.style.display = 'block';
      modal.setAttribute('aria-hidden','false');
    }
    logEvent('comment_modal_open');
  }

  function closeModal(){
    if(backdrop) backdrop.style.display = 'none';
    if(modal){
      modal.style.display = 'none';
      modal.setAttribute('aria-hidden','true');
    }
  }

  document.getElementById('mhOpenModal')?.addEventListener('click', openModal);
  document.getElementById('mhCloseModal')?.addEventListener('click', closeModal);
  document.getElementById('mhCancel')?.addEventListener('click', closeModal);
  backdrop?.addEventListener('click', closeModal);

  // -------------------------------
  // comments submit + prepend
  // -------------------------------
  const form = document.getElementById('mhCommentForm');
  const submitBtn = document.getElementById('mhSubmitBtn');
  const list = document.getElementById('mhCommentList');

  function escapeHtml(s){
    return String(s ?? '')
      .replaceAll("&","&amp;")
      .replaceAll("<","&lt;")
      .replaceAll(">","&gt;")
      .replaceAll('"',"&quot;")
      .replaceAll("'","&#039;");
  }

  function renderCommentCard(it){
    const div = document.createElement('div');
    div.className = 'mh-comment-card';
    div.innerHTML = `
      <div class="mh-comment-top">
        <div class="mh-comment-name">${escapeHtml(it.name)}</div>
        <div class="mh-comment-time">${escapeHtml(it.createdAt)}</div>
      </div>
      <div class="mh-comment-text">${escapeHtml(it.comment)}</div>
    `;
    return div;
  }

  form?.addEventListener('submit', async (e)=>{
    e.preventDefault();
    if(!submitBtn || !list) return;

    submitBtn.disabled = true;
    const old = submitBtn.innerHTML;
    submitBtn.innerHTML = `<span class="mh-spinner"></span>Posting...`;

    try{
      const fd = new FormData(form);
      const res = await fetch(base + 'articles/comment-submit.php', {method:'POST', body:fd});
      const json = await res.json();

      if(json?.ok){
        if(json.item){
          list.prepend(renderCommentCard(json.item));
        }
        toast('Comment posted');
        logEvent('comment_post_ok');
        form.reset();
        closeModal();
      }else{
        toast(json?.message || 'Failed');
      }
    }catch(err){
      toast('Network error');
    }finally{
      submitBtn.disabled = false;
      submitBtn.innerHTML = old;
    }
  });

  // -------------------------------
  // load more comments (pagination)
  // -------------------------------
  const loadBtn = document.getElementById('mhLoadMore');
  let page = 1;
  const perPage = 5;

  loadBtn?.addEventListener('click', async ()=>{
    if(!list) return;

    loadBtn.disabled = true;
    const old = loadBtn.textContent;
    loadBtn.textContent = 'Loading...';

    try{
      page += 1;
      const url = base + `articles/comment-list.php?slug=${enc(slug)}&page=${page}&perPage=${perPage}`;
      const res = await fetch(url);
      const json = await res.json();

      if(json?.ok && Array.isArray(json.items)){
        if(json.items.length === 0){
          loadBtn.style.display = 'none';
        }else{
          // append in same order as your older script
          json.items.reverse().forEach(it => list.append(renderCommentCard(it)));
        }

        const shown = document.querySelectorAll('#mhCommentList .mh-comment-card').length;
        if(shown >= (json.total || 0)) loadBtn.style.display = 'none';
      }else{
        toast('Failed to load');
      }
    }catch(e){
      toast('Network error');
    }finally{
      loadBtn.disabled = false;
      loadBtn.textContent = old;
    }
  });

  // -------------------------------
  // engagement buttons (helpful/reaction/follow + reason)
  // -------------------------------
  const engToast = document.getElementById('mhEngToast');
  function engMsg(msg){
    if(!engToast) return;
    engToast.textContent = msg;
    engToast.style.display = 'block';
    setTimeout(()=> engToast.style.display='none', 2400);
  }

  const reasonWrap = document.getElementById('mhReasonWrap');

  async function submitEng(payload){
    try{
      const fd = new FormData();
      fd.append('slug', slug);
      Object.keys(payload).forEach(k=>{
        const v = payload[k];
        if(v !== undefined && v !== null && v !== '') fd.append(k, v);
      });

      const res = await fetch(base + 'articles/engagement-submit.php', {method:'POST', body:fd});
      const json = await res.json();

      if(json?.ok) engMsg(json.message || 'Saved');
      else engMsg(json?.message || 'Failed');
    }catch(e){
      engMsg('Network error');
    }
  }

  document.querySelectorAll('[data-mh-helpful]').forEach(btn=>{
    btn.addEventListener('click', ()=>{
      const v = btn.getAttribute('data-mh-helpful');
      if(v === 'no' && reasonWrap) reasonWrap.style.display = 'block';
      else if(reasonWrap) reasonWrap.style.display = 'none';
      submitEng({helpful: v});
    });
  });

  document.querySelectorAll('input[name="mh_reason"]').forEach(r=>{
    r.addEventListener('change', ()=>{
      submitEng({helpful:'no', reason: r.value});
    });
  });

  document.querySelectorAll('[data-mh-reaction]').forEach(btn=>{
    btn.addEventListener('click', ()=>{
      submitEng({reaction: btn.getAttribute('data-mh-reaction')});
    });
  });

  document.querySelectorAll('[data-mh-follow]').forEach(btn=>{
    btn.addEventListener('click', ()=>{
      submitEng({follow: btn.getAttribute('data-mh-follow') || 'on'});
    });
  });

})();

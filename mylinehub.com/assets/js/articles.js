(function(){
  const root = document.querySelector('[data-mh-articles]');
  if(!root) return;

  const base = root.getAttribute('data-base') || '/';
  const sortSel = document.getElementById('mhSort');
  const search = document.querySelector('[data-mh-articles-search]');

  function logEvent(event, data){
    try{
      fetch(base + 'articles/activity-submit.php', {
        method: 'POST',
        headers: {'Content-Type':'application/json'},
        body: JSON.stringify({
          event,
          path: location.pathname + location.search,
          slug: '',
          data: data || {}
        })
      }).catch(()=>{});
    }catch(e){}
  }

  logEvent('page_view', {ref: document.referrer || ''});

  // sort change submit
  sortSel?.addEventListener('change', () => {
    const form = document.getElementById('mhFilterForm');
    if(form) form.submit();
  });

  // keep cursor at end if q exists
  if(search){
    const q = search.value || '';
    if(q.length > 0){
      try{
        search.focus();
        search.setSelectionRange(q.length, q.length);
      }catch(e){}
    }
  }
})();

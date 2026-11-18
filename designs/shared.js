document.addEventListener('click',e=>{const b=e.target.closest('[data-fav]');if(!b)return;const on=b.getAttribute('aria-pressed')!=='true';b.setAttribute('aria-pressed',on);b.textContent=on?'♥':'♡'});
document.addEventListener('click',e=>{const t=e.target.closest('[role=tab]');if(!t)return;t.parentElement.querySelectorAll('[role=tab]').forEach(x=>x.setAttribute('aria-selected',x===t))});

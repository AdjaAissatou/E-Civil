// Animations au défilement (AOS), comme dans la version d'origine
if (window.AOS) AOS.init({ duration: 800, once: true, offset: 100 });

// Page Documents : recherche et filtre par catégorie
(function () {
  const cartes = document.querySelectorAll('.document-card');
  if (!cartes.length) return;
  const recherche = document.getElementById('searchInput');
  const boutons = document.querySelectorAll('[data-filter]');
  const aucun = document.getElementById('aucun-document');
  let filtre = 'all';
  function appliquer() {
    const texte = (recherche.value || '').toLowerCase();
    let visibles = 0;
    cartes.forEach(c => {
      const ok = (filtre === 'all' || c.dataset.category === filtre) && c.dataset.texte.toLowerCase().includes(texte);
      c.style.display = ok ? '' : 'none';
      if (ok) visibles++;
    });
    if (aucun) aucun.classList.toggle('d-none', visibles > 0);
  }
  boutons.forEach(b => b.addEventListener('click', () => {
    boutons.forEach(x => x.classList.remove('active'));
    b.classList.add('active');
    filtre = b.dataset.filter;
    appliquer();
  }));
  recherche.addEventListener('input', appliquer);
})();

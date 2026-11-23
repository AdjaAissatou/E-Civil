// Indique que le script est actif : seuls alors les blocs .reveler sont masqués avant leur apparition
document.documentElement.classList.add('js');

// Barre de navigation : plus compacte et opaque dès que l'on fait défiler la page
(function () {
  const barre = document.querySelector('.navbar');
  if (!barre) return;
  const maj = () => barre.classList.toggle('defile', window.scrollY > 24);
  maj();
  window.addEventListener('scroll', maj, { passive: true });
})();

// Apparition douce des blocs marqués .reveler lorsqu'ils entrent à l'écran
(function () {
  const blocs = document.querySelectorAll('.reveler');
  if (!blocs.length) return;
  if (!('IntersectionObserver' in window)) { blocs.forEach(b => b.classList.add('visible')); return; }
  const obs = new IntersectionObserver(entrees => entrees.forEach(e => {
    if (e.isIntersecting) { e.target.classList.add('visible'); obs.unobserve(e.target); }
  }), { rootMargin: '0px 0px -8% 0px', threshold: .08 });
  blocs.forEach(b => obs.observe(b));
})();

// Page Documents : recherche instantanée et filtre par catégorie
(function () {
  const cartes = document.querySelectorAll('.document-card');
  if (!cartes.length) return;
  const recherche = document.getElementById('searchInput');
  const boutons = document.querySelectorAll('[data-filter]');
  const aucun = document.getElementById('aucun-document');
  const compteur = document.getElementById('compteur-documents');
  let filtre = 'all';
  const sansAccents = t => t.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase();
  function appliquer() {
    const texte = sansAccents(recherche.value || '');
    let visibles = 0;
    cartes.forEach(c => {
      const ok = (filtre === 'all' || c.dataset.category === filtre) && sansAccents(c.dataset.texte).includes(texte);
      c.hidden = !ok;
      if (ok) visibles++;
    });
    if (aucun) aucun.classList.toggle('d-none', visibles > 0);
    if (compteur) compteur.textContent = visibles + (visibles > 1 ? ' documents' : ' document');
  }
  boutons.forEach(b => b.addEventListener('click', () => {
    boutons.forEach(x => { x.classList.remove('active'); x.setAttribute('aria-pressed', 'false'); });
    b.classList.add('active'); b.setAttribute('aria-pressed', 'true');
    filtre = b.dataset.filter;
    appliquer();
  }));
  recherche.addEventListener('input', appliquer);
  appliquer();
})();

/* Formulaire de demande : l'adresse de livraison n'apparaît que pour un document papier */
(function () {
  const bloc = document.getElementById('bloc-livraison');
  if (!bloc) return;
  const choix = document.querySelectorAll('input[name="format"]');
  function appliquer() {
    const papier = Array.from(choix).some(r => r.checked && r.value === 'PHYSIQUE');
    bloc.classList.toggle('d-none', !papier);
    bloc.querySelectorAll('input').forEach(i => { i.required = papier; });
  }
  choix.forEach(r => r.addEventListener('change', appliquer));
  appliquer();
})();

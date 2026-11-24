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

/* Confirmation des actions sensibles (désactiver un compte, supprimer un profil ou un rôle) */
document.addEventListener('click', function (e) {
  const b = e.target.closest('[data-confirmation]');
  if (b && !window.confirm(b.dataset.confirmation)) e.preventDefault();
});

/* Formulaire de demande : pièces à fournir selon le motif, adresse masquée en cas de retrait à une mission */
(function () {
  const motif = document.getElementById('motifId');
  const pieces = document.getElementById('pieces-motif');
  if (motif && pieces) {
    const afficher = () => {
      const o = motif.options[motif.selectedIndex];
      const p = o && o.dataset.pieces;
      pieces.textContent = p ? 'Pièces à fournir pour ce motif : ' + p : '';
    };
    motif.addEventListener('change', afficher);
    afficher();
  }
  const mission = document.getElementById('missionRetraitId');
  const adresse = document.getElementById('bloc-adresse');
  if (mission && adresse) {
    const appliquer = () => {
      const retrait = mission.value !== '';
      adresse.classList.toggle('d-none', retrait);
      adresse.querySelectorAll('input').forEach(i => { if (retrait) i.required = false; });
    };
    mission.addEventListener('change', appliquer);
    document.querySelectorAll('input[name="format"]').forEach(r => r.addEventListener('change', appliquer));
    appliquer();
  }
})();

/* Mon compte : le pays de résidence ne concerne que la diaspora */
(function () {
  const bloc = document.getElementById('bloc-pays');
  if (!bloc) return;
  const radios = document.querySelectorAll('input[name="typeUtilisateur"]');
  const appliquer = () => {
    const diaspora = Array.from(radios).some(r => r.checked && r.value === 'DIASPORA');
    bloc.classList.toggle('d-none', !diaspora);
    bloc.querySelector('input').required = diaspora;
  };
  radios.forEach(r => r.addEventListener('change', appliquer));
  appliquer();
})();


/* Vérification d'identité : pièce présentée, aperçus, selfie pris avec la caméra (ou à défaut un fichier) */
(function () {
  const selfie = document.getElementById('selfie');
  if (!selfie) return;
  const verso = document.getElementById('bloc-verso');
  const radios = document.querySelectorAll('input[name="typePiece"]');
  const libelleRecto = document.getElementById('libelle-recto');
  const pieceChoisie = () => {
    const passeport = Array.from(radios).some(r => r.checked && r.value === 'PASSEPORT');
    verso.classList.toggle('d-none', passeport);
    verso.querySelector('input').required = !passeport;
    libelleRecto.textContent = passeport ? 'Passeport : page avec la photo' : "Carte d'identité : recto";
  };
  radios.forEach(r => r.addEventListener('change', pieceChoisie));
  pieceChoisie();

  const apercu = (input) => {
    const img = document.querySelector('[data-apercu="' + input.name + '"]');
    if (!img) return;
    const f = input.files && input.files[0];
    if (f) { img.src = URL.createObjectURL(f); img.classList.remove('d-none'); } else { img.classList.add('d-none'); }
  };
  document.querySelectorAll('input[type="file"][name="recto"], input[type="file"][name="verso"], #selfie')
    .forEach(i => i.addEventListener('change', () => apercu(i)));

  const ouvrir = document.getElementById('ouvrir-camera');
  const capturer = document.getElementById('capturer');
  const video = document.getElementById('camera');
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia || typeof DataTransfer === 'undefined') return;
  ouvrir.classList.remove('d-none');
  let flux = null;
  ouvrir.addEventListener('click', async () => {
    try {
      flux = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'user', width: { ideal: 1280 } }, audio: false });
      video.srcObject = flux; video.classList.remove('d-none'); await video.play();
      capturer.classList.remove('d-none'); ouvrir.classList.add('d-none');
    } catch (e) {
      ouvrir.textContent = 'Caméra indisponible : choisissez une photo ci-dessous';
      ouvrir.disabled = true;
    }
  });
  capturer.addEventListener('click', () => {
    const toile = document.createElement('canvas');
    toile.width = video.videoWidth; toile.height = video.videoHeight;
    toile.getContext('2d').drawImage(video, 0, 0);
    toile.toBlob(blob => {
      const dt = new DataTransfer();
      dt.items.add(new File([blob], 'selfie.jpg', { type: 'image/jpeg' }));
      selfie.files = dt.files;
      apercu(selfie);
      flux.getTracks().forEach(t => t.stop());
      video.classList.add('d-none'); capturer.classList.add('d-none');
      ouvrir.classList.remove('d-none'); ouvrir.innerHTML = '<i class="bi bi-camera me-1"></i>Reprendre le selfie';
    }, 'image/jpeg', 0.9);
  });
})();

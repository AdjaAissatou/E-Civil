<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- AOS CSS -->
<link href="https://unpkg.com/aos@2.3.1/dist/aos.css" rel="stylesheet">



<!-- Hero Section améliorée -->
<section class="hero d-flex align-items-center text-center text-white" 
         style="height: 100vh; background-image: linear-gradient(rgba(0,0,0,.7), rgba(0,0,0,.5)), url('${pageContext.request.contextPath}/images/herobg.png') center/cover no-repeat; padding-top: 76px;">
    <div class="container">
        <div data-aos="fade-up" data-aos-duration="1000">
            <h1 class="display-3 fw-bold mb-4">E-Civil Sénégal</h1>
            <p class="lead mb-4 fs-5">Vos documents d'état civil en ligne, accessibles partout, en toute sécurité.</p>
            <div class="d-flex gap-3 justify-content-center flex-wrap">
                <a href="${pageContext.request.contextPath}/inscription" class="btn btn-success btn-lg rounded-pill shadow-lg hover-scale px-4">
                    <i class="bi bi-person-plus me-2"></i>Créer un compte
                </a>
                <a href="#services" class="btn btn-outline-light btn-lg rounded-pill hover-scale px-4">
                    <i class="bi bi-info-circle me-2"></i>Découvrir nos services
                </a>
            </div>
        </div>
        
        <!-- Statistiques rapides -->
        <div class="row mt-5 pt-5" data-aos="fade-up" data-aos-delay="300">
            <div class="col-md-3">
                <h3 class="fw-bold text-warning">10,000+</h3>
                <p class="small">Documents délivrés</p>
            </div>
            <div class="col-md-3">
                <h3 class="fw-bold text-warning">5,000+</h3>
                <p class="small">Utilisateurs satisfaits</p>
            </div>
            <div class="col-md-3">
                <h3 class="fw-bold text-warning">24/7</h3>
                <p class="small">Service disponible</p>
            </div>
            <div class="col-md-3">
                <h3 class="fw-bold text-warning">100%</h3>
                <p class="small">Sécurisé</p>
            </div>
        </div>
    </div>
    
    <!-- Scroll indicator -->
    <div class="position-absolute bottom-0 start-50 translate-middle-x mb-4">
        <a href="#services" class="text-white text-decoration-none">
            <div class="d-flex flex-column align-items-center">
                <span class="small mb-2">Découvrir</span>
                <i class="bi bi-chevron-down fs-4 animate-bounce"></i>
            </div>
        </a>
    </div>
</section>

<!-- Services Section améliorée -->
<section id="services" class="container py-5 my-5">
    <div class="text-center mb-5" data-aos="fade-up">
        <h2 class="fw-bold mb-3">Nos Services de Documents</h2>
        <p class="text-muted lead">Obtenez vos documents officiels rapidement et facilement</p>
    </div>
    
    <div class="row g-4">
        <div class="col-md-4" data-aos="fade-up" data-aos-delay="100">
            <div class="card service-card text-center p-4 shadow rounded h-100 border-0">
                <div class="card-icon bg-success bg-opacity-10 rounded-circle mx-auto mb-4 d-flex align-items-center justify-content-center" style="width: 80px; height: 80px;">
                    <img src="${pageContext.request.contextPath}/images/birth.png" alt="Acte de naissance" class="img-fluid" style="height:50px;">
                </div>
                <h5 class="fw-semibold mb-3">Acte de Naissance</h5>
                <p class="text-muted small">Obtenez votre extrait d'acte de naissance en quelques clics, sans vous déplacer.</p>
                <div class="mt-auto">
                    <span class="badge bg-success">À partir de 2.000 FCFA</span>
                </div>
            </div>
        </div>
        
        <div class="col-md-4" data-aos="fade-up" data-aos-delay="200">
            <div class="card service-card text-center p-4 shadow rounded h-100 border-0">
                <div class="card-icon bg-primary bg-opacity-10 rounded-circle mx-auto mb-4 d-flex align-items-center justify-content-center" style="width: 80px; height: 80px;">
                    <img src="${pageContext.request.contextPath}/images/marriage.png" alt="Acte de mariage" class="img-fluid" style="height:50px;">
                </div>
                <h5 class="fw-semibold mb-3">Acte de Mariage</h5>
                <p class="text-muted small">Consultez et téléchargez vos actes de mariage rapidement et en toute sécurité.</p>
                <div class="mt-auto">
                    <span class="badge bg-primary">À partir de 3.000 FCFA</span>
                </div>
            </div>
        </div>
        
        <div class="col-md-4" data-aos="fade-up" data-aos-delay="300">
            <div class="card service-card text-center p-4 shadow rounded h-100 border-0">
                <div class="card-icon bg-warning bg-opacity-10 rounded-circle mx-auto mb-4 d-flex align-items-center justify-content-center" style="width: 80px; height: 80px;">
                    <img src="${pageContext.request.contextPath}/images/death.png" alt="Acte de décès" class="img-fluid" style="height:50px;">
                </div>
                <h5 class="fw-semibold mb-3">Acte de Décès</h5>
                <p class="text-muted small">Accédez facilement aux documents officiels en cas de décès d'un proche.</p>
                <div class="mt-auto">
                    <span class="badge bg-warning">À partir de 2.500 FCFA</span>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Process Section -->
<section class="bg-light py-5">
    <div class="container">
        <div class="text-center mb-5" data-aos="fade-up">
            <h2 class="fw-bold mb-3">Comment ça marche ?</h2>
            <p class="text-muted lead">3 étapes simples pour obtenir vos documents</p>
        </div>
        
        <div class="row g-4">
            <div class="col-md-4 text-center" data-aos="fade-up" data-aos-delay="100">
                <div class="process-step bg-white rounded-circle shadow mx-auto mb-3 d-flex align-items-center justify-content-center" style="width: 100px; height: 100px;">
                    <span class="fs-2 fw-bold text-success">1</span>
                </div>
                <h5>Inscription</h5>
                <p class="text-muted small">Créez votre compte en 2 minutes avec vos informations personnelles</p>
            </div>
            
            <div class="col-md-4 text-center" data-aos="fade-up" data-aos-delay="200">
                <div class="process-step bg-white rounded-circle shadow mx-auto mb-3 d-flex align-items-center justify-content-center" style="width: 100px; height: 100px;">
                    <span class="fs-2 fw-bold text-success">2</span>
                </div>
                <h5>Demande & Paiement</h5>
                <p class="text-muted small">Sélectionnez le document souhaité et payez en ligne de manière sécurisée</p>
            </div>
            
            <div class="col-md-4 text-center" data-aos="fade-up" data-aos-delay="300">
                <div class="process-step bg-white rounded-circle shadow mx-auto mb-3 d-flex align-items-center justify-content-center" style="width: 100px; height: 100px;">
                    <span class="fs-2 fw-bold text-success">3</span>
                </div>
                <h5>Récupération</h5>
                <p class="text-muted small">Téléchargez votre document depuis votre espace personnel sous 48h</p>
            </div>
        </div>
    </div>
</section>

<!-- About Section améliorée -->
<section class="container my-5 py-5">
    <div class="row align-items-center">
        <div class="col-md-6 mb-4 mb-md-0" data-aos="fade-right">
            <h2 class="fw-bold mb-4">Une révolution numérique pour l'état civil</h2>
            <p class="text-muted mb-4">E-Civil Sénégal modernise l'accès aux documents officiels en offrant une plateforme digitale sécurisée, rapide et accessible à tous les Sénégalais.</p>
            
            <div class="d-flex mb-3" data-aos="fade-right" data-aos-delay="100">
                <i class="bi bi-shield-check text-success fs-4 me-3"></i>
                <div>
                    <h6 class="fw-bold">Sécurité garantie</h6>
                    <p class="text-muted small mb-0">Vos données personnelles sont protégées et cryptées</p>
                </div>
            </div>
            
            <div class="d-flex mb-3" data-aos="fade-right" data-aos-delay="200">
                <i class="bi bi-lightning-charge text-success fs-4 me-3"></i>
                <div>
                    <h6 class="fw-bold">Rapidité d'exécution</h6>
                    <p class="text-muted small mb-0">Obtenez vos documents en moins de 48 heures</p>
                </div>
            </div>
            
            <div class="d-flex mb-3" data-aos="fade-right" data-aos-delay="300">
                <i class="bi bi-globe text-success fs-4 me-3"></i>
                <div>
                    <h6 class="fw-bold">Accessibilité totale</h6>
                    <p class="text-muted small mb-0">Disponible partout, y compris pour la diaspora</p>
                </div>
            </div>
        </div>
        
        <div class="col-md-6" data-aos="fade-left">
            <div class="position-relative">
                <img src="${pageContext.request.contextPath}/images/about.jpg" alt="Plateforme E-Civil" class="img-fluid rounded shadow-lg">
                <div class="position-absolute top-0 start-0 mt-3 ms-3">
                    <span class="badge bg-success fs-6">Nouveau</span>
                </div>
            </div>
        </div>
    </div>
</section>
<!-- FAQ -->
<section class="bg-light py-5">
    <div class="container">
        <h2 class="text-center fw-bold mb-5" data-aos="fade-up">Questions fréquentes</h2>
        <div class="accordion" id="faqAccordion">
            <div class="accordion-item" data-aos="fade-up" data-aos-delay="100">
                <h2 class="accordion-header" id="faq1">
                    <button class="accordion-button" type="button" data-bs-toggle="collapse" data-bs-target="#collapse1">
                        Comment créer un compte ?
                    </button>
                </h2>
                <div id="collapse1" class="accordion-collapse collapse show" data-bs-parent="#faqAccordion">
                    <div class="accordion-body">Cliquez sur "Créer un compte" en haut de la page et remplissez le formulaire.</div>
                </div>
            </div>
            <div class="accordion-item" data-aos="fade-up" data-aos-delay="200">
                <h2 class="accordion-header" id="faq2">
                    <button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapse2">
                        Quels documents sont disponibles ?
                    </button>
                </h2>
                <div id="collapse2" class="accordion-collapse collapse" data-bs-parent="#faqAccordion">
                    <div class="accordion-body">Actes de naissance, mariage, décès et d’autres documents officiels à venir.</div>
                </div>
            </div>
        </div>
    </div>
</section>
<!-- Témoignages -->
<section class="bg-light py-5">
    <div class="container">
        <div class="text-center mb-5" data-aos="fade-up">
            <h2 class="fw-bold mb-3">Ils nous font confiance</h2>
            <p class="text-muted lead">Découvrez les retours de nos utilisateurs</p>
        </div>
        
        <div class="row g-4">
            <div class="col-md-4" data-aos="fade-up" data-aos-delay="100">
                <div class="card testimonial-card h-100 border-0 shadow-sm">
                    <div class="card-body p-4">
                        <div class="d-flex align-items-center mb-3">
                            <img src="${pageContext.request.contextPath}/images/user1.jpg" alt="User" class="rounded-circle me-3" width="50" height="50">
                            <div>
                                <h6 class="mb-0">Mariama Diop</h6>
                                <small class="text-muted">Dakar</small>
                            </div>
                        </div>
                        <p class="card-text">"Service excellent ! J'ai pu obtenir l'acte de naissance de mon fils depuis la France en moins de 24h."</p>
                        <div class="text-warning">
                            <i class="bi bi-star-fill"></i>
                            <i class="bi bi-star-fill"></i>
                            <i class="bi bi-star-fill"></i>
                            <i class="bi bi-star-fill"></i>
                            <i class="bi bi-star-fill"></i>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="col-md-4" data-aos="fade-up" data-aos-delay="200">
                <div class="card testimonial-card h-100 border-0 shadow-sm">
                    <div class="card-body p-4">
                        <div class="d-flex align-items-center mb-3">
                            <img src="${pageContext.request.contextPath}/images/user2.jpg" alt="User" class="rounded-circle me-3" width="50" height="50">
                            <div>
                                <h6 class="mb-0">Papa Sow</h6>
                                <small class="text-muted">Thiès</small>
                            </div>
                        </div>
                        <p class="card-text">"Plus besoin de faire la queue à la mairie. Simple, rapide et efficace. Je recommande !"</p>
                        <div class="text-warning">
                            <i class="bi bi-star-fill"></i>
                            <i class="bi bi-star-fill"></i>
                            <i class="bi bi-star-fill"></i>
                            <i class="bi bi-star-fill"></i>
                            <i class="bi bi-star-fill"></i>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="col-md-4" data-aos="fade-up" data-aos-delay="300">
                <div class="card testimonial-card h-100 border-0 shadow-sm">
                    <div class="card-body p-4">
                        <div class="d-flex align-items-center mb-3">
                            <img src="${pageContext.request.contextPath}/images/user3.jpg" alt="User" class="rounded-circle me-3" width="50" height="50">
                            <div>
                                <h6 class="mb-0">Aïcha Fall</h6>
                                <small class="text-muted">Saint-Louis</small>
                            </div>
                        </div>
                        <p class="card-text">"Interface très intuitive. J'ai aidé ma grand-mère à faire sa demande en 10 minutes seulement."</p>
                        <div class="text-warning">
                            <i class="bi bi-star-fill"></i>
                            <i class="bi bi-star-fill"></i>
                            <i class="bi bi-star-fill"></i>
                            <i class="bi bi-star-fill"></i>
                            <i class="bi bi-star-half"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- Call to Action amélioré -->
<section class="text-center py-5 position-relative" style="background: linear-gradient(135deg, #00b894, #55efc4); overflow: hidden;">
    <div class="position-absolute top-0 start-0 w-100 h-100 opacity-10">
        <div class="pattern-dots"></div>
    </div>
    
    <div class="container position-relative" data-aos="zoom-in">
        <h3 class="text-white fw-bold mb-4 display-6">Prêt à simplifier vos démarches ?</h3>
        <p class="text-white mb-4 lead">Rejoignez les milliers de Sénégalais qui utilisent déjà E-Civil</p>
        <div class="d-flex gap-3 justify-content-center flex-wrap">
            <a href="${pageContext.request.contextPath}/inscription" class="btn btn-light btn-lg rounded-pill shadow-lg hover-scale px-5">
                <i class="bi bi-rocket-takeoff me-2"></i>Commencer maintenant
            </a>
            <a href="#services" class="btn btn-outline-light btn-lg rounded-pill hover-scale px-4">
                <i class="bi bi-play-circle me-2"></i>Voir la démo
            </a>
        </div>
        <p class="text-white mt-3 small">Aucun frais d'inscription • Support 7j/7 • Paiement sécurisé</p>
    </div>
</section>



<!-- AOS JS et initialisation -->
<script src="https://unpkg.com/aos@2.3.1/dist/aos.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<style>
.hover-scale { transition: transform .3s, box-shadow .3s; }
.hover-scale:hover { transform: scale(1.05); box-shadow: 0 10px 20px rgba(0,0,0,0.2); }
.service-card { transition: transform .3s, box-shadow .3s; }
.service-card:hover { transform: translateY(-10px); box-shadow: 0 15px 30px rgba(0,0,0,0.15); }
.testimonial-card { transition: transform .3s; }
.testimonial-card:hover { transform: translateY(-5px); }
.animate-bounce { animation: bounce 2s infinite; }
@keyframes bounce {
    0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
    40% { transform: translateY(-10px); }
    60% { transform: translateY(-5px); }
}
.pattern-dots {
    background-image: radial-gradient(#ffffff 1px, transparent 1px);
    background-size: 20px 20px;
}
</style>

<script>
// Initialisation AOS
AOS.init({
    duration: 800,
    once: true,
    offset: 100
});

// Smooth scroll pour les ancres
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});
</script>
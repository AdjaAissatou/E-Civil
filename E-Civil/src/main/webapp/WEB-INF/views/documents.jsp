<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<link href="https://unpkg.com/aos@2.3.1/dist/aos.css" rel="stylesheet">

<div class="container-fluid py-4">
    <!-- En-tête -->
    <div class="text-center mb-5">
        <h1 class="display-4 fw-bold text-brown mb-3">
            <i class="bi bi-file-earmark-text me-2"></i>Documents d'État Civil
        </h1>
        <p class="lead text-muted">Consultez les différents documents disponibles et leurs tarifs</p>
    </div>

    <!-- Barre de recherche -->
    <div class="row mb-4">
        <div class="col-12">
            <div class="card shadow-sm border-0 bg-beige-light">
                <div class="card-body py-3">
                    <div class="input-group">
                        <span class="input-group-text bg-brown text-light border-0">
                            <i class="bi bi-search"></i>
                        </span>
                        <input type="text" id="searchInput" class="form-control border-0 bg-beige-light" 
                               placeholder="Rechercher un document..." onkeyup="searchDocuments()">
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Filtres par catégorie -->
    <div class="row mb-4">
        <div class="col-12">
            <div class="card shadow-sm border-0 bg-beige-light">
                <div class="card-body">
                    <h5 class="card-title text-brown">
                        <i class="bi bi-filter me-2"></i>Filtrer par catégorie
                    </h5>
                    <div class="btn-group flex-wrap" role="group">
                        <button type="button" class="btn btn-brown active" data-filter="all">Tous</button>
                        <c:forEach var="variete" items="${varietes}">
                            <button type="button" class="btn btn-brown-outline" 
                                    data-filter="variete-${variete[0]}">${variete[1]}</button>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Cartes des documents -->
    <div class="row g-4" id="documents-container">
        <c:choose>
            <c:when test="${not empty documents}">
                <c:forEach var="doc" items="${documents}">
                    <div class="col-xl-4 col-lg-6 col-md-6 mb-4 document-card" 
                         data-variete="variete-${doc[0]}" 
                         data-category="${doc[6]}"
                         data-name="${doc[1]}">
                        <div class="card h-100 shadow-sm border-0 document-card-inner">
                            <div class="card-header bg-brown text-light border-0 py-3">
                                <div class="d-flex justify-content-between align-items-center">
                                    <span class="badge bg-beige text-brown">${doc[6]}</span>
                                    <c:if test="${doc[5] <= 30}">
                                        <span class="badge bg-warning-brown text-dark">
                                            <i class="bi bi-lightning me-1"></i>Validation rapide
                                        </span>
                                    </c:if>
                                </div>
                            </div>
                            
                            <div class="card-body d-flex flex-column bg-beige-light">
                                <h5 class="card-title text-brown mb-3">
                                    <i class="bi bi-file-earmark me-2"></i>${doc[1]}
                                </h5>
                                
                                <p class="card-text text-brown-light flex-grow-1">${doc[2]}</p>
                                
                                <div class="mt-auto">
                                    <!-- Informations tarifaires -->
                                    <div class="row text-center mb-3">
                                        <div class="col-4">
                                            <div class="border-brown rounded p-2 bg-beige">
                                                <small class="text-brown-light d-block">Tarif</small>
                                                <strong class="text-brown-dark">
                                                    <fmt:formatNumber value="${doc[4]}" type="number"/> FCFA
                                                </strong>
                                            </div>
                                        </div>
                                        <div class="col-4">
                                            <div class="border-brown rounded p-2 bg-beige">
                                                <small class="text-brown-light d-block">Validité</small>
                                                <strong class="text-brown">${doc[3]} jours</strong>
                                            </div>
                                        </div>
                                        <div class="col-4">
                                            <div class="border-brown rounded p-2 bg-beige">
                                                <small class="text-brown-light d-block">Délai</small>
                                                <strong class="text-brown-dark">${doc[5]} jour(s)</strong>
                                            </div>
                                        </div>
                                    </div>
                                    
                                    <!-- BOUTONS D'ACTION -->
                                    <div class="d-grid gap-2">
                                        <c:choose>
                                            <c:when test="${not empty sessionScope.compte}">
                                                <a href="${pageContext.request.contextPath}/demande?type=nature&id=${doc[0]}" 
                                                   class="btn btn-brown btn-sm">
                                                    <i class="bi bi-cart-plus me-1"></i>Faire une demande
                                                </a>
                                            </c:when>
                                            <c:otherwise>
                                                <a href="${pageContext.request.contextPath}/connexion?redirect=documents" 
                                                   class="btn btn-brown-outline btn-sm">
                                                    <i class="bi bi-person me-1"></i>Se connecter pour demander
                                                </a>
                                            </c:otherwise>
                                        </c:choose>
		                                        
		<button class="btn btn-beige-outline btn-sm" 
		        onclick="showDetails('${doc[0]}', '${doc[1]}', '${doc[2]}', ${doc[3]}, ${doc[4]}, ${doc[5]}, '${doc[6]}')">
		    <i class="bi bi-info-circle me-1"></i>Plus d'infos
		</button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="col-12 text-center py-5">
                    <i class="bi bi-search display-1 text-brown-light"></i>
                    <h4 class="text-brown-light mt-3">Aucun document disponible</h4>
                    <p class="text-brown-light">Aucun type de document n'est actuellement disponible.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- Message si aucun document trouvé -->
    <div class="row d-none" id="no-results">
        <div class="col-12 text-center py-5">
            <i class="bi bi-search display-1 text-brown-light"></i>
            <h4 class="text-brown-light mt-3">Aucun document trouvé</h4>
            <p class="text-brown-light">Essayez de modifier vos critères de recherche</p>
        </div>
    </div>
</div>

<!-- Modal pour les détails -->
<div class="modal fade" id="documentModal" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header bg-brown text-light">
                <h5 class="modal-title" id="modalTitle">Détails du document</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body bg-beige-light" id="modalContent">
                <!-- Contenu chargé dynamiquement -->
            </div>
            <div class="modal-footer bg-beige">
                <button type="button" class="btn btn-beige-outline" data-bs-dismiss="modal">Fermer</button>
                
                <c:choose>
                    <c:when test="${not empty sessionScope.compte}">
                        <a href="#" class="btn btn-brown" id="modalDemandeLink">
                            <i class="bi bi-cart-plus me-1"></i>Faire une demande
                        </a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/connexion?redirect=documents" 
                           class="btn btn-brown-outline" id="modalConnexionLink">
                            <i class="bi bi-person me-1"></i>Se connecter pour demander
                        </a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>
    <!-- Scripts -->
    <script src="https://unpkg.com/aos@2.3.1/dist/aos.js"></script>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<style>
/* Variables de couleurs harmonisées */
:root {
    --brown-dark: #3b2a1f;
    --brown: #5c3d2e;
    --brown-light: #7a5a48;
    --beige: #e0b084;
    --beige-light: #f8f4f0;
    --beige-lighter: #fdfaf7;
}

.text-brown { color: var(--brown) !important; }
.text-brown-dark { color: var(--brown-dark) !important; }
.text-brown-light { color: var(--brown-light) !important; }

.bg-brown { background: linear-gradient(135deg, var(--brown-dark) 0%, var(--brown) 100%) !important; }
.bg-beige { background-color: var(--beige) !important; }
.bg-beige-light { background-color: var(--beige-light) !important; }
.bg-beige-lighter { background-color: var(--beige-lighter) !important; }

.btn-brown {
    background: linear-gradient(135deg, var(--brown) 0%, var(--brown-light) 100%);
    border: none;
    color: white !important;
}

.btn-brown:hover {
    background: linear-gradient(135deg, var(--brown-dark) 0%, var(--brown) 100%);
    color: white;
}

.btn-brown-outline {
    border: 1px solid var(--brown);
    color: var(--brown);
    background: transparent;
}

.btn-brown-outline:hover {
    background: var(--brown);
    color: white;
}

.btn-beige-outline {
    border: 1px solid var(--beige);
    color: var(--brown);
    background: transparent;
}

.btn-beige-outline:hover {
    background: var(--beige);
    color: var(--brown-dark);
}

.border-brown { border: 1px solid var(--beige) !important; }

.badge.bg-warning-brown {
    background: linear-gradient(135deg, #d4a574 0%, #e0b084 100%) !important;
    color: var(--brown-dark) !important;
}

/* Styles existants */
.document-card {
    transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.document-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 25px rgba(92, 61, 46, 0.1) !important;
}

.card {
    border-radius: 10px;
    overflow: hidden;
}

.btn-group .btn {
    border-radius: 20px;
    margin: 2px;
}

.document-card-inner {
    background: linear-gradient(135deg, var(--beige-lighter) 0%, var(--beige-light) 100%);
}

.shadow-sm {
    box-shadow: 0 2px 4px rgba(59, 42, 31, 0.1) !important;
}
</style>

<script>
// Filtrage des documents par catégorie
document.addEventListener('DOMContentLoaded', function() {
    const filterButtons = document.querySelectorAll('[data-filter]');
    const documentCards = document.querySelectorAll('.document-card');
    
    filterButtons.forEach(button => {
        button.addEventListener('click', function() {
            filterButtons.forEach(btn => {
                btn.classList.remove('active', 'btn-brown');
                btn.classList.add('btn-brown-outline');
            });
            
            this.classList.remove('btn-brown-outline');
            this.classList.add('active', 'btn-brown');
            
            const filterValue = this.getAttribute('data-filter');
            let visibleCount = 0;
            
            documentCards.forEach(card => {
                if (filterValue === 'all') {
                    card.style.display = 'block';
                    visibleCount++;
                } else {
                    const cardVariete = card.getAttribute('data-variete');
                    if (cardVariete === filterValue) {
                        card.style.display = 'block';
                        visibleCount++;
                    } else {
                        card.style.display = 'none';
                    }
                }
            });
            
            const noResults = document.getElementById('no-results');
            if (visibleCount === 0) {
                noResults.classList.remove('d-none');
            } else {
                noResults.classList.add('d-none');
            }
        });
    });
});

// Recherche en temps réel
function searchDocuments() {
    const input = document.getElementById('searchInput');
    const filter = input.value.toLowerCase();
    const cards = document.querySelectorAll('.document-card');
    let visibleCount = 0;
    
    cards.forEach(card => {
        const title = card.querySelector('.card-title').textContent.toLowerCase();
        const description = card.querySelector('.card-text').textContent.toLowerCase();
        const category = card.getAttribute('data-category').toLowerCase();
        const name = card.getAttribute('data-name').toLowerCase();
        
        if (title.includes(filter) || description.includes(filter) || 
            category.includes(filter) || name.includes(filter)) {
            card.style.display = 'block';
            visibleCount++;
        } else {
            card.style.display = 'none';
        }
    });
    
    const noResults = document.getElementById('no-results');
    if (visibleCount === 0) {
        noResults.classList.remove('d-none');
    } else {
        noResults.classList.add('d-none');
    }
}

// Fonction pour afficher les détails
function showDetails(docId, nom, description, tarif, validite, delai, variete) {
    const modalTitle = document.getElementById('modalTitle');
    const modalContent = document.getElementById('modalContent');
    const modalDemandeLink = document.getElementById('modalDemandeLink');
    
    modalTitle.textContent = nom;
    modalContent.innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <h6 class="text-brown">Informations générales</h6>
                <ul class="list-unstyled text-brown-light">
                    <li><strong class="text-brown">Catégorie:</strong> ${variete}</li>
                    <li><strong class="text-brown">Validité:</strong> ${validite} jours</li>
                    <li><strong class="text-brown">Délai de traitement:</strong> ${delai} jour(s)</li>
                    <li><strong class="text-brown">Tarif:</strong> ${tarif.toLocaleString()} FCFA</li>
                </ul>
            </div>
            <div class="col-md-6">
                <h6 class="text-brown">Description</h6>
                <p class="text-brown-light">${description}</p>
            </div>
        </div>
    `;
    
    // Mettre à jour le lien de demande
    if (modalDemandeLink) {
        modalDemandeLink.href = '${pageContext.request.contextPath}/demande?type=nature&id=' + docId;
    }
    
    const modal = new bootstrap.Modal(document.getElementById('documentModal'));
    modal.show();
}
</script>
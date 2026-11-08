<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="container-fluid py-4">
    <div class="row">
        <div class="col-12">
            <div class="card shadow-sm border-0 rounded-3">
                <div class="card-header py-3 text-white" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                    <div class="d-flex justify-content-between align-items-center">
                        <h4 class="mb-0">
                            <i class="bi bi-archive me-2"></i>Interface Chargé de Renseignement
                        </h4>
                        <span class="badge text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                            <i class="bi bi-person-check me-1"></i>Chargé de Renseignement
                        </span>
                    </div>
                </div>
                
                <div class="card-body">
                    <!-- Messages d'alerte -->
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="bi bi-exclamation-triangle me-2"></i>
                            ${error}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <c:if test="${not empty success}">
                        <div class="alert alert-success alert-dismissible fade show" role="alert">
                            <i class="bi bi-check-circle me-2"></i>
                            ${success}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <!-- Description du rôle -->
                    <div class="alert alert-info border-0">
                        <h6 class="alert-heading"><i class="bi bi-info-circle me-2"></i>Rôle du Chargé de Renseignement</h6>
                        <p class="mb-0">
                            Votre mission est de renseigner les registres physiques dans le serveur central via l'API. 
                            Les informations que vous saisissez permettront au système de traiter automatiquement les demandes des usagers 
                            et de générer les documents après signature de l'officier et dépôt du code QR de vérification.
                        </p>
                    </div>

                    <!-- Boutons d'action principaux -->
                    <div class="row mb-4">
                        <div class="col-md-6">
                            <button type="button" class="btn text-white w-100 mb-2" 
                                    style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);"
                                    data-bs-toggle="modal" data-bs-target="#modalNouvelActe">
                                <i class="bi bi-plus-circle me-2"></i>Nouvel Acte
                            </button>
                        </div>
                        <div class="col-md-6">
                            <a href="${pageContext.request.contextPath}/test-api" class="btn btn-outline-secondary w-100 mb-2">
                                <i class="bi bi-wrench me-2"></i>Test API Serveur Central
                            </a>
                        </div>
                    </div>

                    <!-- Statistiques -->
                    <div class="row mb-4">
                        <div class="col-md-3">
                            <div class="card text-white text-center" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                                <div class="card-body py-3">
                                    <h5 class="mb-1">${totalRenseignements}</h5>
                                    <small>Total Actes Saisis</small>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="card text-white text-center" style="background: linear-gradient(135deg, #198754 0%, #20c997 100%);">
                                <div class="card-body py-3">
                                    <h5 class="mb-1">${completes}</h5>
                                    <small>Actes Validés</small>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="card text-white text-center" style="background: linear-gradient(135deg, #d4af37 0%, #f4d03f 100%);">
                                <div class="card-body py-3">
                                    <h5 class="mb-1">${enAttente}</h5>
                                    <small>En Cours de Saisie</small>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="card text-white text-center" style="background: linear-gradient(135deg, #6c757d 0%, #adb5bd 100%);">
                                <div class="card-body py-3">
                                    <h5 class="mb-1">${nonTrouves}</h5>
                                    <small>Non Trouvés</small>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Information sur le processus -->
                    <div class="card border-0 shadow-sm mb-4">
                        <div class="card-header bg-light">
                            <h6 class="mb-0">
                                <i class="bi bi-diagram-3 me-2"></i>Processus de Renseignement
                            </h6>
                        </div>
                        <div class="card-body">
                            <div class="row text-center">
                                <div class="col-md-3">
                                    <div class="p-3 border rounded bg-primary text-white">
                                        <i class="bi bi-1-circle display-6"></i>
                                        <h6 class="mt-2">Saisie des Registres</h6>
                                        <small>Renseignez les actes physiques dans le système</small>
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="p-3 border rounded bg-success text-white">
                                        <i class="bi bi-2-circle display-6"></i>
                                        <h6 class="mt-2">Stockage Central</h6>
                                        <small>Les données sont envoyées au serveur central via API</small>
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="p-3 border rounded bg-warning text-dark">
                                        <i class="bi bi-3-circle display-6"></i>
                                        <h6 class="mt-2">Traitement des Demandes</h6>
                                        <small>Le système recherche automatiquement les actes</small>
                                    </div>
                                </div>
                                <div class="col-md-3">
                                    <div class="p-3 border rounded bg-info text-white">
                                        <i class="bi bi-4-circle display-6"></i>
                                        <h6 class="mt-2">Génération Documents</h6>
                                        <small>Signature officier + QR code pour vérification</small>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal pour créer un nouvel acte -->
<div class="modal fade" id="modalNouvelActe" tabindex="-1" aria-labelledby="modalNouvelActeLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="modalNouvelActeLabel">
                    <i class="bi bi-plus-circle me-2"></i>Renseigner un Nouvel Acte dans le Serveur Central
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form action="${pageContext.request.contextPath}/renseignement" method="post" id="formNouvelActe">
                <input type="hidden" name="action" value="nouvel_acte">
                
                <div class="modal-body">
                    <div class="alert alert-warning">
                        <i class="bi bi-exclamation-triangle me-2"></i>
                        <strong>Important :</strong> Ces informations seront enregistrées dans le serveur central et permettront 
                        de traiter automatiquement les futures demandes des usagers.
                    </div>

                    <div class="row g-3">
                        <!-- Informations de base -->
                        <div class="col-md-6">
                            <label class="form-label fw-bold text-primary">Type d'acte *</label>
                            <select class="form-control" name="type_acte" required id="typeActeSelect">
                                <option value="">Sélectionnez un type</option>
                                <option value="naissance">Acte de Naissance</option>
                                <option value="mariage">Acte de Mariage</option>
                                <option value="deces">Acte de Décès</option>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold text-primary">Nom *</label>
                            <input type="text" class="form-control" name="nom" required placeholder="Nom de famille">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold text-primary">Prénom *</label>
                            <input type="text" class="form-control" name="prenom" required placeholder="Prénom">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold text-primary">Date de naissance *</label>
                            <input type="date" class="form-control" name="date_naissance" required id="dateNaissanceInput">
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-bold text-primary">Lieu de naissance *</label>
                            <input type="text" class="form-control" name="lieu_naissance" required placeholder="Lieu de naissance">
                        </div>

                        <!-- Informations des parents -->
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Nom du père</label>
                            <input type="text" class="form-control" name="nom_pere" placeholder="Nom du père">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Prénom du père</label>
                            <input type="text" class="form-control" name="prenom_pere" placeholder="Prénom du père">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Nom de la mère</label>
                            <input type="text" class="form-control" name="nom_mere" placeholder="Nom de la mère">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Prénom de la mère</label>
                            <input type="text" class="form-control" name="prenom_mere" placeholder="Prénom de la mère">
                        </div>

                        <hr class="my-3">

                        <!-- Informations du registre -->
                        <h6 class="text-success mb-3">
                            <i class="bi bi-book me-2"></i>Informations du Registre Physique
                        </h6>
                        
                        <div class="col-md-4">
                            <label class="form-label fw-bold text-success">Numéro d'acte *</label>
                            <input type="text" class="form-control" name="numero_acte" required placeholder="N° d'acte">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold text-success">Volume</label>
                            <input type="text" class="form-control" name="volume" placeholder="Volume">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold text-success">Folio</label>
                            <input type="text" class="form-control" name="folio" placeholder="Folio">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold text-success">Année *</label>
                            <input type="text" class="form-control" name="annee" required placeholder="Année de l'acte">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold text-success">Commune *</label>
                            <input type="text" class="form-control" name="commune" required placeholder="Commune">
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-bold text-success">Centre d'état civil</label>
                            <input type="text" class="form-control" name="centre" placeholder="Centre d'état civil">
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-bold text-success">Observations</label>
                            <textarea class="form-control" name="observations" rows="3" placeholder="Observations supplémentaires..."></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                        <i class="bi bi-x-circle me-1"></i>Annuler
                    </button>
                    <button type="submit" class="btn text-white" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                        <i class="bi bi-cloud-upload me-1"></i>Envoyer au Serveur Central
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<style>
.card {
    border-radius: 12px;
    overflow: hidden;
}

.card-header {
    border-radius: 0 !important;
}

.modal-header {
    background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
    border-bottom: 2px solid #2d5016;
}

/* Animation pour le chargement */
@keyframes fadeIn {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

.card {
    animation: fadeIn 0.5s ease-in-out;
}

/* Style pour les étapes du processus */
.bg-primary { background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%) !important; }
.bg-success { background: linear-gradient(135deg, #198754 0%, #20c997 100%) !important; }
.bg-warning { background: linear-gradient(135deg, #d4af37 0%, #f4d03f 100%) !important; }
.bg-info { background: linear-gradient(135deg, #0dcaf0 0%, #31d2f2 100%) !important; }
</style>

<script>
// Initialisation quand la page est chargée
document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ Interface Chargé de Renseignement chargée');
    
    // Configurer la date de naissance par défaut à aujourd'hui - 18 ans
    const dateNaissanceInput = document.getElementById('dateNaissanceInput');
    if (dateNaissanceInput && !dateNaissanceInput.value) {
        const today = new Date();
        const eighteenYearsAgo = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
        dateNaissanceInput.valueAsDate = eighteenYearsAgo;
    }
    
    // Animation pour les spinners
    const spinners = document.querySelectorAll('.spinner');
    spinners.forEach(spinner => {
        spinner.style.animation = 'spin 1s linear infinite';
    });
});

// CSS pour l'animation de rotation
const style = document.createElement('style');
style.textContent = `
    @keyframes spin {
        from { transform: rotate(0deg); }
        to { transform: rotate(360deg); }
    }
    
    .spinner {
        display: inline-block;
        animation: spin 1s linear infinite;
    }
    
    /* Amélioration de l'accessibilité */
    .btn:focus, .form-control:focus {
        box-shadow: 0 0 0 0.2rem rgba(45, 80, 22, 0.25);
        border-color: #2d5016;
    }
`;
document.head.appendChild(style);

// Validation du formulaire de nouvel acte
document.getElementById('formNouvelActe')?.addEventListener('submit', function(e) {
    const requiredFields = this.querySelectorAll('[required]');
    let valid = true;
    
    requiredFields.forEach(field => {
        if (!field.value.trim()) {
            valid = false;
            field.classList.add('is-invalid');
        } else {
            field.classList.remove('is-invalid');
        }
    });
    
    if (!valid) {
        e.preventDefault();
        alert('Veuillez remplir tous les champs obligatoires (*)');
    }
});
</script>
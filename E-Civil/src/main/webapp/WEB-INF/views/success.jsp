<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="container-fluid py-4">
    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="card shadow-sm border-0 rounded-3" style="background: linear-gradient(135deg, #fefaf0 0%, #f8f4e9 100%);">
                <div class="card-header py-4 text-white" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%); border: none;">
                    <div class="text-center">
                        <i class="bi bi-check-circle display-1 text-gold"></i>
                        <h3 class="mt-3 mb-0 fw-bold">Demande Envoyée avec Succès !</h3>
                        <p class="mt-2 mb-0 opacity-75">Votre demande a été enregistrée avec succès</p>
                    </div>
                </div>
                
                <div class="card-body">
                    <!-- Informations de la demande -->
                    <div class="alert text-white mb-4" style="background: linear-gradient(135deg, #8b7355 0%, #a08666 100%); border: none;">
                        <h5 class="alert-heading mb-3">
                            <i class="bi bi-info-circle me-2"></i>Votre demande a été enregistrée
                        </h5>
                        <p class="mb-0">Votre demande de document d'état civil a été créée avec succès et sera traitée dans les plus brefs délais.</p>
                    </div>

                    <div class="row">
                        <div class="col-md-6">
                            <div class="card mb-4 border-0 shadow-sm">
                                <div class="card-header text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                                    <h6 class="mb-0">
                                        <i class="bi bi-file-text me-2"></i>Récapitulatif de la demande
                                    </h6>
                                </div>
                                <div class="card-body bg-light">
                                    <c:if test="${not empty demande}">
                                        <p><strong class="text-brown-dark">Référence:</strong> <span class="text-brown">#${demande.idDemande}</span></p>
                                        <p><strong class="text-brown-dark">Document:</strong> <span class="text-brown">${natureDocument}</span></p>
                                        <p><strong class="text-brown-dark">Format:</strong> 
                                            <span class="text-brown">
                                                <c:choose>
                                                    <c:when test="${demande.format == 'numerique'}">Numérique (PDF)</c:when>
                                                    <c:when test="${demande.format == 'physique'}">Physique</c:when>
                                                    <c:otherwise>${demande.format}</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </p>
                                        <p><strong class="text-brown-dark">Statut:</strong> 
                                            <span class="badge text-white" style="background: linear-gradient(135deg, #d4af37 0%, #f4d03f 100%);">En attente de traitement</span>
                                        </p>
                                        <p><strong class="text-brown-dark">Date de demande:</strong> 
                                            <span class="text-brown">
                                                <fmt:formatDate value="${demande.date}" pattern="dd/MM/yyyy à HH:mm"/>
                                            </span>
                                        </p>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                        
                        <div class="col-md-6">
                            <div class="card mb-4 border-0 shadow-sm">
                                <div class="card-header text-white" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                                    <h6 class="mb-0">
                                        <i class="bi bi-clock me-2"></i>Prochaines étapes
                                    </h6>
                                </div>
                                <div class="card-body bg-light">
                                    <div class="timeline">
                                        <div class="timeline-item">
                                            <div class="timeline-marker bg-success"></div>
                                            <div class="timeline-content">
                                                <strong class="text-brown-dark">Demande créée</strong>
                                                <small class="text-brown-light d-block">Votre demande a été enregistrée</small>
                                            </div>
                                        </div>
                                        <div class="timeline-item">
                                            <div class="timeline-marker" style="background: #d4af37;"></div>
                                            <div class="timeline-content">
                                                <strong class="text-brown-dark">Vérification des registres</strong>
                                                <small class="text-brown-light d-block">Recherche dans les archives</small>
                                            </div>
                                        </div>
                                        <div class="timeline-item">
                                            <div class="timeline-marker" style="background: #d4af37;"></div>
                                            <div class="timeline-content">
                                                <strong class="text-brown-dark">Traitement en cours</strong>
                                                <small class="text-brown-light d-block">Préparation du document</small>
                                            </div>
                                        </div>
                                        <div class="timeline-item">
                                            <div class="timeline-marker" style="background: #d4af37;"></div>
                                            <div class="timeline-content">
                                                <strong class="text-brown-dark">Document prêt</strong>
                                                <small class="text-brown-light d-block">Notification par email</small>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Informations de la personne concernée -->
                    <div class="card mb-4 border-0 shadow-sm">
                        <div class="card-header text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                            <h6 class="mb-0">
                                <i class="bi bi-person-vcard me-2"></i>Personne concernée
                            </h6>
                        </div>
                        <div class="card-body bg-light">
                            <div class="row">
                                <div class="col-md-6">
                                    <p><strong class="text-brown-dark">Nom complet:</strong> <span class="text-brown">${demande.prenom} ${demande.nom}</span></p>
                                    <p><strong class="text-brown-dark">Date de naissance:</strong> 
                                        <span class="text-brown">
                                            <fmt:formatDate value="${demande.datedenaissance}" pattern="dd/MM/yyyy"/>
                                        </span>
                                    </p>
                                    <p><strong class="text-brown-dark">Lieu de naissance:</strong> <span class="text-brown">${demande.lieudenaissance}</span></p>
                                </div>
                                <div class="col-md-6">
                                    <p><strong class="text-brown-dark">Sexe:</strong> 
                                        <span class="text-brown">
                                            <c:choose>
                                                <c:when test="${demande.sexe == 'M'}">Masculin</c:when>
                                                <c:when test="${demande.sexe == 'F'}">Féminin</c:when>
                                                <c:otherwise>${demande.sexe}</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </p>
                                    <c:if test="${not empty demande.profession}">
                                        <p><strong class="text-brown-dark">Profession:</strong> <span class="text-brown">${demande.profession}</span></p>
                                    </c:if>
                                    <c:if test="${not empty demande.residance}">
                                        <p><strong class="text-brown-dark">Résidence:</strong> <span class="text-brown">${demande.residance}</span></p>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Informations supplémentaires si disponibles -->
                    <c:if test="${not empty varietes}">
                        <div class="card mb-4 border-0 shadow-sm">
                            <div class="card-header text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                                <h6 class="mb-0">
                                    <i class="bi bi-gear me-2"></i>Informations supplémentaires
                                </h6>
                            </div>
                            <div class="card-body bg-light">
                                <c:forEach var="variete" items="${varietes}">
                                    <div class="mb-3">
                                        <strong class="text-brown-dark">${variete.key}:</strong> <span class="text-brown">${variete.value}</span>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>

                    <!-- Actions -->
                    <div class="border-top border-brown pt-4">
                        <div class="row">
                            <div class="col-md-4 mb-2">
                                <div class="d-grid">
                                    <a href="${pageContext.request.contextPath}/utilisateurlocal" 
                                       class="btn text-white border-0" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                                        <i class="bi bi-house me-1"></i>Tableau de bord
                                    </a>
                                </div>
                            </div>
                            <div class="col-md-4 mb-2">
                                <div class="d-grid">
                                    <a href="${pageContext.request.contextPath}/documents" 
                                       class="btn text-white border-0" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                                        <i class="bi bi-plus-circle me-1"></i>Nouvelle demande
                                    </a>
                                </div>
                            </div>
                            <div class="col-md-4 mb-2">
                                <div class="d-grid">
                                    <button onclick="window.print()" class="btn border-0 text-white" style="background: linear-gradient(135deg, #8b7355 0%, #a08666 100%);">
                                        <i class="bi bi-printer me-1"></i>Imprimer
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<style>
/* Couleurs personnalisées */
.text-brown { color: #5c3d2e; }
.text-brown-dark { color: #3b2a1f; }
.text-brown-light { color: #8b7355; }
.text-gold { color: #d4af37; }

.bg-light { background-color: #fefaf0 !important; }

/* Timeline améliorée */
.timeline {
    position: relative;
    padding-left: 30px;
}

.timeline-item {
    position: relative;
    margin-bottom: 20px;
}

.timeline-marker {
    position: absolute;
    left: -30px;
    top: 0;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background-color: #dee2e6;
    border: 3px solid #fefaf0;
    box-shadow: 0 0 0 2px #5c3d2e;
}

.timeline-marker.bg-success {
    background-color: #2d5016 !important;
    box-shadow: 0 0 0 2px #2d5016;
}

.timeline-content {
    padding-left: 0;
}

/* Effets au survol pour les boutons */
.btn {
    transition: all 0.3s ease;
    font-weight: 600;
}

.btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0,0,0,0.2);
}

/* Style pour l'impression */
@media print {
    .btn {
        display: none !important;
    }
    
    .card {
        box-shadow: none !important;
        border: 1px solid #ddd !important;
    }
}

/* Amélioration de la lisibilité */
.card-body p {
    margin-bottom: 0.8rem;
    line-height: 1.6;
}

.alert {
    border-radius: 10px;
}

.card {
    border-radius: 12px;
    overflow: hidden;
}

.card-header {
    border-radius: 0 !important;
}
</style>
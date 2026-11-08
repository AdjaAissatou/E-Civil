<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="container-fluid py-4">
    <div class="row">
        <div class="col-12">
            <div class="card shadow-sm border-0 rounded-3" style="background: linear-gradient(135deg, #fefaf0 0%, #f8f4e9 100%);">
                <div class="card-header py-3 text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                    <div class="d-flex justify-content-between align-items-center">
                        <h4 class="mb-0">
                            <i class="bi bi-eye me-2"></i>Vérification de la Demande #${demande.idDemande}
                        </h4>
                        <a href="${pageContext.request.contextPath}/delegue" 
                           class="btn btn-sm text-white" 
                           style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                            <i class="bi bi-arrow-left me-1"></i>Retour
                        </a>
                    </div>
                </div>
                
                <div class="card-body">
                    <c:if test="${not empty demande}">
                        
                        <!-- Informations de base de la demande -->
                        <div class="card mb-4 border-0 shadow-sm">
                            <div class="card-header text-white" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                                <h6 class="mb-0">
                                    <i class="bi bi-info-circle me-2"></i>Informations de la Demande
                                </h6>
                            </div>
                            <div class="card-body bg-light">
                                <div class="row">
                                    <div class="col-md-6">
                                        <p><strong class="text-brown-dark">Référence:</strong> <span class="text-brown">#${demande.idDemande}</span></p>
                                        <p><strong class="text-brown-dark">Document:</strong> <span class="text-brown">${demande.natureDocument}</span></p>
                                        <p><strong class="text-brown-dark">Format:</strong> 
                                            <span class="text-brown">
                                                <c:choose>
                                                    <c:when test="${demande.format == 'numerique'}">Numérique (PDF)</c:when>
                                                    <c:when test="${demande.format == 'physique'}">Physique</c:when>
                                                    <c:otherwise>${demande.format}</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </p>
                                    </div>
                                    <div class="col-md-6">
                                        <p><strong class="text-brown-dark">Statut:</strong> 
                                            <c:choose>
                                                <c:when test="${demande.status == 'en attente'}">
                                                    <span class="badge text-white" style="background: linear-gradient(135deg, #d4af37 0%, #f4d03f 100%);">
                                                        <i class="bi bi-clock me-1"></i>En attente
                                                    </span>
                                                </c:when>
                                                <c:when test="${demande.status == 'validée'}">
                                                    <span class="badge text-white" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                                                        <i class="bi bi-check-circle me-1"></i>Validée
                                                    </span>
                                                </c:when>
                                                <c:when test="${demande.status == 'en cours'}">
                                                    <span class="badge text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                                                        <i class="bi bi-gear me-1"></i>En cours
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">${demande.status}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </p>
                                        <p><strong class="text-brown-dark">Date de demande:</strong> 
                                            <span class="text-brown">
                                                <fmt:formatDate value="${demande.date}" pattern="dd/MM/yyyy à HH:mm"/>
                                            </span>
                                        </p>
                                        <p><strong class="text-brown-dark">Destinataire:</strong> 
                                            <span class="text-brown">
                                                <c:choose>
                                                    <c:when test="${demande.destinataire == 'moi'}">Le demandeur</c:when>
                                                    <c:when test="${demande.destinataire == 'autre'}">Une autre personne</c:when>
                                                    <c:otherwise>${demande.destinataire}</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>

                       <!-- Informations de la personne concernée -->
<div class="card mb-4 border-0 shadow-sm">
    <div class="card-header text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
        <h6 class="mb-0">
            <i class="bi bi-person-vcard me-2"></i>Personne Concernée
        </h6>
    </div>
    <div class="card-body bg-light">
        <div class="row">
            <div class="col-md-6">
                <p><strong class="text-brown-dark">Nom complet:</strong> 
                   <span class="text-brown">${demande.prenom} ${demande.nom}</span>
                </p>
                <p><strong class="text-brown-dark">Date de naissance:</strong> 
                    <span class="text-brown">
                        <fmt:formatDate value="${demande.datedenaissance}" pattern="dd/MM/yyyy"/>
                    </span>
                </p>
                <p><strong class="text-brown-dark">Lieu de naissance:</strong> 
                   <span class="text-brown">${demande.lieudenaissance}</span>
                </p>
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
                    <p><strong class="text-brown-dark">Profession:</strong> 
                       <span class="text-brown">${demande.profession}</span>
                    </p>
                </c:if>
                <c:if test="${not empty demande.residance}">
                    <p><strong class="text-brown-dark">Résidence:</strong> 
                       <span class="text-brown">${demande.residance}</span>
                    </p>
                </c:if>
            </div>
        </div>
    </div>
</div>

                        <!-- Informations supplémentaires -->
                        <c:if test="${not empty varietes}">
                            <div class="card mb-4 border-0 shadow-sm">
                                <div class="card-header text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                                    <h6 class="mb-0">
                                        <i class="bi bi-gear me-2"></i>Informations Supplémentaires
                                    </h6>
                                </div>
                                <div class="card-body bg-light">
                                    <c:forEach var="variete" items="${varietes}">
                                        <div class="row mb-2">
                                            <div class="col-md-3">
                                                <strong class="text-brown-dark">${variete.key}:</strong>
                                            </div>
                                            <div class="col-md-9">
                                                <span class="text-brown">${variete.value}</span>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </div>
                        </c:if>
<!-- Informations du demandeur (celui qui a soumis la demande) -->
<div class="card mb-4 border-0 shadow-sm">
    <div class="card-header text-white" style="background: linear-gradient(135deg, #8b7355 0%, #a08666 100%);">
        <h6 class="mb-0">
            <i class="bi bi-person me-2"></i>Informations du Demandeur
        </h6>
    </div>
    <div class="card-body bg-light">
        <c:if test="${not empty demandeur and not empty compteDemandeur}">
            <div class="row">
                <div class="col-md-6">
                    <p><strong class="text-brown-dark">Nom complet:</strong> 
                       <span class="text-brown">${demandeur.prenom} ${demandeur.nom}</span>
                    </p>
                    <p><strong class="text-brown-dark">Email:</strong> 
                       <span class="text-brown">${compteDemandeur.email}</span>
                    </p>
                    <p><strong class="text-brown-dark">Téléphone:</strong> 
                       <span class="text-brown">${demandeur.numerotelephone}</span>
                    </p>
                </div>
                <div class="col-md-6">
                    <p><strong class="text-brown-dark">Profession:</strong> 
                       <span class="text-brown">${demandeur.profession}</span>
                    </p>
                    <p><strong class="text-brown-dark">Résidence:</strong> 
                       <span class="text-brown">${demandeur.residance}</span>
                    </p>
                    <p><strong class="text-brown-dark">Rôle:</strong> 
                       <span class="badge text-white" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                           ${compteDemandeur.role}
                       </span>
                    </p>
                </div>
            </div>
            <div class="mt-3 pt-3 border-top">
                <small class="text-brown-light">
                    <i class="bi bi-info-circle me-1"></i>
                    Cet utilisateur a soumis la demande. La personne concernée par le document peut être différente.
                </small>
            </div>
        </c:if>
        <c:if test="${empty demandeur or empty compteDemandeur}">
            <div class="text-center py-3">
                <i class="bi bi-person-x text-brown-light fs-1"></i>
                <p class="text-brown-light mt-2">Informations du demandeur non disponibles</p>
            </div>
        </c:if>
    </div>
</div>
                        <!-- Résultats de la recherche dans les registres -->
                        <div class="card mb-4 border-0 shadow-sm">
                            <div class="card-header text-white d-flex justify-content-between align-items-center" 
                                 style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                                <h6 class="mb-0">
                                    <i class="bi bi-search me-2"></i>Résultats de la Recherche dans les Registres
                                </h6>
                                <span class="badge text-white" style="background: linear-gradient(135deg, #8b7355 0%, #a08666 100%);">
                                    ${not empty resultatsRecherche ? resultatsRecherche.size() : 0} document(s) trouvé(s)
                                </span>
                            </div>
                            <div class="card-body bg-light">
                                <c:choose>
                                    <c:when test="${empty resultatsRecherche}">
                                        <div class="text-center py-4">
                                            <i class="bi bi-search display-4 text-brown-light mb-3"></i>
                                            <h5 class="text-brown-dark">Aucun document trouvé</h5>
                                            <p class="text-brown-light">Aucun document correspondant n'a été trouvé dans les registres.</p>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="table-responsive">
                                            <table class="table table-hover">
                                                <thead class="text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                                                    <tr>
                                                        <th>Type</th>
                                                        <th>Nature</th>
                                                        <th>Description</th>
                                                        <th>Date Événement</th>
                                                        <th>Lieu</th>
                                                        <th>Commune</th>
                                                        <th>Tarif</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="document" items="${resultatsRecherche}">
                                                        <tr>
                                                            <td class="fw-bold text-brown">${document[1]}</td>
                                                            <td>${document[2]}</td>
                                                            <td>${document[3]}</td>
                                                            <td>
                                                                <fmt:formatDate value="${document[4]}" pattern="dd/MM/yyyy"/>
                                                            </td>
                                                            <td>${document[5]}</td>
                                                            <td>${document[6]}</td>
                                                            <td class="fw-bold text-success">
                                                                <fmt:formatNumber value="${document[8]}" type="number"/> FCFA
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- Actions -->
                        <div class="border-top border-brown pt-4">
                            <div class="row">
                                <div class="col-md-4 mb-2">
                                    <div class="d-grid">
                                        <a href="${pageContext.request.contextPath}/delegue" 
                                           class="btn text-white border-0" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                                            <i class="bi bi-arrow-left me-1"></i>Retour à la liste
                                        </a>
                                    </div>
                                </div>
                                <div class="col-md-4 mb-2">
                                    <c:if test="${demande.status == 'en attente'}">
                                        <div class="d-grid">
                                            <a href="${pageContext.request.contextPath}/validerDemande?id=${demande.idDemande}&action=valider" 
                                               class="btn text-white border-0" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                                                <i class="bi bi-check-circle me-1"></i>Valider la demande
                                            </a>
                                        </div>
                                    </c:if>
                                    <c:if test="${demande.status == 'validée'}">
                                        <div class="d-grid">
                                            <a href="${pageContext.request.contextPath}/validerDemande?id=${demande.idDemande}&action=recherche" 
                                               class="btn text-white border-0" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                                                <i class="bi bi-search me-1"></i>Lancer la recherche
                                            </a>
                                        </div>
                                    </c:if>
                                </div>
                                <div class="col-md-4 mb-2">
                                    <div class="d-grid">
                                        <a href="${pageContext.request.contextPath}/validerDemande?id=${demande.idDemande}&action=rejeter" 
                                           class="btn text-white border-0" style="background: linear-gradient(135deg, #dc3545 0%, #e35d6a 100%);">
                                            <i class="bi bi-x-circle me-1"></i>Rejeter la demande
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>

<style>
.table th {
    border: none;
    font-weight: 600;
    font-size: 0.9rem;
}

.table td {
    vertical-align: middle;
    border-color: #f0e6d3;
}

.card {
    border-radius: 12px;
    overflow: hidden;
}

.card-header {
    border-radius: 0 !important;
}
</style>
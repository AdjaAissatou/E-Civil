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
                            <i class="bi bi-clipboard-check me-2"></i>Gestion des Demandes - Panneau Délégué
                        </h4>
                        <span class="badge text-white" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                            <i class="bi bi-person-gear me-1"></i>Délégué
                        </span>
                    </div>
                </div>
                
                <div class="card-body">
                    <!-- Filtres et statistiques -->
                    <div class="row mb-4">
                        <div class="col-md-3">
                            <div class="card text-white text-center" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                                <div class="card-body py-3">
                                    <h5 class="mb-1">${totalDemandes}</h5>
                                    <small>Total Demandes</small>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="card text-white text-center" style="background: linear-gradient(135deg, #d4af37 0%, #f4d03f 100%);">
                                <div class="card-body py-3">
                                    <h5 class="mb-1">${demandesEnAttente}</h5>
                                    <small>En Attente</small>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="card text-white text-center" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                                <div class="card-body py-3">
                                    <h5 class="mb-1">${demandesValidees}</h5>
                                    <small>Validées</small>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="card text-white text-center" style="background: linear-gradient(135deg, #8b7355 0%, #a08666 100%);">
                                <div class="card-body py-3">
                                    <h5 class="mb-1">${demandesTerminees}</h5>
                                    <small>Terminées</small>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Filtres -->
                    <div class="card mb-4 border-0 shadow-sm">
                        <div class="card-body">
                            <form method="get" class="row g-3">
                                <div class="col-md-3">
                                    <label class="form-label text-brown fw-bold">Statut</label>
                                    <select name="statut" class="form-control border-brown">
                                        <option value="">Tous les statuts</option>
                                        <option value="en attente" ${param.statut == 'en attente' ? 'selected' : ''}>En attente</option>
                                        <option value="validée" ${param.statut == 'validée' ? 'selected' : ''}>Validée</option>
                                        <option value="en cours" ${param.statut == 'en cours' ? 'selected' : ''}>En cours</option>
                                        <option value="terminée" ${param.statut == 'terminée' ? 'selected' : ''}>Terminée</option>
                                        <option value="rejetée" ${param.statut == 'rejetée' ? 'selected' : ''}>Rejetée</option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label text-brown fw-bold">Type de document</label>
                                    <select name="typeDocument" class="form-control border-brown">
                                        <option value="">Tous les types</option>
                                        <c:forEach var="nature" items="${natures}">
                                            <option value="${nature[0]}" ${param.typeDocument == nature[0] ? 'selected' : ''}>${nature[1]}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label text-brown fw-bold">Date de début</label>
                                    <input type="date" name="dateDebut" class="form-control border-brown" value="${param.dateDebut}">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label text-brown fw-bold">Date de fin</label>
                                    <input type="date" name="dateFin" class="form-control border-brown" value="${param.dateFin}">
                                </div>
                                <div class="col-12">
                                    <button type="submit" class="btn text-white border-0" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                                        <i class="bi bi-funnel me-1"></i>Filtrer
                                    </button>
                                    <a href="${pageContext.request.contextPath}/delegue" class="btn btn-outline-brown">
                                        <i class="bi bi-arrow-clockwise me-1"></i>Réinitialiser
                                    </a>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Liste des demandes -->
                    <div class="card border-0 shadow-sm">
                        <div class="card-header text-white" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                            <h6 class="mb-0">
                                <i class="bi bi-list-ul me-2"></i>Liste des Demandes
                            </h6>
                        </div>
                        <div class="card-body p-0">
                            <c:if test="${empty demandes}">
                                <div class="text-center py-5">
                                    <i class="bi bi-inbox display-1 text-brown-light"></i>
                                    <h5 class="text-brown-dark mt-3">Aucune demande trouvée</h5>
                                    <p class="text-brown-light">Aucune demande ne correspond aux critères de recherche.</p>
                                </div>
                            </c:if>

                            <c:if test="${not empty demandes}">
                                <div class="table-responsive">
                                    <table class="table table-hover mb-0">
                                        <thead class="text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                                            <tr>
                                                <th>Réf.</th>
                                                <th>Date</th>
                                                <th>Document</th>
                                                <th>Personne concernée</th>
                                                <th>Format</th>
                                                <th>Statut</th>
                                                <th>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="demande" items="${demandes}">
                                                <tr>
                                                    <td class="fw-bold text-brown">#${demande.idDemande}</td>
                                                    <td>
                                                        <fmt:formatDate value="${demande.date}" pattern="dd/MM/yyyy"/>
                                                    </td>
                                                    <td>${demande.natureDocument}</td>
                                                    <td>
                                                        <div class="fw-bold text-brown-dark">${demande.prenom} ${demande.nom}</div>
                                                        <small class="text-brown-light">
                                                            <fmt:formatDate value="${demande.datedenaissance}" pattern="dd/MM/yyyy"/> - ${demande.lieudenaissance}
                                                        </small>
                                                    </td>
                                                    <td>
                                                        <span class="badge text-white" style="background: linear-gradient(135deg, #8b7355 0%, #a08666 100%);">
                                                            <c:choose>
                                                                <c:when test="${demande.format == 'numerique'}">Numérique</c:when>
                                                                <c:when test="${demande.format == 'physique'}">Physique</c:when>
                                                                <c:otherwise>${demande.format}</c:otherwise>
                                                            </c:choose>
                                                        </span>
                                                    </td>
                                                    <td>
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
                                                            <c:when test="${demande.status == 'terminée'}">
                                                                <span class="badge text-white" style="background: linear-gradient(135deg, #198754 0%, #20c997 100%);">
                                                                    <i class="bi bi-check-lg me-1"></i>Terminée
                                                                </span>
                                                            </c:when>
                                                            <c:when test="${demande.status == 'rejetée'}">
                                                                <span class="badge text-white" style="background: linear-gradient(135deg, #dc3545 0%, #e35d6a 100%);">
                                                                    <i class="bi bi-x-circle me-1"></i>Rejetée
                                                                </span>
                                                            </c:when>
                                                        </c:choose>
                                                    </td>
<!-- Dans la colonne Actions de delegue.jsp -->
<td>
    <div class="btn-group">
        <!-- Bouton Vérifier/Détails -->
        <a href="${pageContext.request.contextPath}/detailDemande?id=${demande.idDemande}" 
           class="btn btn-sm text-white" 
           style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
            <i class="bi bi-eye me-1"></i>Vérifier
        </a>
        
        <!-- Menu déroulant pour les actions -->
        <c:if test="${demande.status == 'en attente'}">
            <div class="btn-group">
                <button type="button" class="btn btn-sm text-white dropdown-toggle" 
                        style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);" 
                        data-bs-toggle="dropdown">
                    Actions
                </button>
                <ul class="dropdown-menu">
                    <li>
                        <a class="dropdown-item text-success" 
                           href="${pageContext.request.contextPath}/validerDemande?id=${demande.idDemande}&action=valider">
                            <i class="bi bi-check-circle me-2"></i>Valider la demande
                        </a>
                    </li>
                    <li>
                        <a class="dropdown-item text-warning" 
                           href="${pageContext.request.contextPath}/validerDemande?id=${demande.idDemande}&action=recherche">
                            <i class="bi bi-search me-2"></i>Lancer la recherche
                        </a>
                    </li>
                    <li><hr class="dropdown-divider"></li>
                    <li>
                        <a class="dropdown-item text-danger" 
                           href="${pageContext.request.contextPath}/validerDemande?id=${demande.idDemande}&action=rejeter">
                            <i class="bi bi-x-circle me-2"></i>Rejeter la demande
                        </a>
                    </li>
                </ul>
            </div>
        </c:if>
        
        <c:if test="${demande.status == 'validée'}">
            <a href="${pageContext.request.contextPath}/validerDemande?id=${demande.idDemande}&action=recherche" 
               class="btn btn-sm text-white" 
               style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                <i class="bi bi-search me-1"></i>Lancer recherche
            </a>
        </c:if>
    </div>
</td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:if>
                        </div>
                    </div>
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

.btn-group .btn {
    border-radius: 6px;
    font-size: 0.85rem;
}

.badge {
    font-size: 0.75rem;
    font-weight: 500;
}

.card {
    border-radius: 12px;
    overflow: hidden;
}

.card-header {
    border-radius: 0 !important;
}
</style>
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
                            <i class="bi bi-book me-2"></i>Remplissage des Registres - Serveur Central
                        </h4>
                        <span class="badge text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                            <i class="bi bi-person-check me-1"></i>Chargé de Renseignement
                        </span>
                    </div>
                </div>
                
                <div class="card-body">
                    <!-- Bouton pour créer un nouvel acte -->
                    <div class="row mb-4">
                        <div class="col-12">
                            <button type="button" class="btn text-white mb-3" 
                                    style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);"
                                    data-bs-toggle="modal" data-bs-target="#modalNouvelActe">
                                <i class="bi bi-plus-circle me-2"></i>Créer un Nouvel Acte
                            </button>
                        </div>
                    </div>

                    <!-- Statistiques -->
                    <div class="row mb-4">
                        <div class="col-md-3">
                            <div class="card text-white text-center" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                                <div class="card-body py-3">
                                    <h5 class="mb-1">${totalDemandes}</h5>
                                    <small>À Renseigner</small>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="card text-white text-center" style="background: linear-gradient(135deg, #d4af37 0%, #f4d03f 100%);">
                                <div class="card-body py-3">
                                    <h5 class="mb-1">${enAttente}</h5>
                                    <small>En Attente</small>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="card text-white text-center" style="background: linear-gradient(135deg, #198754 0%, #20c997 100%);">
                                <div class="card-body py-3">
                                    <h5 class="mb-1">${completes}</h5>
                                    <small>Complétées</small>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="card text-white text-center" style="background: linear-gradient(135deg, #dc3545 0%, #e35d6a 100%);">
                                <div class="card-body py-3">
                                    <h5 class="mb-1">${nonTrouves}</h5>
                                    <small>Non Trouvées</small>
                                </div>
                            </div>
                        </div>
                    </div>
<!-- Section d'erreur API -->
<c:if test="${not empty error}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
        <i class="bi bi-exclamation-triangle me-2"></i>
        ${error}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>

<!-- Section succès -->
<c:if test="${not empty success}">
    <div class="alert alert-success alert-dismissible fade show" role="alert">
        <i class="bi bi-check-circle me-2"></i>
        ${success}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
</c:if>
                    <!-- Filtres -->
                    <div class="card mb-4 border-0 shadow-sm">
                        <div class="card-body">
                            <form method="get" class="row g-3">
                                <div class="col-md-3">
                                    <label class="form-label fw-bold">Type d'acte</label>
                                    <select name="typeActe" class="form-control">
                                        <option value="">Tous les types</option>
                                        <option value="naissance" ${param.typeActe == 'naissance' ? 'selected' : ''}>Naissance</option>
                                        <option value="mariage" ${param.typeActe == 'mariage' ? 'selected' : ''}>Mariage</option>
                                        <option value="deces" ${param.typeActe == 'deces' ? 'selected' : ''}>Décès</option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label fw-bold">Statut</label>
                                    <select name="statut" class="form-control">
                                        <option value="">Tous les statuts</option>
                                        <option value="en_attente" ${param.statut == 'en_attente' ? 'selected' : ''}>En attente</option>
                                        <option value="complete" ${param.statut == 'complete' ? 'selected' : ''}>Complétée</option>
                                        <option value="non_trouve" ${param.statut == 'non_trouve' ? 'selected' : ''}>Non trouvée</option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label fw-bold">Date de début</label>
                                    <input type="date" name="dateDebut" class="form-control" value="${param.dateDebut}">
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label fw-bold">Date de fin</label>
                                    <input type="date" name="dateFin" class="form-control" value="${param.dateFin}">
                                </div>
                                <div class="col-12">
                                    <button type="submit" class="btn text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                                        <i class="bi bi-funnel me-1"></i>Filtrer
                                    </button>
                                    <a href="${pageContext.request.contextPath}/remplir-registre" class="btn btn-outline-secondary">
                                        <i class="bi bi-arrow-clockwise me-1"></i>Réinitialiser
                                    </a>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Liste des demandes à renseigner -->
                    <div class="card border-0 shadow-sm">
                        <div class="card-header text-white" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                            <h6 class="mb-0">
                                <i class="bi bi-list-ul me-2"></i>Demandes à Renseigner
                            </h6>
                        </div>
                        <div class="card-body p-0">
                            <c:if test="${empty demandes}">
                                <div class="text-center py-5">
                                    <i class="bi bi-inbox display-1 text-muted"></i>
                                    <h5 class="text-muted mt-3">Aucune demande à renseigner</h5>
                                    <p class="text-muted">Toutes les demandes ont été traitées ou aucune ne correspond aux critères.</p>
                                </div>
                            </c:if>

                            <c:if test="${not empty demandes}">
                                <div class="table-responsive">
                                    <table class="table table-hover mb-0">
                                        <thead class="text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                                            <tr>
                                                <th>Réf.</th>
                                                <th>Date Réception</th>
                                                <th>Type Acte</th>
                                                <th>Personne concernée</th>
                                                <th>Naissance</th>
                                                <th>Parents</th>
                                                <th>Priorité</th>
                                                <th>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="demande" items="${demandes}">
                                                <tr>
                                                    <td class="fw-bold">#${demande.idRenseignement}</td>
                                                    <td>
                                                        <fmt:formatDate value="${demande.dateReception}" pattern="dd/MM/yyyy"/>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${demande.typeActe == 'naissance'}">
                                                                <span class="badge bg-info">Naissance</span>
                                                            </c:when>
                                                            <c:when test="${demande.typeActe == 'mariage'}">
                                                                <span class="badge bg-success">Mariage</span>
                                                            </c:when>
                                                            <c:when test="${demande.typeActe == 'deces'}">
                                                                <span class="badge bg-dark">Décès</span>
                                                            </c:when>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <div class="fw-bold">${demande.prenom} ${demande.nom}</div>
                                                    </td>
                                                    <td>
                                                        <small>
                                                            <fmt:formatDate value="${demande.dateNaissance}" pattern="dd/MM/yyyy"/><br>
                                                            ${demande.lieuNaissance}
                                                        </small>
                                                    </td>
                                                    <td>
                                                        <small>
                                                            Père: ${demande.prenomPere} ${demande.nomPere}<br>
                                                            Mère: ${demande.prenomMere} ${demande.nomMere}
                                                        </small>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${demande.priorite == 1}">
                                                                <span class="badge bg-danger">Haute</span>
                                                            </c:when>
                                                            <c:when test="${demande.priorite == 2}">
                                                                <span class="badge bg-warning">Moyenne</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge bg-secondary">Basse</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <button type="button" class="btn btn-sm btn-primary" 
                                                                data-bs-toggle="modal" 
                                                                data-bs-target="#modalRenseigner${demande.idRenseignement}">
                                                            <i class="bi bi-pencil me-1"></i>Renseigner
                                                        </button>
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

<!-- Modal pour créer un nouvel acte -->
<div class="modal fade" id="modalNouvelActe" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Créer un Nouvel Acte</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form action="${pageContext.request.contextPath}/remplir-registre" method="post">
                <input type="hidden" name="action" value="nouvel_acte">
                
                <div class="modal-body">
                    <div class="row g-3">
                        <!-- Informations de base -->
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Type d'acte *</label>
                            <select class="form-control" name="type_acte" required>
                                <option value="naissance">Acte de Naissance</option>
                                <option value="mariage">Acte de Mariage</option>
                                <option value="deces">Acte de Décès</option>
                            </select>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Nom *</label>
                            <input type="text" class="form-control" name="nom" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Prénom *</label>
                            <input type="text" class="form-control" name="prenom" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Date de naissance *</label>
                            <input type="date" class="form-control" name="date_naissance" required>
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-bold">Lieu de naissance *</label>
                            <input type="text" class="form-control" name="lieu_naissance" required>
                        </div>

                        <!-- Informations des parents -->
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Nom du père</label>
                            <input type="text" class="form-control" name="nom_pere">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Prénom du père</label>
                            <input type="text" class="form-control" name="prenom_pere">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Nom de la mère</label>
                            <input type="text" class="form-control" name="nom_mere">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Prénom de la mère</label>
                            <input type="text" class="form-control" name="prenom_mere">
                        </div>

                        <!-- Informations du registre -->
                        <div class="col-md-4">
                            <label class="form-label fw-bold">Numéro d'acte *</label>
                            <input type="text" class="form-control" name="numero_acte" required>
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold">Volume</label>
                            <input type="text" class="form-control" name="volume">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label fw-bold">Folio</label>
                            <input type="text" class="form-control" name="folio">
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Année *</label>
                            <input type="text" class="form-control" name="annee" required>
                        </div>
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Commune *</label>
                            <input type="text" class="form-control" name="commune" required>
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-bold">Centre d'état civil</label>
                            <input type="text" class="form-control" name="centre">
                        </div>
                        <div class="col-12">
                            <label class="form-label fw-bold">Observations</label>
                            <textarea class="form-control" name="observations" rows="3"></textarea>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annuler</button>
                    <button type="submit" class="btn text-white" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                        <i class="bi bi-save me-1"></i>Créer l'acte
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Modals pour renseigner les demandes existantes -->
<c:forEach var="demande" items="${demandes}">
<div class="modal fade" id="modalRenseigner${demande.idRenseignement}" tabindex="-1">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Renseigner le Registre - #${demande.idRenseignement}</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <form action="${pageContext.request.contextPath}/remplir-registre" method="post">
                <input type="hidden" name="action" value="renseigner">
                <input type="hidden" name="id_renseignement" value="${demande.idRenseignement}">
                
                <div class="modal-body">
                    <!-- Informations de la demande -->
                    <div class="card mb-3">
                        <div class="card-header bg-light">
                            <h6 class="mb-0">Informations de la Demande</h6>
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-6">
                                    <p><strong>Personne:</strong> ${demande.prenom} ${demande.nom}</p>
                                    <p><strong>Naissance:</strong> 
                                        <fmt:formatDate value="${demande.dateNaissance}" pattern="dd/MM/yyyy"/> 
                                        à ${demande.lieuNaissance}
                                    </p>
                                </div>
                                <div class="col-md-6">
                                    <p><strong>Père:</strong> ${demande.prenomPere} ${demande.nomPere}</p>
                                    <p><strong>Mère:</strong> ${demande.prenomMere} ${demande.nomMere}</p>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Formulaire de renseignement -->
                    <div class="card">
                        <div class="card-header bg-light">
                            <h6 class="mb-0">Informations du Registre</h6>
                        </div>
                        <div class="card-body">
                            <div class="row g-3">
                                <div class="col-md-4">
                                    <label class="form-label fw-bold">Numéro d'acte *</label>
                                    <input type="text" class="form-control" name="numero_acte" 
                                           value="${demande.numeroActe}" required>
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label fw-bold">Volume</label>
                                    <input type="text" class="form-control" name="volume" 
                                           value="${demande.volume}">
                                </div>
                                <div class="col-md-4">
                                    <label class="form-label fw-bold">Folio</label>
                                    <input type="text" class="form-control" name="folio" 
                                           value="${demande.folio}">
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Année *</label>
                                    <input type="text" class="form-control" name="annee" 
                                           value="${demande.annee}" required>
                                </div>
                                <div class="col-md-6">
                                    <label class="form-label fw-bold">Commune *</label>
                                    <input type="text" class="form-control" name="commune" 
                                           value="${demande.commune}" required>
                                </div>
                                <div class="col-12">
                                    <label class="form-label fw-bold">Centre d'état civil</label>
                                    <input type="text" class="form-control" name="centre" 
                                           value="${demande.centre}">
                                </div>
                                <div class="col-12">
                                    <label class="form-label fw-bold">Statut *</label>
                                    <select class="form-control" name="statut" required>
                                        <option value="en_attente" ${demande.statut == 'en_attente' ? 'selected' : ''}>En attente</option>
                                        <option value="complete" ${demande.statut == 'complete' ? 'selected' : ''}>Complétée</option>
                                        <option value="non_trouve" ${demande.statut == 'non_trouve' ? 'selected' : ''}>Non trouvée</option>
                                    </select>
                                </div>
                                <div class="col-12">
                                    <label class="form-label fw-bold">Observations</label>
                                    <textarea class="form-control" name="observations" rows="3">${demande.observations}</textarea>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Annuler</button>
                    <button type="submit" class="btn text-white" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                        <i class="bi bi-save me-1"></i>Enregistrer
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
</c:forEach>

<style>
.table th {
    border: none;
    font-weight: 600;
    font-size: 0.9rem;
}

.table td {
    vertical-align: middle;
}

.btn {
    border-radius: 6px;
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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div class="container-fluid">
    <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
        <h1 class="h2"><i class="fas fa-list me-2"></i>Mes Demandes</h1>
        <div class="btn-toolbar mb-2 mb-md-0">
            <span class="badge bg-primary fs-6">Total: ${totalDemandes} demande(s)</span>
        </div>
    </div>

    <!-- Messages -->
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="fas fa-exclamation-triangle me-2"></i>${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <c:if test="${not empty success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="fas fa-check-circle me-2"></i>${success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <!-- Statistiques -->
    <div class="row mb-4">
        <div class="col-md-2">
            <div class="card bg-primary text-white">
                <div class="card-body text-center p-3">
                    <h4 class="card-title mb-1">${stats['en attente'] != null ? stats['en attente'] : 0}</h4>
                    <small>En attente</small>
                </div>
            </div>
        </div>
        <div class="col-md-2">
            <div class="card bg-success text-white">
                <div class="card-body text-center p-3">
                    <h4 class="card-title mb-1">${stats['validée'] != null ? stats['validée'] : 0}</h4>
                    <small>Validées</small>
                </div>
            </div>
        </div>
        <div class="col-md-2">
            <div class="card bg-info text-white">
                <div class="card-body text-center p-3">
                    <h4 class="card-title mb-1">${stats['recherché'] != null ? stats['recherché'] : 0}</h4>
                    <small>En recherche</small>
                </div>
            </div>
        </div>
        <div class="col-md-2">
            <div class="card bg-warning text-white">
                <div class="card-body text-center p-3">
                    <h4 class="card-title mb-1">${stats['en cours'] != null ? stats['en cours'] : 0}</h4>
                    <small>En cours</small>
                </div>
            </div>
        </div>
        <div class="col-md-2">
            <div class="card bg-secondary text-white">
                <div class="card-body text-center p-3">
                    <h4 class="card-title mb-1">${stats['terminée'] != null ? stats['terminée'] : 0}</h4>
                    <small>Terminées</small>
                </div>
            </div>
        </div>
        <div class="col-md-2">
            <div class="card bg-danger text-white">
                <div class="card-body text-center p-3">
                    <h4 class="card-title mb-1">${stats['rejetée'] != null ? stats['rejetée'] : 0}</h4>
                    <small>Rejetées</small>
                </div>
            </div>
        </div>
    </div>

    <!-- Liste des demandes -->
    <div class="card">
        <div class="card-header">
            <h5 class="card-title mb-0">
                <i class="fas fa-history me-2"></i>Historique des demandes
            </h5>
        </div>
        <div class="card-body">
            <c:choose>
                <c:when test="${empty demandes}">
                    <div class="text-center py-5">
                        <i class="fas fa-inbox fa-3x text-muted mb-3"></i>
                        <h4 class="text-muted">Aucune demande trouvée</h4>
                        <p class="text-muted">Vous n'avez pas encore effectué de demande.</p>
                        <a href="documents" class="btn btn-primary">
                            <i class="fas fa-plus me-2"></i>Faire une nouvelle demande
                        </a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-hover">
                            <thead class="table-light">
                                <tr>
                                    <th>Référence</th>
                                    <th>Document</th>
                                    <th>Date</th>
                                    <th>Format</th>
                                    <th>Statut</th>
                                    <th>Paiement</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="demande" items="${demandes}">
                                    <tr>
                                        <td>
                                            <strong>#${demande.idDemande}</strong>
                                            <c:if test="${not empty demande.qrCode}">
                                                <i class="fas fa-qrcode text-info ms-1" title="QR Code disponible"></i>
                                            </c:if>
                                        </td>
                                        <td>
                                            <div><strong>${demande.nomDocument}</strong></div>
                                            <small class="text-muted">${demande.prenom} ${demande.nom}</small>
                                        </td>
                                        <td>
                                            <fmt:formatDate value="${demande.date}" pattern="dd/MM/yyyy"/>
                                        </td>
                                        <td>
                                            <span class="badge bg-secondary">${demande.format}</span>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${demande.status == 'en cours'}">
                                                    <span class="badge bg-success">Validée</span>
                                                </c:when>
                                                <c:when test="${demande.status == 'en attente'}">
                                                    <span class="badge bg-warning">En attente</span>
                                                </c:when>
                                                <c:when test="${demande.status == 'recherché'}">
                                                    <span class="badge bg-info">En recherche</span>
                                                </c:when>
                                                <c:when test="${demande.status == 'en cours'}">
                                                    <span class="badge bg-primary">En cours</span>
                                                </c:when>
                                                <c:when test="${demande.status == 'terminée'}">
                                                    <span class="badge bg-secondary">Terminée</span>
                                                </c:when>
                                                <c:when test="${demande.status == 'rejetée'}">
                                                    <span class="badge bg-danger">Rejetée</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">${demande.status}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${demande.paiementRequis}">
                                                    <span class="badge bg-warning">Paiement requis</span>
                                                </c:when>
                                                <c:when test="${demande.idpaiement != null}">
                                                    <span class="badge bg-success">Payé</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">-</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div class="btn-group btn-group-sm">
                                                <a href="suividemande?iddemande=${demande.idDemande}" 
                                                   class="btn btn-outline-primary"
                                                   title="Suivi de la demande">
                                                    <i class="fas fa-tasks"></i> Suivi
                                                </a>
                                                
                                                <c:if test="${demande.paiementRequis}">
                                                    <a href="paiement?iddemande=${demande.idDemande}" 
                                                       class="btn btn-outline-success"
                                                       title="Procéder au paiement">
                                                        <i class="fas fa-credit-card"></i> Payer
                                                    </a>
                                                </c:if>
                                            </div>
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
</div>
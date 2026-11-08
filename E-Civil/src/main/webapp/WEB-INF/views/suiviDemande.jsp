<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div class="container-fluid">
    <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
        <h1 class="h2">
            <i class="fas fa-tasks me-2"></i>Suivi de la demande #${demande.idDemande}
        </h1>
        <div class="btn-toolbar mb-2 mb-md-0">
            <a href="mesdemandes" class="btn btn-secondary">
                <i class="fas fa-arrow-left me-2"></i>Retour
            </a>
        </div>
    </div>

    <!-- Informations de la demande -->
    <div class="row mb-4">
        <div class="col-md-8">
            <div class="card">
                <div class="card-header">
                    <h5 class="card-title mb-0">
                        <i class="fas fa-info-circle me-2"></i>Informations de la demande
                    </h5>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <strong>Document:</strong> ${demande.nomDocument}<br>
                            <strong>Référence:</strong> #${demande.idDemande}<br>
                            <strong>Date de demande:</strong> <fmt:formatDate value="${demande.date}" pattern="dd/MM/yyyy à HH:mm"/><br>
                            <strong>Format:</strong> ${demande.format}
                        </div>
                        <div class="col-md-6">
                            <strong>Demandeur:</strong> ${demande.prenom} ${demande.nom}<br>
                            <strong>Statut:</strong> 
                            <span class="badge 
                                <c:choose>
                                    <c:when test="${demande.status == 'validée'}">bg-success</c:when>
                                    <c:when test="${demande.status == 'en attente'}">bg-warning</c:when>
                                    <c:when test="${demande.status == 'recherché'}">bg-info</c:when>
                                    <c:when test="${demande.status == 'en cours'}">bg-primary</c:when>
                                    <c:when test="${demande.status == 'terminée'}">bg-secondary</c:when>
                                    <c:when test="${demande.status == 'rejetée'}">bg-danger</c:when>
                                    <c:otherwise>bg-secondary</c:otherwise>
                                </c:choose>">
                                ${demande.status}
                            </span><br>
                            <c:if test="${demande.idpaiement != null}">
                                <strong>Paiement:</strong> 
                                <span class="badge bg-success">Payé</span>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Historique des statuts -->
    <div class="card">
        <div class="card-header">
            <h5 class="card-title mb-0">
                <i class="fas fa-history me-2"></i>Historique du statut
            </h5>
        </div>
        <div class="card-body">
            <c:choose>
                <c:when test="${empty historique}">
                    <div class="text-center py-4 text-muted">
                        <i class="fas fa-clock fa-2x mb-3"></i>
                        <p>Aucun historique disponible pour le moment.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="timeline">
                        <c:forEach var="etape" items="${historique}" varStatus="loop">
                            <div class="timeline-item ${loop.first ? 'current' : ''} ${etape.statut == 'terminée' ? 'active' : ''}">
                                <div class="card mb-3">
                                    <div class="card-body">
                                        <div class="d-flex justify-content-between">
                                            <h6 class="card-title">
                                                <c:choose>
                                                    <c:when test="${etape.statut == 'validée'}">
                                                        <i class="fas fa-check-circle text-success me-2"></i>
                                                    </c:when>
                                                    <c:when test="${etape.statut == 'en attente'}">
                                                        <i class="fas fa-clock text-warning me-2"></i>
                                                    </c:when>
                                                    <c:when test="${etape.statut == 'recherché'}">
                                                        <i class="fas fa-search text-info me-2"></i>
                                                    </c:when>
                                                    <c:when test="${etape.statut == 'en cours'}">
                                                        <i class="fas fa-spinner text-primary me-2"></i>
                                                    </c:when>
                                                    <c:when test="${etape.statut == 'terminée'}">
                                                        <i class="fas fa-flag-checkered text-secondary me-2"></i>
                                                    </c:when>
                                                    <c:when test="${etape.statut == 'rejetée'}">
                                                        <i class="fas fa-times-circle text-danger me-2"></i>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <i class="fas fa-circle text-primary me-2"></i>
                                                    </c:otherwise>
                                                </c:choose>
                                                ${etape.statut}
                                            </h6>
                                            <small class="text-muted">
                                                <fmt:formatDate value="${etape.date}" pattern="dd/MM/yyyy HH:mm"/>
                                            </small>
                                        </div>
                                        <c:if test="${not empty etape.commentaire}">
                                            <p class="card-text mt-2">${etape.commentaire}</p>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<style>
.timeline { position: relative; padding-left: 30px; }
.timeline::before { content: ''; position: absolute; left: 15px; top: 0; bottom: 0; width: 2px; background: #007bff; }
.timeline-item { position: relative; margin-bottom: 20px; }
.timeline-item::before { content: ''; position: absolute; left: -23px; top: 5px; width: 12px; height: 12px; border-radius: 50%; background: #007bff; }
.timeline-item.active::before { background: #28a745; }
.timeline-item.current::before { background: #ffc107; border: 2px solid #007bff; }
</style>
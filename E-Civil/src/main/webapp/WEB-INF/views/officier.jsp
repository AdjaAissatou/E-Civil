<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="container-fluid py-4">
    <!-- En-tête -->
    <div class="row mb-4">
        <div class="col-12">
            <div class="card shadow-sm border-0 rounded-3 bg-brown text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #3b2a1f 100%);">
                <div class="card-body py-4">
                    <div class="row align-items-center">
                        <div class="col-md-8">
                            <h2 class="mb-1">
                                <i class="bi bi-shield-lock me-2"></i>Panneau Officier de l'État Civil
                            </h2>
                            <p class="mb-0 opacity-75">
                                Génération des actes officiels et signature électronique
                            </p>
                        </div>
                        <div class="col-md-4 text-end">
                            <div class="d-flex justify-content-end align-items-center">
                                <div class="me-3">
                                    <small class="opacity-75">Officier connecté</small>
                                    <div class="fw-bold">${sessionScope.compte.personne.prenom} ${sessionScope.compte.personne.nom}</div>
                                </div>
                                <div class="bg-beige text-brown rounded-circle d-flex align-items-center justify-content-center" 
                                     style="width: 50px; height: 50px;">
                                    <i class="bi bi-person-gear fs-4"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Cartes de statistiques SIMPLIFIÉES -->
    <div class="row mb-4">
        <div class="col-xl-3 col-md-6 mb-3">
            <div class="card text-white text-center stat-card" style="background: linear-gradient(135deg, #5c3d2e 0%, #3b2a1f 100%);">
                <div class="card-body py-3">
                    <i class="bi bi-files display-6 mb-2 opacity-75"></i>
                    <h4 class="mb-1">${totalDemandes}</h4>
                    <small>Total Demandes</small>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6 mb-3">
            <div class="card text-white text-center stat-card" style="background: linear-gradient(135deg, #a67c52 0%, #8b6b47 100%);">
                <div class="card-body py-3">
                    <i class="bi bi-file-earmark-pdf display-6 mb-2 opacity-75"></i>
                    <h4 class="mb-1">${demandesTrouvees}</h4>
                    <small>Actes à Générer</small>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6 mb-3">
            <div class="card text-white text-center stat-card" style="background: linear-gradient(135deg, #28a745 0%, #20c997 100%);">
                <div class="card-body py-3">
                    <i class="bi bi-pen-fill display-6 mb-2 opacity-75"></i>
                    <h4 class="mb-1">${demandesSignees}</h4>
                    <small>Actes Signés</small>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6 mb-3">
            <div class="card text-white text-center stat-card" style="background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);">
                <div class="card-body py-3">
                    <i class="bi bi-x-circle display-6 mb-2 opacity-75"></i>
                    <h4 class="mb-1">${demandesRejetees}</h4>
                    <small>Actes Rejetés</small>
                </div>
            </div>
        </div>
    </div>

    <!-- Filtres SIMPLIFIÉS -->
    <div class="filters-section shadow-sm bg-beige-light border-brown rounded-3">
        <form method="get" action="${pageContext.request.contextPath}/officier">
            <div class="row g-3 align-items-end">
                <div class="col-md-3">
                    <label class="form-label text-brown fw-bold">Statut</label>
                    <select name="statut" class="form-select border-brown">
                        <option value="">Tous les actes à signer</option>
                        <option value="trouvé" ${param.statut == 'trouvé' ? 'selected' : ''}>Actes à signer</option>
                        <option value="signé" ${param.statut == 'signé' ? 'selected' : ''}>Actes signés</option>
                        <option value="rejetée" ${param.statut == 'rejetée' ? 'selected' : ''}>Actes rejetés</option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label text-brown fw-bold">Type d'acte</label>
                    <select name="typeDocument" class="form-select border-brown">
                        <option value="">Tous les types</option>
                        <c:forEach var="nature" items="${natures}">
                            <option value="${nature[1]}" ${param.typeDocument == nature[1] ? 'selected' : ''}>${nature[1]}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label text-brown fw-bold">Format</label>
                    <select name="format" class="form-select border-brown">
                        <option value="">Tous</option>
                        <option value="numerique" ${param.format == 'numerique' ? 'selected' : ''}>Numérique</option>
                        <option value="physique" ${param.format == 'physique' ? 'selected' : ''}>Physique</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label text-brown fw-bold">Date</label>
                    <input type="date" name="dateDemande" class="form-control border-brown" value="${param.dateDemande}">
                </div>
                <div class="col-md-2">
                    <div class="d-grid">
                        <button type="submit" class="btn btn-brown text-white">
                            <i class="bi bi-funnel me-1"></i>Filtrer
                        </button>
                    </div>
                </div>
            </div>
        </form>
    </div>

    <!-- Liste des demandes SIMPLIFIÉE -->
    <div class="card border-0 shadow-sm border-brown">
        <div class="card-header text-white py-3" style="background: linear-gradient(135deg, #5c3d2e 0%, #3b2a1f 100%);">
            <div class="d-flex justify-content-between align-items-center">
                <h5 class="mb-0">
                    <i class="bi bi-file-earmark-pdf me-2"></i>Actes à Générer et Signer
                    <span class="badge bg-beige text-brown ms-2">${demandes.size()} actes</span>
                </h5>
                <div class="text-white opacity-75">
                    <i class="bi bi-info-circle me-1"></i>
                    <small>Générez les PDF officiels et signez-les</small>
                </div>
            </div>
        </div>
        
        <div class="card-body p-0">
            <c:choose>
                <c:when test="${empty demandes}">
                    <div class="text-center py-5">
                        <i class="bi bi-inbox display-1 text-brown-light mb-3"></i>
                        <h4 class="text-brown-dark">Aucun acte à générer</h4>
                        <p class="text-brown-light">Aucun acte ne correspond aux critères de recherche.</p>
                    </div>
                </c:when>
                
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead>
                                <tr>
                                    <th class="text-brown-dark" style="background: linear-gradient(135deg, #5c3d2e 0%, #3b2a1f 100%); color: white;">Référence</th>
                                    <th class="text-brown-dark" style="background: linear-gradient(135deg, #5c3d2e 0%, #3b2a1f 100%); color: white;">Date</th>
                                    <th class="text-brown-dark" style="background: linear-gradient(135deg, #5c3d2e 0%, #3b2a1f 100%); color: white;">Type d'acte</th>
                                    <th class="text-brown-dark" style="background: linear-gradient(135deg, #5c3d2e 0%, #3b2a1f 100%); color: white;">Personne</th>
                                    <th class="text-brown-dark" style="background: linear-gradient(135deg, #5c3d2e 0%, #3b2a1f 100%); color: white;">Statut</th>
                                    <th class="text-brown-dark text-center" style="background: linear-gradient(135deg, #5c3d2e 0%, #3b2a1f 100%); color: white;">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="demande" items="${demandes}">
                                    <tr>
                                        <td class="fw-bold text-brown-dark">#${demande.idDemande}</td>
                                        <td>
                                            <fmt:formatDate value="${demande.date}" pattern="dd/MM/yyyy"/>
                                        </td>
                                        <td>
                                            <div class="fw-bold text-brown-dark">${demande.natureDocument}</div>
                                        </td>
                                        <td>
                                            <div class="fw-bold text-brown-dark">${demande.prenom} ${demande.nom}</div>
                                            <small class="text-brown-light">
                                                <fmt:formatDate value="${demande.datedenaissance}" pattern="dd/MM/yyyy"/>
                                            </small>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${demande.status == 'trouvé' || demande.status == 'recherché'}">
                                                    <span class="badge status-badge bg-warning text-dark">
                                                        <i class="bi bi-file-earmark-pdf me-1"></i>À Générer
                                                    </span>
                                                </c:when>
                                                <c:when test="${demande.status == 'signé'}">
                                                    <span class="badge status-badge bg-success text-white">
                                                        <i class="bi bi-pen-fill me-1"></i>Signé
                                                    </span>
                                                </c:when>
                                                <c:when test="${demande.status == 'rejetée'}">
                                                    <span class="badge status-badge bg-danger text-white">
                                                        <i class="bi bi-x-circle me-1"></i>Rejeté
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge status-badge bg-secondary text-white">
                                                        ${demande.status}
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-center">
                                            <div class="btn-group">
                                                <!-- Générer l'acte PDF OFFICIEL -->
                                                <a href="${pageContext.request.contextPath}/GenererActeServlet?idDemande=${demande.idDemande}" 
                                                   class="btn btn-sm btn-brown text-white"
                                                   target="_blank"
                                                   data-bs-toggle="tooltip" 
                                                   title="Générer l'acte PDF officiel">
                                                    <i class="bi bi-file-earmark-pdf me-1"></i>Générer PDF
                                                </a>
                                                
                                                <!-- Signer l'acte - Pour tous les statuts "trouvé" et "recherché" -->
                                                <c:if test="${demande.status == 'trouvé' || demande.status == 'recherché'}">
                                                    <form method="post" 
                                                          action="${pageContext.request.contextPath}/signerDemande" 
                                                          style="display:inline;">
                                                        <input type="hidden" name="idDemande" value="${demande.idDemande}">
                                                        <button type="submit" 
                                                                class="btn btn-sm btn-success text-white"
                                                                data-bs-toggle="tooltip" 
                                                                title="Signer électroniquement cet acte"
                                                                onclick="return confirm('Signer l\\'acte officiel #${demande.idDemande} ?')">
                                                            <i class="bi bi-pen-fill me-1"></i>Signer
                                                        </button>
                                                    </form>
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

    <!-- Instructions -->
    <div class="row mt-4">
        <div class="col-12">
            <div class="card border-brown bg-beige-light">
                <div class="card-body">
                    <h6 class="text-brown-dark mb-3">
                        <i class="bi bi-info-circle me-2"></i>Instructions pour l'officier
                    </h6>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="d-flex align-items-center mb-2">
                                <div class="bg-brown text-white rounded-circle p-2 me-3">
                                    <i class="bi bi-file-earmark-pdf"></i>
                                </div>
                                <div>
                                    <strong class="text-brown-dark">Générer PDF</strong>
                                    <p class="mb-0 text-brown-light small">Créez l'acte officiel au format PDF</p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="d-flex align-items-center mb-2">
                                <div class="bg-success text-white rounded-circle p-2 me-3">
                                    <i class="bi bi-pen-fill"></i>
                                </div>
                                <div>
                                    <strong class="text-brown-dark">Signer l'acte</strong>
                                    <p class="mb-0 text-brown-light small">Validation électronique définitive</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
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
                    <c:if test="${not empty sessionScope.error}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="bi bi-exclamation-triangle me-2"></i>
                            ${sessionScope.error}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                        <c:remove var="error" scope="session"/>
                    </c:if>

                    <c:if test="${not empty sessionScope.success}">
                        <div class="alert alert-success alert-dismissible fade show" role="alert">
                            <i class="bi bi-check-circle me-2"></i>
                            ${sessionScope.success}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                        <c:remove var="success" scope="session"/>
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
                                    <h5 class="mb-1">${totalActes}</h5>
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

                    <!-- Processus de Renseignement -->
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
<%@ include file="/WEB-INF/views/renseignement_modal.jsp" %>
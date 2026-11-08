<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="container-fluid py-4">
    <div class="row justify-content-center">
        <div class="col-lg-10">
            <div class="card shadow-sm border-0 rounded-3 bg-beige-light">
                <div class="card-header bg-brown text-white py-3" style="background: linear-gradient(135deg,#5c3d2e 0%, #3b2a1f 100%);">
                    <div class="d-flex justify-content-between align-items-center">
                        <h4 class="fw-bold mb-0">
                            <i class="bi bi-cart-plus me-2"></i>Nouvelle Demande de Document d'État Civil
                        </h4>
                        <a href="${pageContext.request.contextPath}/utilisateurlocal" 
                           class="btn btn-sm btn-beige text-brown">
                            <i class="bi bi-arrow-left me-1"></i>Retour
                        </a>
                    </div>
                </div>
                
                <div class="card-body">
                    <!-- Messages d'alerte -->
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="bi bi-exclamation-triangle me-2"></i>${error}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>
                    
                    <c:if test="${not empty success}">
                        <div class="alert alert-success alert-dismissible fade show" role="alert">
                            <i class="bi bi-check-circle me-2"></i>${success}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <!-- Résumé du document -->
                    <div class="card mb-4 bg-beige border-brown">
                        <div class="card-body">
                            <h5 class="text-brown-dark mb-3">
                                <i class="bi bi-file-earmark-text me-2"></i>Document sélectionné
                            </h5>
                            <c:if test="${not empty nature}">
                                <div class="row">
                                    <div class="col-md-6">
                                        <p class="mb-2">
                                            <strong class="text-brown">Nom:</strong> 
                                            <span class="text-brown-dark">${nature[1]}</span>
                                        </p>
                                        <p class="mb-2">
                                            <strong class="text-brown">Description:</strong> 
                                            <span class="text-brown-dark">${nature[2]}</span>
                                        </p>
                                        <p class="mb-2">
                                            <strong class="text-brown">Catégorie:</strong> 
                                            <span class="badge bg-brown text-white">${nature[6]}</span>
                                        </p>
                                    </div>
                                    <div class="col-md-6">
                                        <p class="mb-2">
                                            <strong class="text-brown">Tarif:</strong> 
                                            <span class="text-success fw-bold">
                                                <fmt:formatNumber value="${nature[3]}" type="number"/> FCFA
                                            </span>
                                        </p>
                                        <p class="mb-2">
                                            <strong class="text-brown">Délai de traitement:</strong> 
                                            <span class="text-warning fw-bold">${nature[5]} jours</span>
                                        </p>
                                        <p class="mb-2">
                                            <strong class="text-brown">Validité:</strong> 
                                            <span class="text-info fw-bold">${nature[4]} jours</span>
                                        </p>
                                    </div>
                                </div>
                                
                                <!-- Afficher les sections requises de manière plus détaillée -->
                                <c:set var="temoins" value="${nature[7]}"/>
                                <c:set var="parents" value="${nature[8]}"/>
                                <c:set var="conjoints" value="${nature[9]}"/>
                                <c:set var="medecin" value="${nature[10]}"/>
                                <c:set var="mentionMarginale" value="${nature[11]}"/>
                                <c:set var="causeDeces" value="${nature[12]}"/>
                                
                                <c:if test="${temoins || parents || conjoints || medecin || mentionMarginale || causeDeces}">
                                    <div class="mt-3 pt-3 border-top border-brown">
                                        <strong class="text-brown">Informations supplémentaires requises:</strong>
                                        <div class="mt-2">
                                            <c:if test="${temoins}">
                                                <span class="badge bg-primary me-1 mb-1">
                                                    <i class="bi bi-people me-1"></i>Témoins (2 minimum)
                                                </span>
                                            </c:if>
                                            <c:if test="${parents}">
                                                <span class="badge bg-primary me-1 mb-1">
                                                    <i class="bi bi-person-heart me-1"></i>Parents
                                                </span>
                                            </c:if>
                                            <c:if test="${conjoints}">
                                                <span class="badge bg-primary me-1 mb-1">
                                                    <i class="bi bi-heart me-1"></i>Conjoint
                                                </span>
                                            </c:if>
                                            <c:if test="${medecin}">
                                                <span class="badge bg-primary me-1 mb-1">
                                                    <i class="bi bi-heart-pulse me-1"></i>Médecin
                                                </span>
                                            </c:if>
                                            <c:if test="${mentionMarginale}">
                                                <span class="badge bg-primary me-1 mb-1">
                                                    <i class="bi bi-pencil-square me-1"></i>Mention marginale
                                                </span>
                                            </c:if>
                                            <c:if test="${causeDeces}">
                                                <span class="badge bg-primary me-1 mb-1">
                                                    <i class="bi bi-file-medical me-1"></i>Cause décès
                                                </span>
                                            </c:if>
                                        </div>
                                        <small class="text-brown-light mt-2 d-block">
                                            <i class="bi bi-info-circle me-1"></i>
                                            Les champs marqués d'un * sont obligatoires pour ce type de document.
                                        </small>
                                    </div>
                                </c:if>
                            </c:if>
                        </div>
                    </div>

                    <!-- Formulaire de demande -->
                    <form action="${pageContext.request.contextPath}/demande" method="post" id="formDemande">
                        <input type="hidden" name="natureId" value="${nature[0]}">
                        <input type="hidden" name="action" value="creerDemande">
                        
                        <!-- Section choix du destinataire -->
                        <div class="card mb-4 border-brown">
                            <div class="card-header bg-brown text-white">
                                <h5 class="mb-0">
                                    <i class="bi bi-person-check me-2"></i>Cette demande est pour
                                </h5>
                            </div>
                            <div class="card-body">
                                <div class="row">
                                    <div class="col-md-6">
                                        <div class="form-check card-option">
                                            <input class="form-check-input" type="radio" name="destinataire" 
                                                   id="destinataireMoi" value="moi" checked>
                                            <label class="form-check-label card-option-label" for="destinataireMoi">
                                                <div class="d-flex align-items-center">
                                                    <i class="bi bi-person-check text-success fs-4 me-3"></i>
                                                    <div>
                                                        <strong class="d-block">Moi-même</strong>
                                                        <small class="text-muted">Utiliser mes informations personnelles</small>
                                                    </div>
                                                </div>
                                            </label>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="form-check card-option">
                                            <input class="form-check-input" type="radio" name="destinataire" 
                                                   id="destinataireAutre" value="autre">
                                            <label class="form-check-label card-option-label" for="destinataireAutre">
                                                <div class="d-flex align-items-center">
                                                    <i class="bi bi-person-plus text-primary fs-4 me-3"></i>
                                                    <div>
                                                        <strong class="d-block">Une autre personne</strong>
                                                        <small class="text-muted">Renseigner les informations manuellement</small>
                                                    </div>
                                                </div>
                                            </label>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Section informations de la personne concernée -->
                        <div class="card mb-4 border-brown" id="sectionPersonne">
                            <div class="card-header bg-brown text-white">
                                <h5 class="mb-0">
                                    <i class="bi bi-person-vcard me-2"></i>Informations de la personne concernée
                                </h5>
                            </div>
                            <div class="card-body">
                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="prenom" class="form-label text-brown fw-bold">Prénom *</label>
                                        <input type="text" class="form-control border-brown" id="prenom" name="prenom" 
                                               value="${sessionScope.compte.personne.prenom}" required>
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="nom" class="form-label text-brown fw-bold">Nom *</label>
                                        <input type="text" class="form-control border-brown" id="nom" name="nom" 
                                               value="${sessionScope.compte.personne.nom}" required>
                                    </div>
                                </div>
                                
                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="datedenaissance" class="form-label text-brown fw-bold">Date de naissance *</label>
                                        <input type="date" class="form-control border-brown" id="datedenaissance" name="datedenaissance" 
                                               value="${sessionScope.compte.personne.datedenaissance}" required>
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="lieudenaissance" class="form-label text-brown fw-bold">Lieu de naissance *</label>
                                        <input type="text" class="form-control border-brown" id="lieudenaissance" name="lieudenaissance" 
                                               value="${sessionScope.compte.personne.lieudenaissance}" required>
                                    </div>
                                </div>
                                
                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="sexe" class="form-label text-brown fw-bold">Sexe *</label>
										 <select class="form-control border-brown" id="sexe" name="sexe" required>
										    <option value="">Sélectionnez</option>
										    <option value="M" <c:if test="${sessionScope.compte.personne.sexe == 'M'}">selected</c:if>>Masculin</option>
										    <option value="F" <c:if test="${sessionScope.compte.personne.sexe == 'F'}">selected</c:if>>Féminin</option>
										</select>
                                            
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="profession" class="form-label text-brown fw-bold">Profession</label>
                                        <input type="text" class="form-control border-brown" id="profession" name="profession" 
                                               value="${sessionScope.compte.personne.profession}">
                                    </div>
                                </div>
                                
                                <div class="mb-3">
                                    <label for="residance" class="form-label text-brown fw-bold">Résidence actuelle</label>
                                    <input type="text" class="form-control border-brown" id="residance" name="residance" 
                                           value="${sessionScope.compte.personne.residance}">
                                </div>
                                
                                <div class="form-text text-brown-light">
                                    <i class="bi bi-info-circle me-1"></i>
                                    Ces informations sont nécessaires pour rechercher dans les registres d'état civil.
                                </div>
                            </div>
                        </div>

                        <!-- Section variétés spécifiques au document -->
                        <c:if test="${not empty nature}">
                            <!-- Section Témoins -->
                            <c:if test="${temoins}">
                                <div class="card mb-4 border-brown">
                                    <div class="card-header bg-brown text-white">
                                        <h5 class="mb-0">
                                            <i class="bi bi-people me-2"></i>Informations sur les témoins
                                        </h5>
                                    </div>
                                    <div class="card-body">
                                        <!-- Témoin 1 -->
                                        <div class="row mb-3">
                                            <div class="col-md-6">
                                                <label for="temoin1_nom" class="form-label text-brown fw-bold">Nom du témoin 1 *</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="temoin1_nom" name="temoin1_nom" required>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="temoin1_prenom" class="form-label text-brown fw-bold">Prénom du témoin 1 *</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="temoin1_prenom" name="temoin1_prenom" required>
                                            </div>
                                        </div>
                                        <!-- Témoin 2 -->
                                        <div class="row">
                                            <div class="col-md-6">
                                                <label for="temoin2_nom" class="form-label text-brown fw-bold">Nom du témoin 2</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="temoin2_nom" name="temoin2_nom">
                                            </div>
                                            <div class="col-md-6">
                                                <label for="temoin2_prenom" class="form-label text-brown fw-bold">Prénom du témoin 2</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="temoin2_prenom" name="temoin2_prenom">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>

                            <!-- Section Parents -->
                            <c:if test="${parents}">
                                <div class="card mb-4 border-brown">
                                    <div class="card-header bg-brown text-white">
                                        <h5 class="mb-0">
                                            <i class="bi bi-person-heart me-2"></i>Informations sur les parents
                                        </h5>
                                    </div>
                                    <div class="card-body">
                                        <!-- Père -->
                                        <h6 class="text-brown-dark mb-3">Père *</h6>
                                        <div class="row mb-3">
                                            <div class="col-md-6">
                                                <label for="pere_nom" class="form-label text-brown fw-bold">Nom du père *</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="pere_nom" name="pere_nom" required>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="pere_prenom" class="form-label text-brown fw-bold">Prénom du père *</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="pere_prenom" name="pere_prenom" required>
                                            </div>
                                        </div>
                                        <div class="row mb-4">
                                            <div class="col-md-6">
                                                <label for="pere_profession" class="form-label text-brown fw-bold">Profession du père</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="pere_profession" name="pere_profession">
                                            </div>
                                            <div class="col-md-6">
                                                <label for="pere_residance" class="form-label text-brown fw-bold">Résidence du père</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="pere_residance" name="pere_residance">
                                            </div>
                                        </div>

                                        <!-- Mère -->
                                        <h6 class="text-brown-dark mb-3">Mère *</h6>
                                        <div class="row mb-3">
                                            <div class="col-md-6">
                                                <label for="mere_nom" class="form-label text-brown fw-bold">Nom de la mère *</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="mere_nom" name="mere_nom" required>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="mere_prenom" class="form-label text-brown fw-bold">Prénom de la mère *</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="mere_prenom" name="mere_prenom" required>
                                            </div>
                                        </div>
                                        <div class="row">
                                            <div class="col-md-6">
                                                <label for="mere_profession" class="form-label text-brown fw-bold">Profession de la mère</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="mere_profession" name="mere_profession">
                                            </div>
                                            <div class="col-md-6">
                                                <label for="mere_residance" class="form-label text-brown fw-bold">Résidence de la mère</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="mere_residance" name="mere_residance">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>

                            <!-- Section Conjoint -->
                            <c:if test="${conjoints}">
                                <div class="card mb-4 border-brown">
                                    <div class="card-header bg-brown text-white">
                                        <h5 class="mb-0">
                                            <i class="bi bi-heart me-2"></i>Informations sur le conjoint
                                        </h5>
                                    </div>
                                    <div class="card-body">
                                        <div class="row mb-3">
                                            <div class="col-md-6">
                                                <label for="conjoint_nom" class="form-label text-brown fw-bold">Nom du conjoint *</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="conjoint_nom" name="conjoint_nom" required
                                                       placeholder="Nom de famille">
                                            </div>
                                            <div class="col-md-6">
                                                <label for="conjoint_prenom" class="form-label text-brown fw-bold">Prénom du conjoint *</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="conjoint_prenom" name="conjoint_prenom" required
                                                       placeholder="Prénom">
                                            </div>
                                        </div>
                                        <div class="row mb-3">
                                            <div class="col-md-6">
                                                <label for="conjoint_date_naissance" class="form-label text-brown fw-bold">Date de naissance *</label>
                                                <input type="date" class="form-control border-brown" 
                                                       id="conjoint_date_naissance" name="conjoint_date_naissance" required>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="conjoint_lieu_naissance" class="form-label text-brown fw-bold">Lieu de naissance *</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="conjoint_lieu_naissance" name="conjoint_lieu_naissance" required
                                                       placeholder="Lieu de naissance">
                                            </div>
                                        </div>
                                        <div class="row">
                                            <div class="col-md-6">
                                                <label for="conjoint_profession" class="form-label text-brown fw-bold">Profession</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="conjoint_profession" name="conjoint_profession"
                                                       placeholder="Profession">
                                            </div>
                                            <div class="col-md-6">
                                                <label for="conjoint_residance" class="form-label text-brown fw-bold">Résidence</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="conjoint_residance" name="conjoint_residance"
                                                       placeholder="Adresse de résidence">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>

                            <!-- Section Médecin -->
                            <c:if test="${medecin}">
                                <div class="card mb-4 border-brown">
                                    <div class="card-header bg-brown text-white">
                                        <h5 class="mb-0">
                                            <i class="bi bi-heart-pulse me-2"></i>Informations du médecin
                                        </h5>
                                    </div>
                                    <div class="card-body">
                                        <div class="row mb-3">
                                            <div class="col-md-6">
                                                <label for="medecin_nom" class="form-label text-brown fw-bold">Nom du médecin *</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="medecin_nom" name="medecin_nom" required
                                                       placeholder="Nom du médecin">
                                            </div>
                                            <div class="col-md-6">
                                                <label for="medecin_prenom" class="form-label text-brown fw-bold">Prénom du médecin *</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="medecin_prenom" name="medecin_prenom" required
                                                       placeholder="Prénom du médecin">
                                            </div>
                                        </div>
                                        <div class="row">
                                            <div class="col-md-12">
                                                <label for="medecin_adresse" class="form-label text-brown fw-bold">Adresse du cabinet</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="medecin_adresse" name="medecin_adresse"
                                                       placeholder="Adresse complète du cabinet médical">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </c:if>

                            <!-- Section Mention Marginale -->
                            <c:if test="${mentionMarginale}">
                                <div class="card mb-4 border-brown">
                                    <div class="card-header bg-brown text-white">
                                        <h5 class="mb-0">
                                            <i class="bi bi-pencil-square me-2"></i>Mention marginale
                                        </h5>
                                    </div>
                                    <div class="card-body">
                                        <div class="mb-3">
                                            <label for="mention_marginale" class="form-label text-brown fw-bold">Type de mention *</label>
                                            <select class="form-control border-brown" id="mention_marginale" name="mention_marginale" required>
                                                <option value="">Sélectionnez le type de mention...</option>
                                                <option value="mariage">Mariage</option>
                                                <option value="divorce">Divorce</option>
                                                <option value="deces">Décès</option>
                                                <option value="rectification">Rectification d'erreur</option>
                                                <option value="jugement">Jugement</option>
                                                <option value="adoption">Adoption</option>
                                                <option value="reconnaissance">Reconnaissance</option>
                                                <option value="autre">Autre mention</option>
                                            </select>
                                        </div>
                                        <div class="mb-3">
                                            <label for="mention_details" class="form-label text-brown fw-bold">Détails de la mention *</label>
                                            <textarea class="form-control border-brown" id="mention_details" name="mention_details" 
                                                      rows="4" required 
                                                      placeholder="Précisez les détails de la mention marginale (date, lieu, références...)"></textarea>
                                        </div>
                                        <div class="form-text text-brown-light">
                                            <i class="bi bi-info-circle me-1"></i>
                                            La mention marginale est une annotation portée en marge de l'acte pour indiquer un événement ultérieur.
                                        </div>
                                    </div>
                                </div>
                            </c:if>

                            <!-- Section Cause de Décès -->
                            <c:if test="${causeDeces}">
                                <div class="card mb-4 border-brown">
                                    <div class="card-header bg-brown text-white">
                                        <h5 class="mb-0">
                                            <i class="bi bi-file-medical me-2"></i>Cause du décès
                                        </h5>
                                    </div>
                                    <div class="card-body">
                                        <div class="mb-3">
                                            <label for="cause_deces" class="form-label text-brown fw-bold">Cause principale du décès *</label>
                                            <input type="text" class="form-control border-brown" 
                                                   id="cause_deces" name="cause_deces" required 
                                                   placeholder="Ex: Arrêt cardiaque, Accident de la route, Maladie...">
                                        </div>
                                        <div class="row mb-3">
                                            <div class="col-md-6">
                                                <label for="date_deces" class="form-label text-brown fw-bold">Date du décès *</label>
                                                <input type="date" class="form-control border-brown" 
                                                       id="date_deces" name="date_deces" required>
                                            </div>
                                            <div class="col-md-6">
                                                <label for="lieu_deces" class="form-label text-brown fw-bold">Lieu du décès *</label>
                                                <input type="text" class="form-control border-brown" 
                                                       id="lieu_deces" name="lieu_deces" required 
                                                       placeholder="Lieu où le décès s'est produit">
                                            </div>
                                        </div>
                                        <div class="form-text text-brown-light">
                                            <i class="bi bi-info-circle me-1"></i>
                                            Ces informations doivent correspondre à celles déclarées à l'état civil.
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                        </c:if>

                        <!-- Section format -->
                        <div class="mb-4">
                            <label class="form-label text-brown fw-bold">
                                <i class="bi bi-printer me-2"></i>Format souhaité
                            </label>
                            <div class="row">
                                <div class="col-md-6">
                                    <div class="form-check card-option">
                                        <input class="form-check-input" type="radio" name="format" 
                                               id="formatNumerique" value="numerique" checked>
                                        <label class="form-check-label card-option-label" for="formatNumerique">
                                            <div class="d-flex align-items-center">
                                                <i class="bi bi-file-earmark-pdf text-primary fs-4 me-3"></i>
                                                <div>
                                                    <strong class="d-block">Format numérique</strong>
                                                    <small class="text-muted">PDF sécurisé</small>
                                                </div>
                                            </div>
                                        </label>
                                    </div>
                                </div>
                                <div class="col-md-6">
                                    <div class="form-check card-option">
                                        <input class="form-check-input" type="radio" name="format" 
                                               id="formatPhysique" value="physique">
                                        <label class="form-check-label card-option-label" for="formatPhysique">
                                            <div class="d-flex align-items-center">
                                                <i class="bi bi-file-earmark-text text-warning fs-4 me-3"></i>
                                                <div>
                                                    <strong class="d-block">Format physique</strong>
                                                    <small class="text-muted">Copie originale</small>
                                                </div>
                                            </div>
                                        </label>
                                    </div>
                                </div>
                            </div>
                        </div>

<!-- Section motif - Version corrigée -->
<div class="mb-4">
    <label for="motifId" class="form-label text-brown fw-bold">
        <i class="bi bi-chat-dots me-2"></i>Motif de la demande *
    </label>
    
    <c:choose>
        <c:when test="${not empty motifs}">
            <!-- Cas normal : motifs prédéfinis disponibles -->
            <select class="form-control border-brown" id="motifId" name="motifId" required>
                <option value="">Sélectionnez un motif...</option>
                <c:forEach var="motif" items="${motifs}">
                    <option value="${motif[0]}" 
                            data-description="${motif[2]}"
                            data-documents="${motif[3]}">
                        ${motif[1]}
                    </option>
                </c:forEach>
            </select>
            
            <!-- Affichage des détails du motif sélectionné -->
            <div id="detailsMotif" class="mt-3 p-3 bg-beige rounded border-brown" style="display: none;">
                <h6 class="text-brown fw-bold mb-2">
                    <i class="bi bi-info-circle me-2"></i>Détails du motif
                </h6>
                <p class="mb-1"><strong>Description:</strong> <span id="motifDescription"></span></p>
                <p class="mb-0"><strong>Documents requis:</strong> <span id="motifDocuments"></span></p>
            </div>
        </c:when>
        
        <c:when test="${aucunMotifPredefini}">
            <!-- NOUVEAU : Cas où aucun motif prédéfini n'est disponible -->
            <div class="alert alert-info">
                <i class="bi bi-info-circle me-2"></i>
                Aucun motif prédéfini n'est disponible pour ce type de document. 
                Veuillez préciser le motif manuellement.
            </div>
            <input type="text" class="form-control border-brown" 
                   id="motifPersonnalise" name="motifPersonnalise" 
                   placeholder="Précisez le motif de votre demande..." required>
            <input type="hidden" id="motifId" name="motifId" value="0">
            <div class="form-text text-brown-light">
                <i class="bi bi-info-circle me-1"></i>
                Décrivez brièvement pourquoi vous avez besoin de ce document.
            </div>
        </c:when>
        
        <c:otherwise>
            <div class="alert alert-warning">
                <i class="bi bi-exclamation-triangle me-2"></i>
                Aucun motif prédéfini disponible pour ce type de document.
            </div>
        </c:otherwise>
    </c:choose>
</div>
                        <!-- Informations de livraison (conditionnel) -->
                        <div id="sectionLivraison" class="mb-4" style="display: none;">
                            <div class="card border-brown">
                                <div class="card-header bg-beige">
                                    <h6 class="mb-0 text-brown">
                                        <i class="bi bi-truck me-2"></i>Adresse de livraison
                                    </h6>
                                </div>
                                <div class="card-body">
                                    <div class="row">
                                        <div class="col-md-6">
                                            <label for="adresse" class="form-label text-brown">Adresse *</label>
                                            <input type="text" class="form-control border-brown" 
                                                   id="adresse" name="adresse" placeholder="Numéro et rue">
                                        </div>
                                        <div class="col-md-6">
                                            <label for="ville" class="form-label text-brown">Ville *</label>
                                            <input type="text" class="form-control border-brown" 
                                                   id="ville" name="ville" placeholder="Ville">
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Informations du demandeur -->
                        <div class="mb-4">
                            <div class="card border-brown">
                                <div class="card-header bg-beige">
                                    <h6 class="mb-0 text-brown">
                                        <i class="bi bi-person me-2"></i>Informations du demandeur
                                    </h6>
                                </div>
                                <div class="card-body">
                                    <div class="row">
                                        <div class="col-md-6">
                                            <p class="mb-2">
                                                <strong>Nom complet:</strong><br>
                                                <span class="text-brown-dark">
                                                    ${sessionScope.compte.personne.prenom} ${sessionScope.compte.personne.nom}
                                                </span>
                                            </p>
                                            <p class="mb-2">
                                                <strong>Email:</strong><br>
                                                <span class="text-brown-dark">${sessionScope.compte.email}</span>
                                            </p>
                                        </div>
                                        <div class="col-md-6">
                                            <p class="mb-2">
                                                <strong>Téléphone:</strong><br>
                                                <span class="text-brown-dark">
                                                    <c:choose>
                                                        <c:when test="${not empty sessionScope.compte.personne.numerotelephone}">
                                                            ${sessionScope.compte.personne.numerotelephone}
                                                        </c:when>
                                                        <c:otherwise>
                                                            Non renseigné
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </p>
                                            <p class="mb-0">
                                                <strong>Date de demande:</strong><br>
                                                <span class="text-brown-dark">
                                                    <fmt:formatDate value="<%= new java.util.Date() %>" pattern="dd/MM/yyyy"/>
                                                </span>
                                            </p>
                                        </div>
                                    </div>
                                    <small class="text-brown-light">
                                        <i class="bi bi-info-circle me-1"></i>
                                        Vous êtes le demandeur de ce document. La personne concernée peut être différente.
                                    </small>
                                </div>
                            </div>
                        </div>

                        <!-- Récapitulatif et validation -->
                        <div class="border-top border-brown pt-4">
                            <div class="row">
                                <div class="col-md-6 mb-2">
                                    <div class="d-grid">
                                        <a href="${pageContext.request.contextPath}/utilisateurlocal" 
                                           class="btn btn-brown-outline">
                                            <i class="bi bi-x-circle me-1"></i>Annuler
                                        </a>
                                    </div>
                                </div>
                                <div class="col-md-6 mb-2">
                                    <div class="d-grid">
                                        <button type="submit" class="btn btn-brown btn-lg" id="btnConfirmer">
                                            <i class="bi bi-check-circle me-1"></i>Confirmer la demande
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        // Références aux éléments
        const destinataireMoi = document.getElementById('destinataireMoi');
        const destinataireAutre = document.getElementById('destinataireAutre');
        const sectionPersonne = document.getElementById('sectionPersonne');
        const prenomInput = document.getElementById('prenom');
        const nomInput = document.getElementById('nom');
        const dateNaissanceInput = document.getElementById('datedenaissance');
        const lieuNaissanceInput = document.getElementById('lieudenaissance');
        const sexeSelect = document.getElementById('sexe');
        const professionInput = document.getElementById('profession');
        const residanceInput = document.getElementById('residance');

        // Valeurs originales (pour "Moi")
        const valeursOriginales = {
            prenom: prenomInput.value,
            nom: nomInput.value,
            datedenaissance: dateNaissanceInput.value,
            lieudenaissance: lieuNaissanceInput.value,
            sexe: sexeSelect.value,
            profession: professionInput.value,
            residance: residanceInput.value
        };

        // Fonction pour basculer entre les modes
        function toggleDestinataire() {
            if (destinataireMoi.checked) {
                // Mode "Moi" - pré-remplir avec les informations de l'utilisateur
                prenomInput.value = valeursOriginales.prenom;
                nomInput.value = valeursOriginales.nom;
                dateNaissanceInput.value = valeursOriginales.datedenaissance;
                lieuNaissanceInput.value = valeursOriginales.lieudenaissance;
                sexeSelect.value = valeursOriginales.sexe;
                professionInput.value = valeursOriginales.profession;
                residanceInput.value = valeursOriginales.residance;
                
                // Rendre les champs en lecture seule
                prenomInput.readOnly = true;
                nomInput.readOnly = true;
                dateNaissanceInput.readOnly = true;
                lieuNaissanceInput.readOnly = true;
                sexeSelect.disabled = false;  // Ne pas désactiver
                professionInput.readOnly = true;
                residanceInput.readOnly = true;
                
                // Style visuel pour indiquer que c'est pré-rempli
                sectionPersonne.classList.add('bg-beige-light');
                sectionPersonne.querySelector('.card-header').classList.add('bg-success');
                sectionPersonne.querySelector('.card-header').classList.remove('bg-brown');
                
            } else {
                // Mode "Autre personne" - vider et rendre modifiables
                prenomInput.value = '';
                nomInput.value = '';
                dateNaissanceInput.value = '';
                lieuNaissanceInput.value = '';
                sexeSelect.value = '';
                professionInput.value = '';
                residanceInput.value = '';
                
                // Rendre les champs modifiables
                prenomInput.readOnly = false;
                nomInput.readOnly = false;
                dateNaissanceInput.readOnly = false;
                lieuNaissanceInput.readOnly = false;
                sexeSelect.disabled = false;
                professionInput.readOnly = false;
                residanceInput.readOnly = false;
                
                // Retirer le style spécial
                sectionPersonne.classList.remove('bg-beige-light');
                sectionPersonne.querySelector('.card-header').classList.remove('bg-success');
                sectionPersonne.querySelector('.card-header').classList.add('bg-brown');
            }
        }

        // Écouteurs d'événements
        destinataireMoi.addEventListener('change', toggleDestinataire);
        destinataireAutre.addEventListener('change', toggleDestinataire);
        
        // Initialiser au chargement
        toggleDestinataire();

        // Gestion de l'affichage de la section livraison
        const formatPhysique = document.getElementById('formatPhysique');
        const formatNumerique = document.getElementById('formatNumerique');
        const sectionLivraison = document.getElementById('sectionLivraison');
        
        function toggleLivraison() {
            if (formatPhysique.checked) {
                sectionLivraison.style.display = 'block';
                document.querySelectorAll('#sectionLivraison input').forEach(input => {
                    input.required = true;
                });
            } else {
                sectionLivraison.style.display = 'none';
                document.querySelectorAll('#sectionLivraison input').forEach(input => {
                    input.required = false;
                });
            }
        }
        
        formatPhysique.addEventListener('change', toggleLivraison);
        formatNumerique.addEventListener('change', toggleLivraison);
        
        // Gestion de l'affichage des détails du motif
        const motifSelect = document.getElementById('motifId');
        const detailsMotif = document.getElementById('detailsMotif');
        const motifDescription = document.getElementById('motifDescription');
        const motifDocuments = document.getElementById('motifDocuments');
        
        if (motifSelect) {
            motifSelect.addEventListener('change', function() {
                const selectedOption = this.options[this.selectedIndex];
                
                if (selectedOption.value && selectedOption.dataset.description) {
                    motifDescription.textContent = selectedOption.dataset.description || 'Non spécifié';
                    motifDocuments.textContent = selectedOption.dataset.documents || 'Aucun document spécifique requis';
                    detailsMotif.style.display = 'block';
                } else {
                    detailsMotif.style.display = 'none';
                }
            });
        }

        // Gestion de la date de décès
        const dateDecesInput = document.getElementById('date_deces');
        
        if (dateDecesInput && dateNaissanceInput) {
            dateDecesInput.addEventListener('change', function() {
                const dateDeces = new Date(this.value);
                const dateNaissance = new Date(dateNaissanceInput.value);
                const today = new Date();
                
                if (dateDeces > today) {
                    alert('La date du décès ne peut pas être dans le futur.');
                    this.value = '';
                    return;
                }
                
                if (dateNaissance && dateDeces < dateNaissance) {
                    alert('La date du décès ne peut pas être antérieure à la date de naissance.');
                    this.value = '';
                    return;
                }
            });
        }

        // Gestion de la date de naissance du conjoint
        const conjointDateNaissanceInput = document.getElementById('conjoint_date_naissance');
        if (conjointDateNaissanceInput) {
            conjointDateNaissanceInput.addEventListener('change', function() {
                const selectedDate = new Date(this.value);
                const today = new Date();
                const minDate = new Date();
                minDate.setFullYear(today.getFullYear() - 120);
                
                if (selectedDate > today) {
                    alert('La date de naissance du conjoint ne peut pas être dans le futur.');
                    this.value = '';
                } else if (selectedDate < minDate) {
                    alert('La date de naissance du conjoint semble invraisemblable (plus de 120 ans).');
                    this.value = '';
                }
            });
        }

        // Validation en temps réel pour les champs obligatoires
        function setupRealTimeValidation() {
            const requiredInputs = document.querySelectorAll('input[required], select[required], textarea[required]');
            
            requiredInputs.forEach(input => {
                input.addEventListener('blur', function() {
                    if (!this.value.trim()) {
                        this.classList.add('is-invalid');
                        this.classList.remove('is-valid');
                    } else {
                        this.classList.remove('is-invalid');
                        this.classList.add('is-valid');
                    }
                });
                
                input.addEventListener('input', function() {
                    if (this.value.trim()) {
                        this.classList.remove('is-invalid');
                        this.classList.add('is-valid');
                    }
                });
            });
        }
        
        setupRealTimeValidation();

        // Validation de la date de naissance
        if (dateNaissanceInput) {
            dateNaissanceInput.addEventListener('change', function() {
                const selectedDate = new Date(this.value);
                const today = new Date();
                const minDate = new Date();
                minDate.setFullYear(today.getFullYear() - 120); // 120 ans maximum
                
                if (selectedDate > today) {
                    alert('La date de naissance ne peut pas être dans le futur.');
                    this.value = '';
                } else if (selectedDate < minDate) {
                    alert('La date de naissance semble invraisemblable (plus de 120 ans).');
                    this.value = '';
                }
            });
        }

        // Confirmation avant soumission
        document.getElementById('formDemande').addEventListener('submit', function(e) {
            const btn = document.getElementById('btnConfirmer');
            btn.innerHTML = '<i class="bi bi-hourglass-split me-1"></i>Traitement en cours...';
            btn.disabled = true;
        });
    });
</script>

<style>
/* Style pour la section personne en mode "Moi" */
.bg-success {
    background: linear-gradient(135deg, #28a745 0%, #20c997 100%) !important;
}

/* Style pour les champs en lecture seule */
.form-control:read-only {
    background-color: #f8f9fa;
    border-color: #ced4da;
    color: #6c757d;
}

/* Style pour les selects désactivés */
.form-select:disabled {
    background-color: #f8f9fa;
    border-color: #ced4da;
    color: #6c757d;
}

/* Styles pour la validation en temps réel */
.is-valid {
    border-color: #198754 !important;
    background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 8 8'%3e%3cpath fill='%23198754' d='M2.3 6.73L.6 4.53c-.4-1.04.46-1.4 1.1-.8l1.1 1.4 3.4-3.8c.6-.63 1.6-.27 1.2.7l-4 4.6c-.43.5-.8.4-1.1.1z'/%3e%3c/svg%3e");
    background-repeat: no-repeat;
    background-position: right calc(0.375em + 0.1875rem) center;
    background-size: calc(0.75em + 0.375rem) calc(0.75em + 0.375rem);
}

.is-invalid {
    border-color: #dc3545 !important;
    background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 12 12' width='12' height='12' fill='none' stroke='%23dc3545'%3e%3ccircle cx='6' cy='6' r='4.5'/%3e%3cpath d='m5.8 3.6.4.4.4-.4'/%3e%3cpath d='M6 7v2.5'/%3e%3c/svg%3e");
    background-repeat: no-repeat;
    background-position: right calc(0.375em + 0.1875rem) center;
    background-size: calc(0.75em + 0.375rem) calc(0.75em + 0.375rem);
}

/* Styles pour les sections conditionnelles */
.card-option {
    border: 2px solid transparent;
    border-radius: 8px;
    padding: 10px;
    margin-bottom: 10px;
    transition: all 0.3s ease;
}

.card-option:hover {
    border-color: #5c3d2e;
    background-color: #f8f9fa;
}

.card-option-label {
    cursor: pointer;
    width: 100%;
    margin: 0;
}

/* Amélioration de l'apparence des sections */
.bg-beige-light {
    background-color: #fefaf0 !important;
}

.border-brown {
    border-color: #5c3d2e !important;
}

.text-brown {
    color: #5c3d2e !important;
}

.text-brown-dark {
    color: #3b2a1f !important;
}

.text-brown-light {
    color: #8b7355 !important;
}

/* Style pour les en-têtes de section */
.card-header.bg-brown {
    background: linear-gradient(135deg, #5c3d2e 0%, #3b2a1f 100%) !important;
}

/* Indicateurs visuels pour les sections requises */
.required-section::before {
    content: "●";
    color: #dc3545;
    margin-right: 5px;
    font-size: 0.8em;
}
/* Ajoutez ce style */
select:read-only {
    background-color: #f8f9fa;
    border-color: #ced4da;
    color: #6c757d;
}
</style>
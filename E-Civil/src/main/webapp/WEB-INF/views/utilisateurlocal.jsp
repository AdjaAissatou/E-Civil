<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="container-fluid py-4">
    <!-- En-tête de bienvenue -->
    <div class="row mb-4">
        <div class="col-12">
            <div class="card bg-brown text-white shadow-lg rounded-4 border-0">
                <div class="card-body p-4">
                    <div class="row align-items-center">
                        <div class="col-md-8">
                            <h2 class="fw-bold mb-2">
                                Bonjour, 
                                <c:choose>
                                    <c:when test="${not empty sessionScope.compte.personne.prenom}">
                                        ${sessionScope.compte.personne.prenom} ${sessionScope.compte.personne.nom}
                                    </c:when>
                                    <c:when test="${not empty sessionScope.compte.personne.nom}">
                                        ${sessionScope.compte.personne.nom}
                                    </c:when>
                                    <c:otherwise>
                                        Cher Utilisateur
                                    </c:otherwise>
                                </c:choose>
                                👋
                            </h2>
                            <p class="mb-0 opacity-75">Bienvenue dans votre espace personnel État Civil Sénégal</p>
                        </div>
                        <div class="col-md-4 text-md-end">
                            <div class="bg-beige text-brown rounded-3 p-3 d-inline-block">
                                <small class="d-block opacity-75">Type d'utilisateur</small>
                                <strong>${sessionScope.compte.role}</strong>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <!-- Sidebar - Navigation -->
        <div class="col-lg-3 mb-4">
            <div class="card shadow-sm border-0 rounded-3 bg-beige-light">
                <div class="card-header bg-transparent border-0 pb-0">
                    <h5 class="fw-bold text-brown">Navigation</h5>
                </div>
                <div class="card-body p-0">
                    <div class="list-group list-group-flush rounded-3">
                        <a href="#profil" class="list-group-item list-group-item-action border-0 active bg-brown text-white" 
                           onclick="showSection('profil')">
                            <i class="bi bi-person me-2"></i>Mon Profil
                        </a>
                        <a href="#documents" class="list-group-item list-group-item-action border-0 text-brown" 
                           onclick="showSection('documents')">
                            <i class="bi bi-file-earmark-text me-2"></i>Demander un document
                        </a>
                        <a href="#historique" class="list-group-item list-group-item-action border-0 text-brown" 
                           onclick="showSection('historique')">
                            <i class="bi bi-clock-history me-2"></i>Mes Demandes
                        </a>
                        <a href="#parametres" class="list-group-item list-group-item-action border-0 text-brown" 
                           onclick="showSection('parametres')">
                            <i class="bi bi-gear me-2"></i>Paramètres
                        </a>
                    </div>
                </div>
            </div>

            <!-- Carte statistiques rapides -->
            <div class="card shadow-sm border-0 rounded-3 mt-3 bg-beige">
                <div class="card-body text-center">
                    <div class="d-flex justify-content-around">
                        <div>
                            <h4 class="text-brown-dark mb-0">${nombreDemandesEnCours}</h4>
                            <small class="text-brown-light">En cours</small>
                        </div>
                        <div>
                            <h4 class="text-brown mb-0">${nombreDemandesValidees}</h4>
                            <small class="text-brown-light">Validées</small>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Contenu principal -->
        <div class="col-lg-9">
            <!-- Section Profil -->
            <div id="profil-section" class="content-section">
                <div class="card shadow-sm border-0 rounded-3 bg-beige-light">
                    <div class="card-header bg-transparent border-0">
                        <h4 class="fw-bold text-brown">
                            <i class="bi bi-person-circle me-2"></i>Mon Profil
                        </h4>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="info-card mb-3 bg-beige rounded p-3">
                                    <label class="text-brown-light small">Nom complet</label>
                                    <p class="fw-semibold text-brown-dark">
                                        <c:choose>
                                            <c:when test="${not empty sessionScope.compte.personne.prenom and not empty sessionScope.compte.personne.nom}">
                                                ${sessionScope.compte.personne.prenom} ${sessionScope.compte.personne.nom}
                                            </c:when>
                                            <c:when test="${not empty sessionScope.compte.personne.nom}">
                                                ${sessionScope.compte.personne.nom}
                                            </c:when>
                                            <c:otherwise>
                                                Non renseigné
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                                <div class="info-card mb-3 bg-beige rounded p-3">
                                    <label class="text-brown-light small">Email</label>
                                    <p class="fw-semibold text-brown-dark">${sessionScope.compte.email}</p>
                                </div>
                                <div class="info-card mb-3 bg-beige rounded p-3">
                                    <label class="text-brown-light small">Rôle</label>
                                    <p class="fw-semibold">
                                        <span class="badge bg-brown text-white">${sessionScope.compte.role}</span>
                                    </p>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="info-card mb-3 bg-beige rounded p-3">
                                    <label class="text-brown-light small">Téléphone</label>
                                    <p class="fw-semibold text-brown-dark">
                                        <c:choose>
                                            <c:when test="${not empty sessionScope.compte.personne.numerotelephone and sessionScope.compte.personne.numerotelephone != 0}">
                                                ${sessionScope.compte.personne.numerotelephone}
                                            </c:when>
                                            <c:otherwise>
                                                Non renseigné
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                                <div class="info-card mb-3 bg-beige rounded p-3">
                                    <label class="text-brown-light small">Date d'inscription</label>
                                    <p class="fw-semibold text-brown-dark">
                                        <c:choose>
                                            <c:when test="${not empty sessionScope.compte.personne.datedinscription}">
                                                <fmt:formatDate value="${sessionScope.compte.personne.datedinscription}" pattern="dd/MM/yyyy"/>
                                            </c:when>
                                            <c:otherwise>
                                                Non renseignée
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                                <div class="info-card mb-3 bg-beige rounded p-3">
                                    <label class="text-brown-light small">Nombre d'enfants</label>
                                    <p class="fw-semibold text-brown-dark">
                                        <c:choose>
                                            <c:when test="${not empty sessionScope.compte.personne.nombredenfants}">
                                                ${sessionScope.compte.personne.nombredenfants}
                                            </c:when>
                                            <c:otherwise>
                                                0
                                            </c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                            </div>
                        </div>
<!-- Formulaire de modification du profil -->
<form id="formProfil" action="${pageContext.request.contextPath}/utilisateurLocal" method="post" style="display: none;">
    <input type="hidden" name="action" value="modifierProfil">
    
    <div class="row">
        <div class="col-md-6">
            <div class="mb-3">
                <label class="form-label text-brown">Prénom</label>
                <input type="text" class="form-control border-brown" name="prenom" 
                       value="${sessionScope.compte.personne.prenom}">
            </div>
            <div class="mb-3">
                <label class="form-label text-brown">Nom</label>
                <input type="text" class="form-control border-brown" name="nom" 
                       value="${sessionScope.compte.personne.nom}">
            </div>
        </div>
        <div class="col-md-6">
            <div class="mb-3">
                <label class="form-label text-brown">Téléphone</label>
                <input type="tel" class="form-control border-brown" name="telephone" 
                       value="${sessionScope.compte.personne.numerotelephone}">
            </div>
            <div class="mb-3">
                <label class="form-label text-brown">Nombre d'enfants</label>
                <input type="number" class="form-control border-brown" name="nombreEnfants" 
                       value="${sessionScope.compte.personne.nombredenfants}" min="0">
            </div>
        </div>
    </div>
    
    <div class="text-end">
        <button type="button" class="btn btn-brown-outline me-2" onclick="annulerModification()">
            Annuler
        </button>
        <button type="submit" class="btn btn-brown">
            Enregistrer les modifications
        </button>
    </div>
</form>

<!-- Affichage normal du profil -->
<div id="affichageProfil">
    <!-- Votre contenu actuel d'affichage du profil -->
    <div class="text-end">
        <button class="btn btn-brown-outline" onclick="afficherFormulaireProfil()">
            <i class="bi bi-pencil me-1"></i>Modifier le profil
        </button>
    </div>
</div>
                    </div>
                </div>
            </div>

           <!-- Section Documents -->
<!-- Dans la section documents -->
<div class="d-grid">
    <a href="${pageContext.request.contextPath}/demande?type=document&id=${doc[0]}" 
       class="btn btn-brown btn-sm">
        <i class="bi bi-cart-plus me-1"></i>Demander ce document
    </a>
            <p class="text-brown-light mb-0">Choisissez le type de document dont vous avez besoin</p>
        </div>
        <div class="card-body">
            <!-- Filtres -->
            <div class="row mb-4">
                <div class="col-12">
                    <div class="btn-group flex-wrap" role="group">
                        <button type="button" class="btn btn-brown active" 
                                onclick="filterDocuments('all')">Tous</button>
                        <button type="button" class="btn btn-brown-outline" 
                                onclick="filterDocuments('naissance')">Naissance</button>
                        <button type="button" class="btn btn-brown-outline" 
                                onclick="filterDocuments('mariage')">Mariage</button>
                        <button type="button" class="btn btn-brown-outline" 
                                onclick="filterDocuments('deces')">Décès</button>
                    </div>
                </div>
            </div>

            <!-- Cartes des documents -->
            <div class="row g-3" id="documents-container">
                <c:choose>
                    <c:when test="${not empty documentsDisponibles}">
                        <c:forEach var="doc" items="${documentsDisponibles}">
                            <div class="col-xl-4 col-lg-6 col-md-6 document-card" data-type="${doc.type}">
                                <div class="card h-100 shadow-sm border-0 hover-lift bg-beige-light">
                                    <div class="card-header bg-brown text-white">
                                        <h6 class="card-title mb-0">
                                            <i class="bi bi-file-earmark me-2"></i>${doc.nom}
                                        </h6>
                                    </div>
                                    <div class="card-body d-flex flex-column">
                                        <p class="card-text text-brown-light small flex-grow-1">${doc.description}</p>
                                        
                                        <div class="mt-auto">
                                            <div class="row text-center mb-3">
                                                <div class="col-6">
                                                    <div class="border-brown rounded p-2 bg-beige">
                                                        <small class="text-brown-light d-block">Tarif</small>
                                                        <strong class="text-brown-dark">
                                                            <fmt:formatNumber value="${doc.tarif}" type="number"/> FCFA
                                                        </strong>
                                                    </div>
                                                </div>
                                                <div class="col-6">
                                                    <div class="border-brown rounded p-2 bg-beige">
                                                        <small class="text-brown-light d-block">Délai</small>
                                                        <strong class="text-brown">${doc.delai} jour(s)</strong>
                                                    </div>
                                                </div>
                                            </div>
                                            
                                            <div class="d-grid">
                                                <a href="${pageContext.request.contextPath}/demande?type=document&id=${doc.id}" 
                                                   class="btn btn-brown btn-sm">
                                                    <i class="bi bi-cart-plus me-1"></i>Demander ce document
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="col-12 text-center py-5">
                            <i class="bi bi-file-earmark-x display-1 text-brown-light"></i>
                            <h4 class="text-brown-light mt-3">Aucun document disponible</h4>
                            <p class="text-brown-light">Les documents seront bientôt disponibles.</p>
                            <a href="${pageContext.request.contextPath}/documents" class="btn btn-brown mt-3">
                                Voir tous les documents
                            </a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>


            <!-- Section Historique des demandes -->
            <div id="historique-section" class="content-section" style="display: none;">
                <div class="card shadow-sm border-0 rounded-3 bg-beige-light">
                    <div class="card-header bg-transparent border-0">
                        <h4 class="fw-bold text-brown">
                            <i class="bi bi-clock-history me-2"></i>Mes Demandes
                        </h4>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead class="table-brown">
                                    <tr>
                                        <th class="text-brown">Document</th>
                                        <th class="text-brown">Date demande</th>
                                        <th class="text-brown">Statut</th>
                                        <th class="text-brown">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty mesDemandes}">
                                            <c:forEach var="demande" items="${mesDemandes}">
                                                <tr>
                                                    <td class="text-brown-dark">${demande.nomDocument}</td>
                                                    <td class="text-brown-dark">
                                                        <fmt:formatDate value="${demande.dateDemande}" pattern="dd/MM/yyyy"/>
                                                    </td>
                                                    <td>
                                                        <span class="badge ${demande.statut == 'validé' ? 'bg-success' : 
                                                                        demande.statut == 'en attente' ? 'bg-warning' : 
                                                                        demande.statut == 'rejeté' ? 'bg-danger' : 'bg-secondary'}">
                                                            ${demande.statut}
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <a href="#" class="btn btn-sm btn-brown-outline">
                                                            <i class="bi bi-eye"></i>
                                                        </a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="4" class="text-center text-brown-light py-4">
                                                    <i class="bi bi-inbox display-4 d-block mb-2"></i>
                                                    <p>Aucune demande pour le moment</p>
                                                    <a href="#documents" onclick="showSection('documents')" class="btn btn-brown btn-sm">
                                                        Faire une demande
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Section Paramètres avec déconnexion -->
            <div id="parametres-section" class="content-section" style="display: none;">
                <div class="card shadow-sm border-0 rounded-3 bg-beige-light">
                    <div class="card-header bg-transparent border-0">
                        <h4 class="fw-bold text-brown">
                            <i class="bi bi-gear me-2"></i>Paramètres du compte
                        </h4>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label text-brown">Notification par email</label>
                                    <div class="form-check form-switch">
                                        <input class="form-check-input" type="checkbox" checked>
                                        <label class="form-check-label text-brown-light">Activer les notifications</label>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label text-brown">Langue préférée</label>
                                    <select class="form-select border-brown">
                                        <option>Français</option>
                                        <option>Wolof</option>
                                        <option>Anglais</option>
                                    </select>
                                </div>
                            </div>
                        </div>
                        
                        <!-- Section sécurité -->
                        <div class="border-top border-brown pt-3 mt-3">
                            <h6 class="fw-bold text-brown mb-3">
                                <i class="bi bi-shield-lock me-2"></i>Sécurité
                            </h6>
                            <div class="d-grid gap-2">
                                <button class="btn btn-beige-outline" onclick="showChangePasswordModal()">
                                    <i class="bi bi-key me-1"></i>Changer le mot de passe
                                </button>
                                <button class="btn btn-brown" onclick="confirmLogout()">
                                    <i class="bi bi-box-arrow-right me-1"></i>Se déconnecter
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<style>
    /* Variables de couleurs harmonisées avec votre base */
    :root {
        --brown-dark: #3b2a1f;
        --brown: #5c3d2e;
        --brown-light: #7a5a48;
        --beige: #e0b084;
        --beige-light: #f8f4f0;
        --beige-lighter: #fdfaf7;
    }

    .text-brown { color: var(--brown) !important; }
    .text-brown-dark { color: var(--brown-dark) !important; }
    .text-brown-light { color: var(--brown-light) !important; }

    .bg-brown { background: linear-gradient(135deg, var(--brown-dark) 0%, var(--brown) 100%) !important; }
    .bg-beige { background-color: var(--beige) !important; }
    .bg-beige-light { background-color: var(--beige-light) !important; }
    .bg-beige-lighter { background-color: var(--beige-lighter) !important; }

    .btn-brown {
        background: linear-gradient(135deg, var(--brown) 0%, var(--brown-light) 100%);
        border: none;
        color: white !important;
    }

    .btn-brown:hover {
        background: linear-gradient(135deg, var(--brown-dark) 0%, var(--brown) 100%);
        color: white;
    }

    .btn-brown-outline {
        border: 1px solid var(--brown);
        color: var(--brown);
        background: transparent;
    }

    .btn-brown-outline:hover {
        background: var(--brown);
        color: white;
    }

    .btn-beige-outline {
        border: 1px solid var(--beige);
        color: var(--brown);
        background: transparent;
    }

    .btn-beige-outline:hover {
        background: var(--beige);
        color: var(--brown-dark);
    }

    .border-brown { border: 1px solid var(--beige) !important; }

    .table-brown {
        background: linear-gradient(135deg, var(--beige) 0%, var(--beige-light) 100%);
    }

    /* Styles existants */
    .hover-lift {
        transition: transform 0.3s ease, box-shadow 0.3s ease;
    }

    .hover-lift:hover {
        transform: translateY(-5px);
        box-shadow: 0 8px 25px rgba(92, 61, 46, 0.1) !important;
    }

    .list-group-item.active {
        background: linear-gradient(135deg, var(--brown-dark) 0%, var(--brown) 100%) !important;
        border-color: var(--brown) !important;
    }

    .content-section {
        animation: fadeIn 0.5s ease;
    }

    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(20px); }
        to { opacity: 1; transform: translateY(0); }
    }

    .card {
        border-radius: 10px;
        overflow: hidden;
    }
</style>

<script>
    // JavaScript pour la navigation entre les sections
    function showSection(sectionId) {
        document.querySelectorAll('.content-section').forEach(section => {
            section.style.display = 'none';
        });
        document.getElementById(sectionId + '-section').style.display = 'block';
        
        document.querySelectorAll('.list-group-item').forEach(item => {
            item.classList.remove('active', 'bg-brown', 'text-white');
            item.classList.add('text-brown');
        });
        
        const activeLink = document.querySelector(`a[href="#${sectionId}"]`);
        if (activeLink) {
            activeLink.classList.add('active', 'bg-brown', 'text-white');
            activeLink.classList.remove('text-brown');
        }
    }

    function filterDocuments(type) {
        const cards = document.querySelectorAll('.document-card');
        const buttons = document.querySelectorAll('.btn-group .btn');
        
        buttons.forEach(btn => {
            btn.classList.remove('active', 'btn-brown');
            btn.classList.add('btn-brown-outline');
        });
        
        buttons.forEach(btn => {
            if (btn.textContent.toLowerCase().includes(type) || (type === 'all' && btn.textContent === 'Tous')) {
                btn.classList.remove('btn-brown-outline');
                btn.classList.add('active', 'btn-brown');
            }
        });
        
        cards.forEach(card => {
            if (type === 'all') {
                card.style.display = 'block';
            } else {
                const cardType = card.getAttribute('data-type');
                card.style.display = cardType === type ? 'block' : 'none';
            }
        });
    }// Gestion de l'affichage du formulaire de profil
    function afficherFormulaireProfil() {
        document.getElementById('affichageProfil').style.display = 'none';
        document.getElementById('formProfil').style.display = 'block';
    }

    function annulerModification() {
        document.getElementById('formProfil').style.display = 'none';
        document.getElementById('affichageProfil').style.display = 'block';
    }

    // Confirmation avant actions importantes
    function confirmerAction(message) {
        return confirm(message);
    }

    // Soumission des formulaires avec confirmation
    document.getElementById('formMotDePasse').addEventListener('submit', function(e) {
        if (!confirmerAction('Êtes-vous sûr de vouloir changer votre mot de passe ?')) {
            e.preventDefault();
        }
    });

    function confirmLogout() {
        if (confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) {
            window.location.href = '${pageContext.request.contextPath}/deconnexion';
        }
    }

    document.addEventListener('DOMContentLoaded', function() {
        showSection('profil');
    });
</script>
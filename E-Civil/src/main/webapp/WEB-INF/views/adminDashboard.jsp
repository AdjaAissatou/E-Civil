<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="container mt-4">
    <h2 class="fw-bold mb-4" style="color: #5c3d2e;">Tableau de Bord Administrateur</h2>
    
    <!-- Cartes de statistiques -->
    <div class="row mb-4">
        <div class="col-md-3">
            <div class="card text-center shadow">
                <div class="card-body">
                    <i class="bi bi-people-fill fs-1" style="color: #5c3d2e;"></i>
                    <h5 class="card-title mt-2">Utilisateurs</h5>
                    <p class="card-text fs-4">${nombreUtilisateurs}</p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card text-center shadow">
                <div class="card-body">
                    <i class="bi bi-file-earmark-text fs-1" style="color: #5c3d2e;"></i>
                    <h5 class="card-title mt-2">Documents</h5>
                    <p class="card-text fs-4">${nombreDocuments}</p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card text-center shadow">
                <div class="card-body">
                    <i class="bi bi-shield-check fs-1" style="color: #5c3d2e;"></i>
                    <h5 class="card-title mt-2">Rôles</h5>
                    <p class="card-text fs-4">${nombreRoles}</p>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card text-center shadow">
                <div class="card-body">
                    <i class="bi bi-key-fill fs-1" style="color: #5c3d2e;"></i>
                    <h5 class="card-title mt-2">Permissions</h5>
                    <p class="card-text fs-4">${nombrePermissions}</p>
                </div>
            </div>
        </div>
    </div>

    <!-- Menu de gestion -->
    <div class="row">
        <div class="col-md-4 mb-3">
            <div class="card shadow">
                <div class="card-header" style="background-color: #5c3d2e; color: white;">
                    <h5 class="mb-0">Gestion des Utilisateurs</h5>
                </div>
                <div class="card-body">
                    <p class="card-text">Gérez les utilisateurs et leurs comptes.</p>
                    <a href="${pageContext.request.contextPath}/gestionUtilisateurs" class="btn w-100" style="background-color: #5c3d2e; color: white;">
                        <i class="bi bi-people-fill me-2"></i>Gérer les utilisateurs
                    </a>
                </div>
            </div>
        </div>
        <div class="col-md-4 mb-3">
            <div class="card shadow">
                <div class="card-header" style="background-color: #5c3d2e; color: white;">
                    <h5 class="mb-0">Gestion des Documents</h5>
                </div>
                <div class="card-body">
                    <p class="card-text">Gérez les documents d'état civil.</p>
                    <a href="${pageContext.request.contextPath}/gestionDocuments" class="btn w-100" style="background-color: #5c3d2e; color: white;">
                        <i class="bi bi-file-earmark-text me-2"></i>Gérer les documents
                    </a>
                </div>
            </div>
        </div>
        <div class="col-md-4 mb-3">
            <div class="card shadow">
                <div class="card-header" style="background-color: #5c3d2e; color: white;">
                    <h5 class="mb-0">Gestion des Rôles</h5>
                </div>
                <div class="card-body">
                    <p class="card-text">Gérez les rôles et leurs permissions.</p>
                    <a href="${pageContext.request.contextPath}/gestionRoles" class="btn w-100" style="background-color: #5c3d2e; color: white;">
                        <i class="bi bi-shield-check me-2"></i>Gérer les rôles
                    </a>
                </div>
            </div>
        </div>
        <div class="col-md-4 mb-3">
            <div class="card shadow">
                <div class="card-header" style="background-color: #5c3d2e; color: white;">
                    <h5 class="mb-0">Gestion des Permissions</h5>
                </div>
                <div class="card-body">
                    <p class="card-text">Gérez les permissions système.</p>
                    <a href="${pageContext.request.contextPath}/gestionPermissions" class="btn w-100" style="background-color: #5c3d2e; color: white;">
                        <i class="bi bi-key-fill me-2"></i>Gérer les permissions
                    </a>
                </div>
            </div>
        </div>
        <div class="col-md-4 mb-3">
            <div class="card shadow">
                <div class="card-header" style="background-color: #5c3d2e; color: white;">
                    <h5 class="mb-0">Gestion des Profils</h5>
                </div>
                <div class="card-body">
                    <p class="card-text">Gérez les profils utilisateurs.</p>
                    <a href="${pageContext.request.contextPath}/gestionProfils" class="btn w-100" style="background-color: #5c3d2e; color: white;">
                        <i class="bi bi-person-badge me-2"></i>Gérer les profils
                    </a>
                </div>
            </div>
        </div>
        <div class="col-md-4 mb-3">
            <div class="card shadow">
                <div class="card-header" style="background-color: #5c3d2e; color: white;">
                    <h5 class="mb-0">Statistiques</h5>
                </div>
                <div class="card-body">
                    <p class="card-text">Consultez les statistiques du système.</p>
                    <a href="${pageContext.request.contextPath}/statistiques" class="btn w-100" style="background-color: #5c3d2e; color: white;">
                        <i class="bi bi-bar-chart-fill me-2"></i>Voir les statistiques
                    </a>
                </div>
            </div>
        </div>
    </div>

    <!-- Activité récente -->
    <div class="card shadow mt-4">
        <div class="card-header" style="background-color: #5c3d2e; color: white;">
            <h5 class="mb-0">Activité Récente</h5>
        </div>
        <div class="card-body">
            <div class="list-group">
     <%--            <c:forEach var="activite" items="${activitesRecentes}">
                    <div class="list-group-item">
                        <div class="d-flex w-100 justify-content-between">
                            <h6 class="mb-1">${activite.titre}</h6>
                            <small class="text-muted">${activite.date}</small>
                        </div>
                        <p class="mb-1">${activite.description}</p>
                        <small class="text-muted">${activite.utilisateur}</small>
                    </div>
                </c:forEach> --%>
                <c:if test="${empty activitesRecentes}">
                    <p class="text-center text-muted py-3">Aucune activité récente</p>
                </c:if>
            </div>
        </div>
    </div>
</div>
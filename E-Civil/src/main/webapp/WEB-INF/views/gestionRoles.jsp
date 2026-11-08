<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="fw-bold" style="color: #5c3d2e;">Gestion des rôles</h2>
        <a href="${pageContext.request.contextPath}/gestion-roles?action=ajouter" class="btn" style="background-color: #5c3d2e; color: white;">
            <i class="bi bi-plus-circle"></i> Ajouter un rôle
        </a>
    </div>

    <!-- Messages d'alerte -->
    <c:if test="${not empty message}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>
    
    <c:if test="${not empty erreur}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${erreur}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <!-- Tableau des rôles -->
    <div class="card shadow">
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-striped table-hover">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nom</th>
                            <th>Description</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="role" items="${roles}">
                            <tr>
                                <td>${role.idrole}</td>
                                <td>${role.nom}</td>
                                <td>${role.description}</td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/gestion-roles?action=modifier&id=${role.idrole}" class="btn btn-sm btn-outline-primary me-1">
                                        <i class="bi bi-pencil"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/gestion-roles?action=supprimer&id=${role.idrole}" class="btn btn-sm btn-outline-danger" 
                                       onclick="return confirm('Êtes-vous sûr de vouloir supprimer ce rôle?')">
                                        <i class="bi bi-trash"></i>
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                        
                        <c:if test="${empty roles}">
                            <tr>
                                <td colspan="4" class="text-center text-muted py-4">
                                    <i class="bi bi-inbox" style="font-size: 2rem;"></i>
                                    <p class="mt-2">Aucun rôle enregistré</p>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
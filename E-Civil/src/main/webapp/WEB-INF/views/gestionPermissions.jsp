<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="fw-bold" style="color: #5c3d2e;">Gestion des permissions</h2>
        <a href="${pageContext.request.contextPath}/gestion-permissions?action=ajouter" class="btn" style="background-color: #5c3d2e; color: white;">
            <i class="bi bi-plus-circle"></i> Ajouter une permission
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

    <!-- Tableau des permissions -->
    <div class="card shadow">
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-striped table-hover">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nom</th>
                            <th>Description</th>
                            <th>Avantage</th>
                            <th>Interdiction</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="permission" items="${permissions}">
                            <tr>
                                <td>${permission.idpermission}</td>
                                <td>${permission.nom}</td>
                                <td>${permission.description}</td>
                                <td>${permission.avantage}</td>
                                <td>${permission.interdiction}</td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/gestion-permissions?action=modifier&id=${permission.idpermission}" class="btn btn-sm btn-outline-primary me-1">
                                        <i class="bi bi-pencil"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/gestion-permissions?action=gerer-roles&id=${permission.idpermission}" class="btn btn-sm btn-outline-info me-1">
                                        <i class="bi bi-people"></i>
                                    </a>
                                    <a href="${pageContext.request.contextPath}/gestion-permissions?action=supprimer&id=${permission.idpermission}" class="btn btn-sm btn-outline-danger" 
                                       onclick="return confirm('Êtes-vous sûr de vouloir supprimer cette permission?')">
                                        <i class="bi bi-trash"></i>
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                        
                        <c:if test="${empty permissions}">
                            <tr>
                                <td colspan="6" class="text-center text-muted py-4">
                                    <i class="bi bi-inbox" style="font-size: 2rem;"></i>
                                    <p class="mt-2">Aucune permission enregistrée</p>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
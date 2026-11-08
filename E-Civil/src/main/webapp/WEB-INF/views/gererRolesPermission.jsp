<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="container mt-4">
    <div class="row justify-content-center">
        <div class="col-md-10">
            <div class="card shadow">
                <div class="card-header" style="background-color: #5c3d2e; color: white;">
                    <h4 class="mb-0">Gérer les rôles pour la permission: ${permission.nom}</h4>
                </div>
                <div class="card-body">
                    
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

                    <!-- Formulaire pour ajouter un rôle -->
                    <div class="card mb-4">
                        <div class="card-header bg-light">
                            <h5 class="mb-0">Ajouter un rôle</h5>
                        </div>
                        <div class="card-body">
                            <form action="${pageContext.request.contextPath}/gestionPermissions" method="post">
                                <input type="hidden" name="action" value="ajouter-role">
                                <input type="hidden" name="idPermission" value="${permission.idpermission}">
                                
                                <div class="row align-items-end">
                                    <div class="col-md-8">
                                        <label for="role" class="form-label">Sélectionner un rôle</label>
                                        <select class="form-select" id="role" name="role" required>
                                            <option value="">Sélectionnez un rôle</option>
                                            <c:forEach var="role" items="${roles}">
                                                <option value="${role.idrole}">${role.nom} - ${role.description}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="col-md-4">
                                        <button type="submit" class="btn w-100" style="background-color: #5c3d2e; color: white;">
                                            <i class="bi bi-plus-circle"></i> Ajouter
                                        </button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>

                    <!-- Liste des rôles associés -->
                    <div class="card">
                        <div class="card-header bg-light">
                            <h5 class="mb-0">Rôles associés à cette permission</h5>
                        </div>
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
                                        <c:forEach var="role" items="${rolesAssocies}">
                                            <tr>
                                                <td>${role.idrole}</td>
                                                <td>${role.nom}</td>
                                                <td>${role.description}</td>
                                                <td>
                                                    <form action="${pageContext.request.contextPath}/gestionPermissions" method="post" class="d-inline">
                                                        <input type="hidden" name="action" value="supprimer-role">
                                                        <input type="hidden" name="idPermission" value="${permission.idpermission}">
                                                        <input type="hidden" name="role" value="${role.idrole}">
                                                        <button type="submit" class="btn btn-sm btn-outline-danger" 
                                                                onclick="return confirm('Êtes-vous sûr de vouloir dissocier ce rôle?')">
                                                            <i class="bi bi-trash"></i> Dissocier
                                                        </button>
                                                    </form>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        
                                        <c:if test="${empty rolesAssocies}">
                                            <tr>
                                                <td colspan="4" class="text-center text-muted py-4">
                                                    <i class="bi bi-inbox" style="font-size: 2rem;"></i>
                                                    <p class="mt-2">Aucun rôle associé à cette permission</p>
                                                </td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <div class="mt-3">
                        <a href="${pageContext.request.contextPath}/gestion-permissions" class="btn btn-secondary">
                            <i class="bi bi-arrow-left"></i> Retour à la liste des permissions
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="container mt-4">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card shadow">
                <div class="card-header" style="background-color: #5c3d2e; color: white;">
                    <h4 class="mb-0">
                        <c:choose>
                            <c:when test="${not empty role}">Modifier le rôle</c:when>
                            <c:otherwise>Ajouter un nouveau rôle</c:otherwise>
                        </c:choose>
                    </h4>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/gestion-roles" method="post">
                        <input type="hidden" name="action" value="${not empty role ? 'modifier' : 'ajouter'}">
                        
                        <c:if test="${not empty role}">
                            <input type="hidden" name="idrole" value="${role.idrole}">
                        </c:if>
                        
                        <div class="mb-3">
                            <label for="nom" class="form-label">Nom du rôle</label>
                            <input type="text" class="form-control" id="nom" name="nom" 
                                   value="${role.nom}" required>
                        </div>
                        
                        <div class="mb-3">
                            <label for="description" class="form-label">Description</label>
                            <textarea class="form-control" id="description" name="description" rows="3" required>${role.description}</textarea>
                        </div>
                        
                        <div class="d-flex justify-content-between">
                            <a href="${pageContext.request.contextPath}/gestion-roles" class="btn btn-secondary">Annuler</a>
                            <button type="submit" class="btn" style="background-color: #5c3d2e; color: white;">
                                <c:choose>
                                    <c:when test="${not empty role}">Modifier</c:when>
                                    <c:otherwise>Ajouter</c:otherwise>
                                </c:choose>
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
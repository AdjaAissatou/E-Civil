<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="container mt-4">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card shadow">
                <div class="card-header" style="background-color: #5c3d2e; color: white;">
                    <h4 class="mb-0">
                        <c:choose>
                            <c:when test="${not empty profil}">Modifier le profil</c:when>
                            <c:otherwise>Ajouter un nouveau profil</c:otherwise>
                        </c:choose>
                    </h4>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/gestionProfils" method="post">
                        <input type="hidden" name="action" value="${not empty profil ? 'modifier' : 'ajouter'}">
                        
                        <c:if test="${not empty profil}">
                            <input type="hidden" name="idProfil" value="${profil.idProfil}">
                        </c:if>
                        
                        <div class="mb-3">
                            <label for="nom" class="form-label">Nom du profil</label>
                            <input type="text" class="form-control" id="nom" name="nom" 
                                   value="${profil.nom}" required>
                        </div>
                        
                        <div class="mb-3">
                            <label for="role" class="form-label">Rôle associé</label>
                            <select class="form-select" id="role" name="role" required>
                                <option value="">Sélectionnez un rôle</option>
                                <c:forEach var="role" items="${roles}">
                                    <option value="${role.idrole}" ${profil.role.idrole == role.idrole ? 'selected' : ''}>
                                        ${role.nom} - ${role.description}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        
                        <div class="d-flex justify-content-between">
                            <a href="${pageContext.request.contextPath}/gestionProfils" class="btn btn-secondary">Annuler</a>
                            <button type="submit" class="btn" style="background-color: #5c3d2e; color: white;">
                                <c:choose>
                                    <c:when test="${not empty profil}">Modifier</c:when>
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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="container mt-4">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card shadow">
                <div class="card-header" style="background-color: #5c3d2e; color: white;">
                    <h4 class="mb-0">
                        <c:choose>
                            <c:when test="${not empty permission}">Modifier la permission</c:when>
                            <c:otherwise>Ajouter une nouvelle permission</c:otherwise>
                        </c:choose>
                    </h4>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/gestion-permissions" method="post">
                        <input type="hidden" name="action" value="${not empty permission ? 'modifier' : 'ajouter'}">
                        
                        <c:if test="${not empty permission}">
                            <input type="hidden" name="idpermission" value="${permission.idpermission}">
                        </c:if>
                        
                        <div class="mb-3">
                            <label for="nom" class="form-label">Nom de la permission</label>
                            <input type="text" class="form-control" id="nom" name="nom" 
                                   value="${permission.nom}" required>
                        </div>
                        
                        <div class="mb-3">
                            <label for="description" class="form-label">Description</label>
                            <textarea class="form-control" id="description" name="description" rows="3" required>${permission.description}</textarea>
                        </div>
                        
                        <div class="mb-3">
                            <label for="avantage" class="form-label">Avantage</label>
                            <textarea class="form-control" id="avantage" name="avantage" rows="2" required>${permission.avantage}</textarea>
                        </div>
                        
                        <div class="mb-3">
                            <label for="interdiction" class="form-label">Interdiction</label>
                            <textarea class="form-control" id="interdiction" name="interdiction" rows="2" required>${permission.interdiction}</textarea>
                        </div>
                        
                        <div class="d-flex justify-content-between">
                            <a href="${pageContext.request.contextPath}/gestion-permissions" class="btn btn-secondary">Annuler</a>
                            <button type="submit" class="btn" style="background-color: #5c3d2e; color: white;">
                                <c:choose>
                                    <c:when test="${not empty permission}">Modifier</c:when>
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
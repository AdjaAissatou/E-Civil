<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="container mt-4">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card shadow">
                <div class="card-header" style="background-color: #5c3d2e; color: white;">
                    <h4 class="mb-0">
                        <c:choose>
                            <c:when test="${not empty document}">Modifier le document</c:when>
                            <c:otherwise>Ajouter un nouveau document</c:otherwise>
                        </c:choose>
                    </h4>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/gestion-documents" method="post">
                        <input type="hidden" name="action" value="${not empty document ? 'modifier' : 'ajouter'}">
                        
                        <c:if test="${not empty document}">
                            <input type="hidden" name="numerodocument" value="${document.numerodocument}">
                        </c:if>
                        
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label for="numeroregistre" class="form-label">Numéro de registre</label>
                                <input type="number" class="form-control" id="numeroregistre" name="numeroregistre" 
                                       value="${document.numeroregistre}" required>
                            </div>
                            <div class="col-md-6">
                                <label for="numerodocument" class="form-label">Numéro de document</label>
                                <input type="number" class="form-control" id="numerodocument" name="numerodocument" 
                                       value="${document.numerodocument}" 
                                       <c:if test="${not empty document}">readonly</c:if> required>
                            </div>
                        </div>
                        
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label for="dateevenement" class="form-label">Date de l'événement</label>
                                <input type="date" class="form-control" id="dateevenement" name="dateevenement" 
                                       value="${document.dateevenement}" required>
                            </div>
                            <div class="col-md-6">
                                <label for="dateenregistrement" class="form-label">Date d'enregistrement</label>
                                <input type="date" class="form-control" id="dateenregistrement" name="dateenregistrement" 
                                       value="${document.dateenregistrement}" required>
                            </div>
                        </div>
                        
                        <div class="mb-3">
                            <label for="lieuevenement" class="form-label">Lieu de l'événement</label>
                            <input type="text" class="form-control" id="lieuevenement" name="lieuevenement" 
                                   value="${document.lieuevenement}" required>
                        </div>
                        
                        <div class="row mb-3">
                            <div class="col-md-6">
                                <label for="typedocument" class="form-label">Type de document</label>
                                <select class="form-select" id="typedocument" name="typedocument" required>
                                    <option value="">Sélectionnez un type</option>
                                    <option value="Acte de naissance" ${document.typeDocument == 'Acte de naissance' ? 'selected' : ''}>Acte de naissance</option>
                                    <option value="Acte de mariage" ${document.typeDocument == 'Acte de mariage' ? 'selected' : ''}>Acte de mariage</option>
                                    <option value="Acte de décès" ${document.typeDocument == 'Acte de décès' ? 'selected' : ''}>Acte de décès</option>
                                    <option value="Jugement" ${document.typeDocument == 'Jugement' ? 'selected' : ''}>Jugement</option>
                                    <option value="Certificat" ${document.typeDocument == 'Certificat' ? 'selected' : ''}>Certificat</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label for="statut" class="form-label">Statut</label>
                                <select class="form-select" id="statut" name="statut" required>
                                    <option value="">Sélectionnez un statut</option>
                                    <option value="En attente" ${document.statut == 'En attente' ? 'selected' : ''}>En attente</option>
                                    <option value="Validé" ${document.statut == 'Validé' ? 'selected' : ''}>Validé</option>
                                    <option value="Rejeté" ${document.statut == 'Rejeté' ? 'selected' : ''}>Rejeté</option>
                                </select>
                            </div>
                        </div>
                        
                        <div class="mb-3">
                            <label for="commune" class="form-label">Commune</label>
                            <select class="form-select" id="commune" name="commune" required>
                                <option value="">Sélectionnez une commune</option>
                                <c:forEach var="commune" items="${communes}">
                                    <option value="${commune.idCommune}" ${document.commune.idCommune == commune.idCommune ? 'selected' : ''}>
                                        ${commune.nom} - ${commune.ville}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                        
                        <div class="d-flex justify-content-between">
                            <a href="${pageContext.request.contextPath}/gestion-documents" class="btn btn-secondary">Annuler</a>
                            <button type="submit" class="btn" style="background-color: #5c3d2e; color: white;">
                                <c:choose>
                                    <c:when test="${not empty document}">Modifier</c:when>
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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2 class="fw-bold" style="color: #5c3d2e;">Gestion des documents d'état civil</h2>
        <a href="gestion-documents?action=ajouter" class="btn" style="background-color: #5c3d2e; color: white;">
            <i class="bi bi-plus-circle"></i> Ajouter un document
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

    <!-- Tableau des documents -->
    <div class="card shadow">
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-striped table-hover">
                    <thead>
                        <tr>
                            <th>N° Registre</th>
                            <th>N° Document</th>
                            <th>Type</th>
                            <th>Date Événement</th>
                            <th>Lieu</th>
                            <th>Statut</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="doc" items="${documents}">
                            <tr>
                                <td>${doc.numeroregistre}</td>
                                <td>${doc.numerodocument}</td>
                                <td>${doc.typeDocument}</td>
                                <td>${doc.dateevenement}</td>
                                <td>${doc.lieuevenement}</td>
                                <td>
                                    <span class="badge 
                                        <c:choose>
                                            <c:when test="${doc.statut == 'Validé'}">bg-success</c:when>
                                            <c:when test="${doc.statut == 'En attente'}">bg-warning</c:when>
                                            <c:when test="${doc.statut == 'Rejeté'}">bg-danger</c:when>
                                            <c:otherwise>bg-secondary</c:otherwise>
                                        </c:choose>">
                                        ${doc.statut}
                                    </span>
                                </td>
                                <td>
                                    <a href="gestion-documents?action=modifier&id=${doc.numerodocument}" class="btn btn-sm btn-outline-primary me-1">
                                        <i class="bi bi-pencil"></i>
                                    </a>
                                    <a href="gestion-documents?action=supprimer&id=${doc.numerodocument}" class="btn btn-sm btn-outline-danger" 
                                       onclick="return confirm('Êtes-vous sûr de vouloir supprimer ce document?')">
                                        <i class="bi bi-trash"></i>
                                    </a>
                                </td>
                            </tr>
                        </c:forEach>
                        
                        <c:if test="${empty documents}">
                            <tr>
                                <td colspan="7" class="text-center text-muted py-4">
                                    <i class="bi bi-inbox" style="font-size: 2rem;"></i>
                                    <p class="mt-2">Aucun document enregistré</p>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
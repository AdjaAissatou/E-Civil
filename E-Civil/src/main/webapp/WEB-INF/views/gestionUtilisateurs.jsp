<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*, EtatCivil.modeles.Utilisateur, EtatCivil.modeles.Compte" %>

<div class="container mt-4">
    <h2 class="text-center mb-4">Gestion des Utilisateurs</h2>

    <!-- Messages d'alerte -->
    <c:if test="${not empty success}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            ${success}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>
    
    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            ${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="card">
        <div class="card-header bg-primary text-white">
            <h5 class="mb-0">Liste des Utilisateurs</h5>
        </div>
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-bordered table-hover table-striped">
                    <thead class="table-dark">
                        <tr>
                            <th>ID Utilisateur</th>
                            <th>Nom complet</th>
                            <th>Téléphone</th>
                            <th>Email</th>
                            <th>Rôle actuel</th>
                            <th>Statut</th>
                            <th>Nouveau rôle</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            List<Utilisateur> utilisateurs = (List<Utilisateur>) request.getAttribute("utilisateurs");
                            if (utilisateurs != null && !utilisateurs.isEmpty()) {
                                for (Utilisateur u : utilisateurs) {
                                    for (Compte c : u.getCompte()) {
                        %>
                        <tr>
                            <td><%= u.getIdutilisateur() %></td>
                            <td>
                                <strong><%= u.getPrenom() %> <%= u.getNom() %></strong>
                                <br>
                                <small class="text-muted">Profession: <%= u.getProfession() != null ? u.getProfession() : "Non spécifié" %></small>
                            </td>
                            <td>
                                <%= u.getNumerotelephone() %>
                                <% if (u.getNumerocartedintentite_visa() > 0) { %>
                                    <br>
                                    <small class="text-muted">Carte: <%= u.getNumerocartedintentite_visa() %></small>
                                <% } %>
                            </td>
                            <td><%= c.getEmail() %></td>
                            <td>
                                <span class="badge 
                                    <% if ("admin".equals(c.getRole())) { %>bg-danger
                                    <% } else if ("officier".equals(c.getRole())) { %>bg-warning
                                    <% } else if ("Chargé de Renseignement".equals(c.getRole())) { %>bg-info
                                    <% } else { %>bg-secondary<% } %>">
                                    <%= c.getRole() %>
                                </span>
                            </td>
                            <td>
                                <span class="badge <%= c.getStatut() ? "bg-success" : "bg-secondary" %>">
                                    <%= c.getStatut() ? "Actif" : "Inactif" %>
                                </span>
                            </td>
                            <td>
                                <form action="gestionUtilisateurs" method="post" class="mb-2">
                                    <input type="hidden" name="action" value="changerRole">
                                    <input type="hidden" name="idcompte" value="<%= c.getIdcompte() %>">
                                    <div class="input-group input-group-sm">
                                        <select name="role" class="form-select">
                                            <option value="utilisateur" <%= "utilisateur".equals(c.getRole()) ? "selected" : "" %>>Utilisateur</option>
                                            <option value="officier" <%= "officier".equals(c.getRole()) ? "selected" : "" %>>Officier</option>
                                            <option value="chargé de Renseignement" <%= "chargé de Renseignement".equals(c.getRole()) ? "selected" : "" %>>Chargé de Renseignement</option>
                                            <option value="admin" <%= "admin".equals(c.getRole()) ? "selected" : "" %>>Admin</option>
                                            <option value="delegue" <%= "delegue".equals(c.getRole()) ? "selected" : "" %>>Délégué</option>
                                        </select>
                                        <button type="submit" class="btn btn-primary btn-sm">
                                            <i class="fas fa-sync-alt"></i> Mettre à jour
                                        </button>
                                    </div>
                                </form>
                            </td>
                            <td>
                                <div class="btn-group btn-group-sm">
                                    <a href="detailsUtilisateur?id=<%= u.getIdutilisateur() %>" class="btn btn-info" title="Détails">
                                        <i class="fas fa-eye"></i>
                                    </a>
                                    <a href="supprimerUtilisateur?id=<%= u.getIdutilisateur() %>" class="btn btn-danger" title="Supprimer" onclick="return confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')">
                                        <i class="fas fa-trash"></i>
                                    </a>
                                </div>
                            </td>
                        </tr>
                        <%
                                    }
                                }
                            } else {
                        %>
                        <tr>
                            <td colspan="8" class="text-center text-muted py-4">
                                <i class="fas fa-users fa-2x mb-2"></i><br>
                                Aucun utilisateur trouvé.
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="card-footer">
            <div class="row">
                <div class="col-md-6">
                    <strong>Total utilisateurs:</strong> <%= utilisateurs != null ? utilisateurs.size() : 0 %>
                </div>
                <div class="col-md-6 text-end">
                    <a href="exportUtilisateurs" class="btn btn-outline-success btn-sm">
                        <i class="fas fa-download"></i> Exporter la liste
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Ajouter Font Awesome pour les icônes -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
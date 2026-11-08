<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="container my-5">
    <div class="row justify-content-center">
        <div class="col-md-5">
            <div class="card p-4 shadow-lg" style="border-radius: 15px; border: none;">
                
                <!-- Header -->
                <div class="text-center mb-4">
                    <i class="bi bi-person-circle" style="font-size: 3.2rem; color: #5c3d2e;"></i>
                    <h3 class="fw-bold mt-2" style="color: #5c3d2e;">Connexion</h3>
                    <p class="text-muted">Accédez à votre espace E-Civil</p>
                </div>

                <!-- Message d'erreur -->
                <%
                    String erreur = (String) request.getAttribute("erreur");
                    if (erreur != null) {
                %>
                    <div class="alert alert-danger text-center"><%= erreur %></div>
                <% } %>

                <!-- Formulaire -->
                <form action="${pageContext.request.contextPath}/connexion" method="post">
                    <div class="mb-3">
                        <label class="form-label fw-semibold">Nom d'utilisateur</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light"><i class="bi bi-person-fill"></i></span>
                            <input type="text" name="email" class="form-control rounded-end" 
                                   placeholder="Entrez votre identifiant " required>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">Mot de passe</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light"><i class="bi bi-lock-fill"></i></span>
                            <input type="password" name="motdepasse" class="form-control rounded-end" 
                                   placeholder="***********" required>
                        </div>
                    </div>

                    <button type="submit" class="btn w-100 rounded-3 shadow-sm mt-2" 
                            style="background-color: #5c3d2e; color: white; font-weight:600;">
                        <i class="bi bi-box-arrow-in-right me-2"></i>Se connecter
                    </button>
                </form>

                <!-- Lien vers inscription -->
                <div class="mt-4 text-center">
                    <small>Pas encore inscrit ? 
                        <a href="${pageContext.request.contextPath}/inscription" class="fw-semibold text-decoration-none" style="color: #5c3d2e;">
                            Créez un compte
                        </a>
                    </small>
                </div>
            </div>
        </div>
    </div>
</div>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="container my-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card p-4 shadow-lg" style="border-radius: 15px; border: none;">
                
                <!-- Header -->
                <div class="text-center mb-4">
                    <i class="bi bi-person-plus-fill" style="font-size: 3rem; color: #5c3d2e;"></i>
                    <h3 class="fw-bold mt-2" style="color: #5c3d2e;">Inscription</h3>
                    <p class="text-muted">Créez votre compte E-Civil</p>
                </div>

                <!-- Messages d'erreur / succès -->
                <%
                   String erreur = (String) request.getAttribute("erreur");
                   if (erreur != null) {
                %>
                    <div class="alert alert-danger"><%= erreur %></div>
                <% } %>

                <!-- Formulaire complet -->
                <form action="${pageContext.request.contextPath}/inscription" method="post">
                    <div class="row">
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-semibold">Prénom *</label>
                            <input type="text" name="prenom" class="form-control rounded-3" required 
                                   value="${param.prenom}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-semibold">Nom *</label>
                            <input type="text" name="nom" class="form-control rounded-3" required
                                   value="${param.nom}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-semibold">Sexe *</label>
                            <select name="sexe" class="form-select rounded-3" required>
                                <option value="">Sélectionnez</option>
                                <option value="Masculin" ${param.sexe == 'Masculin' ? 'selected' : ''}>Masculin</option>
                                <option value="Féminin" ${param.sexe == 'Féminin' ? 'selected' : ''}>Féminin</option>
                            </select>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-semibold">Date de naissance *</label>
                            <input type="date" name="datedenaissance" class="form-control rounded-3" required
                                   value="${param.datedenaissance}">
                        </div>
                        <div class="col-12 mb-3">
                            <label class="form-label fw-semibold">Lieu de naissance *</label>
                            <input type="text" name="lieudenaissance" class="form-control rounded-3" required
                                   value="${param.lieudenaissance}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-semibold">Profession</label>
                            <input type="text" name="profession" class="form-control rounded-3"
                                   value="${param.profession}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-semibold">Résidence</label>
                            <input type="text" name="residance" class="form-control rounded-3"
                                   value="${param.residance}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-semibold">Localité *</label>
                            <select name="localite" class="form-select rounded-3" required>
                                <option value="">Sélectionnez</option>
                                <option value="local" ${param.localite == 'local' ? 'selected' : ''}>Sénégal</option>
                                <option value="diaspora" ${param.localite == 'diaspora' ? 'selected' : ''}>Diaspora</option>
                            </select>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-semibold">Numéro de carte/visa *</label>
                            <input type="number" name="numerocarte" class="form-control rounded-3" required
                                   value="${param.numerocarte}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-semibold">Numéro de téléphone *</label>
                            <input type="tel" name="telephone" class="form-control rounded-3" required
                                   value="${param.telephone}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-semibold">Nombre d'enfants *</label>
                            <input type="number" name="nombredenfants" class="form-control rounded-3" required min="0"
                                   value="${empty param.nombredenfants ? '0' : param.nombredenfants}">
                        </div>
                        <div class="col-12 mb-3">
                            <label class="form-label fw-semibold">Email *</label>
                            <input type="email" name="email" class="form-control rounded-3" required
                                   value="${param.email}">
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-semibold">Mot de passe *</label>
                            <input type="password" name="password" class="form-control rounded-3" required>
                        </div>
                        <div class="col-md-6 mb-3">
                            <label class="form-label fw-semibold">Confirmation mot de passe *</label>
                            <input type="password" name="confirmationPassword" class="form-control rounded-3" required>
                        </div>
                    </div>

                    <button type="submit" class="btn w-100 rounded-3 shadow-sm" style="background-color: #5c3d2e; color: white;">
                        <i class="bi bi-check-circle me-2"></i>S'inscrire
                    </button>
                </form>

                <div class="mt-3 text-center">
                    <small>Déjà inscrit ? 
                        <a href="${pageContext.request.contextPath}/connexion" class="text-decoration-none fw-semibold" style="color: #5c3d2e;">
                            Connectez-vous
                        </a>
                    </small>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
// Validation des mots de passe
document.addEventListener('DOMContentLoaded', function() {
    const passwordField = document.querySelector('input[name="password"]');
    const confirmationField = document.querySelector('input[name="confirmationPassword"]');
    
    function validatePassword() {
        if (passwordField.value !== confirmationField.value) {
            confirmationField.setCustomValidity("Les mots de passe ne correspondent pas");
        } else {
            confirmationField.setCustomValidity("");
        }
    }
    
    passwordField.addEventListener('input', validatePassword);
    confirmationField.addEventListener('input', validatePassword);
});
</script>
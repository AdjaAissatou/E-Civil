<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- Modal Création Nouvel Acte -->
<div class="modal fade" id="modalNouvelActe" tabindex="-1" aria-labelledby="modalNouvelActeLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header bg-success text-white">
                <h5 class="modal-title" id="modalNouvelActeLabel">
                    <i class="bi bi-file-earmark-plus me-2"></i>Nouvel Acte d'État Civil
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <form action="${pageContext.request.contextPath}/acteregistre" method="post" id="formNouvelActe">
                <div class="modal-body">
                    <!-- Type d'acte -->
                    <div class="row mb-3">
                        <div class="col-md-4">
                            <label for="typeActe" class="form-label">Type d'acte *</label>
                            <select class="form-select" id="typeActe" name="typeActe" required>
                                <option value="">-- Sélectionner --</option>
                                <option value="Naissance">Naissance</option>
                                <option value="Mariage">Mariage</option>
                                <option value="Divorce">Divorce</option>
                                <option value="Décès">Décès</option>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <label for="dateActe" class="form-label">Date d'enregistrement *</label>
                            <input type="date" class="form-control" id="dateActe" name="dateActe" required>
                        </div>
                        <div class="col-md-4">
                            <label for="lieuActe" class="form-label">Lieu de l'acte *</label>
                            <input type="text" class="form-control" id="lieuActe" name="lieuActe" required>
                        </div>
                    </div>

                    <!-- Informations personnelles de base -->
                    <div class="card mb-3">
                        <div class="card-header bg-light">
                            <h6 class="mb-0"><i class="bi bi-person-fill me-2"></i>Informations Personnelles</h6>
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-6 mb-3">
                                    <label for="nom" class="form-label">Nom *</label>
                                    <input type="text" class="form-control" id="nom" name="nom" required>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label for="prenom" class="form-label">Prénom(s) *</label>
                                    <input type="text" class="form-control" id="prenom" name="prenom" required>
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label for="dateNaissance" class="form-label">Date de naissance</label>
                                    <input type="date" class="form-control" id="dateNaissance" name="dateNaissance">
                                </div>
                                <div class="col-md-6 mb-3">
                                    <label for="lieuNaissance" class="form-label">Lieu de naissance</label>
                                    <input type="text" class="form-control" id="lieuNaissance" name="lieuNaissance">
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- SECTION NAISSANCE -->
                    <div id="section-naissance" class="d-none">
                        <div class="card mb-3">
                            <div class="card-header bg-light">
                                <h6 class="mb-0"><i class="bi bi-people-fill me-2"></i>Informations des Parents</h6>
                            </div>
                            <div class="card-body">
                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="nomPere" class="form-label">Nom du père</label>
                                        <input type="text" class="form-control" id="nomPere" name="nomPere">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="prenomPere" class="form-label">Prénom du père</label>
                                        <input type="text" class="form-control" id="prenomPere" name="prenomPere">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="dateNaissancePere" class="form-label">Date de naissance du père</label>
                                        <input type="date" class="form-control" id="dateNaissancePere" name="dateNaissancePere">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="nomMere" class="form-label">Nom de la mère</label>
                                        <input type="text" class="form-control" id="nomMere" name="nomMere">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="prenomMere" class="form-label">Prénom de la mère</label>
                                        <input type="text" class="form-control" id="prenomMere" name="prenomMere">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="dateNaissanceMere" class="form-label">Date de naissance de la mère</label>
                                        <input type="date" class="form-control" id="dateNaissanceMere" name="dateNaissanceMere">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- SECTION MARIAGE/DIVORCE -->
                    <div id="section-couple" class="d-none">
                        <div class="card mb-3">
                            <div class="card-header bg-light">
                                <h6 class="mb-0"><i class="bi bi-heart-fill me-2"></i>Informations du Conjoint</h6>
                            </div>
                            <div class="card-body">
                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="nomConjoint" class="form-label">Nom du conjoint *</label>
                                        <input type="text" class="form-control" id="nomConjoint" name="nomConjoint">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="prenomConjoint" class="form-label">Prénom du conjoint *</label>
                                        <input type="text" class="form-control" id="prenomConjoint" name="prenomConjoint">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="dateNaissanceConjoint" class="form-label">Date de naissance du conjoint</label>
                                        <input type="date" class="form-control" id="dateNaissanceConjoint" name="dateNaissanceConjoint">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="lieuMariage" class="form-label">Lieu du mariage</label>
                                        <input type="text" class="form-control" id="lieuMariage" name="lieuMariage">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="temoin1" class="form-label">Témoin 1</label>
                                        <input type="text" class="form-control" id="temoin1" name="temoin1">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="temoin2" class="form-label">Témoin 2</label>
                                        <input type="text" class="form-control" id="temoin2" name="temoin2">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- SECTION DECES -->
                    <div id="section-deces" class="d-none">
                        <div class="card mb-3">
                            <div class="card-header bg-light">
                                <h6 class="mb-0"><i class="bi bi-emoji-frown-fill me-2"></i>Informations du Décès</h6>
                            </div>
                            <div class="card-body">
                                <div class="row">
                                    <div class="col-md-6 mb-3">
                                        <label for="dateDeces" class="form-label">Date du décès *</label>
                                        <input type="date" class="form-control" id="dateDeces" name="dateDeces">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="heureDeces" class="form-label">Heure du décès</label>
                                        <input type="time" class="form-control" id="heureDeces" name="heureDeces">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="causeDeces" class="form-label">Cause du décès</label>
                                        <input type="text" class="form-control" id="causeDeces" name="causeDeces">
                                    </div>
                                    <div class="col-md-6 mb-3">
                                        <label for="lieuDeces" class="form-label">Lieu du décès</label>
                                        <input type="text" class="form-control" id="lieuDeces" name="lieuDeces">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Informations du registre -->
                    <div class="card mb-3">
                        <div class="card-header bg-light">
                            <h6 class="mb-0"><i class="bi bi-archive-fill me-2"></i>Informations du Registre</h6>
                        </div>
                        <div class="card-body">
                            <div class="row">
                                <div class="col-md-4 mb-3">
                                    <label for="volume" class="form-label">Volume</label>
                                    <input type="text" class="form-control" id="volume" name="volume">
                                </div>
                                <div class="col-md-4 mb-3">
                                    <label for="folio" class="form-label">Folio</label>
                                    <input type="text" class="form-control" id="folio" name="folio">
                                </div>
                                <div class="col-md-4 mb-3">
                                    <label for="centre" class="form-label">Centre d'état civil *</label>
                                    <input type="text" class="form-control" id="centre" name="centre" required>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Observations -->
                    <div class="mb-3">
                        <label for="observations" class="form-label">Observations</label>
                        <textarea class="form-control" id="observations" name="observations" rows="3" placeholder="Notes supplémentaires..."></textarea>
                    </div>

                    <div class="alert alert-info">
                        <small><i class="bi bi-info-circle me-1"></i>Les champs marqués d'un * sont obligatoires.</small>
                    </div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
                        <i class="bi bi-x-circle me-1"></i>Annuler
                    </button>
                    <button type="submit" class="btn btn-success">
                        <i class="bi bi-check-circle me-1"></i>Enregistrer l'acte
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Script dynamique amélioré -->
<script>
document.addEventListener('DOMContentLoaded', function() {
    const typeActeSelect = document.getElementById('typeActe');
    const sectionNaissance = document.getElementById('section-naissance');
    const sectionCouple = document.getElementById('section-couple');
    const sectionDeces = document.getElementById('section-deces');
    const nomConjointInput = document.getElementById('nomConjoint');
    const prenomConjointInput = document.getElementById('prenomConjoint');
    const dateDecesInput = document.getElementById('dateDeces');

    function toggleSections() {
        const type = typeActeSelect.value;
        
        // Masquer toutes les sections
        sectionNaissance.classList.add('d-none');
        sectionCouple.classList.add('d-none');
        sectionDeces.classList.add('d-none');
        
        // Réinitialiser les champs requis
        nomConjointInput.removeAttribute('required');
        prenomConjointInput.removeAttribute('required');
        dateDecesInput.removeAttribute('required');

        // Afficher la section correspondante
        if (type === 'Naissance') {
            sectionNaissance.classList.remove('d-none');
        } else if (type === 'Mariage' || type === 'Divorce') {
            sectionCouple.classList.remove('d-none');
            nomConjointInput.setAttribute('required', 'required');
            prenomConjointInput.setAttribute('required', 'required');
        } else if (type === 'Décès') {
            sectionDeces.classList.remove('d-none');
            dateDecesInput.setAttribute('required', 'required');
        }
    }

    typeActeSelect.addEventListener('change', toggleSections);
    
    // Initialiser au chargement
    toggleSections();
});
</script>
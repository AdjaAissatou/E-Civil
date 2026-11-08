<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="container-fluid py-4">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
            <div class="card shadow-sm border-0 rounded-3" style="background: linear-gradient(135deg, #fefaf0 0%, #f8f4e9 100%);">
                <div class="card-header py-3 text-white text-center" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                    <h4 class="mb-0">
                        <i class="bi bi-credit-card me-2"></i>Paiement en Ligne
                    </h4>
                </div>
                
                <div class="card-body">
                    <c:if test="${not empty demande}">
                        
                        <!-- Récapitulatif de la demande -->
                        <div class="card mb-4 border-0 shadow-sm">
                            <div class="card-header text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                                <h6 class="mb-0">
                                    <i class="bi bi-receipt me-2"></i>Récapitulatif de la Demande
                                </h6>
                            </div>
                            <div class="card-body bg-light">
                                <div class="row">
                                    <div class="col-md-6">
                                        <p><strong>Référence:</strong> #${demande.idDemande}</p>
                                        <p><strong>Document:</strong> ${demande.natureDocument}</p>
                                        <p><strong>Personne concernée:</strong> ${demande.prenom} ${demande.nom}</p>
                                    </div>
                                    <div class="col-md-6">
                                        <p><strong>Montant:</strong> 
                                            <span class="fw-bold text-success fs-5">
                                                <fmt:formatNumber value="${montant}" type="number"/> FCFA
                                            </span>
                                        </p>
                                        <p><strong>Statut:</strong> 
                                            <span class="badge text-white" style="background: linear-gradient(135deg, #d4af37 0%, #f4d03f 100%);">
                                                En attente de paiement
                                            </span>
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Formulaire de paiement -->
                        <div class="card border-0 shadow-sm">
                            <div class="card-header text-white" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                                <h6 class="mb-0">
                                    <i class="bi bi-wallet2 me-2"></i>Choisissez votre moyen de paiement
                                </h6>
                            </div>
                            <div class="card-body bg-light">
                                <form action="${pageContext.request.contextPath}/paiement" method="post" id="formPaiement">
                                    <input type="hidden" name="iddemande" value="${demande.idDemande}">
                                    <input type="hidden" name="token" value="${token}">
                                    
                                    <!-- Méthodes de paiement -->
                                    <div class="mb-4">
                                        <label class="form-label fw-bold text-brown-dark">Moyen de paiement</label>
                                        
                                        <div class="form-check mb-3">
                                            <input class="form-check-input" type="radio" name="methodePaiement" 
                                                   id="wave" value="wave" checked>
                                            <label class="form-check-label d-flex align-items-center" for="wave">
                                                <i class="bi bi-phone me-2 fs-4 text-primary"></i>
                                                <div>
                                                    <strong>Wave</strong>
                                                    <div class="text-muted small">Paiement mobile</div>
                                                </div>
                                            </label>
                                        </div>
                                        
                                        <div class="form-check mb-3">
                                            <input class="form-check-input" type="radio" name="methodePaiement" 
                                                   id="orange" value="orange_money">
                                            <label class="form-check-label d-flex align-items-center" for="orange">
                                                <i class="bi bi-phone me-2 fs-4 text-warning"></i>
                                                <div>
                                                    <strong>Orange Money</strong>
                                                    <div class="text-muted small">Paiement mobile</div>
                                                </div>
                                            </label>
                                        </div>
                                        
                                        <div class="form-check mb-3">
                                            <input class="form-check-input" type="radio" name="methodePaiement" 
                                                   id="carte" value="carte_bancaire">
                                            <label class="form-check-label d-flex align-items-center" for="carte">
                                                <i class="bi bi-credit-card me-2 fs-4 text-success"></i>
                                                <div>
                                                    <strong>Carte Bancaire</strong>
                                                    <div class="text-muted small">Visa, MasterCard</div>
                                                </div>
                                            </label>
                                        </div>
                                    </div>
                                    
                                    <!-- Référence de paiement -->
                                    <div class="mb-4">
                                        <label for="referencePaiement" class="form-label fw-bold text-brown-dark">
                                            Référence de transaction
                                        </label>
                                        <input type="text" class="form-control" id="referencePaiement" 
                                               name="referencePaiement" required 
                                               placeholder="Entrez la référence de votre transaction">
                                        <div class="form-text">
                                            Pour Wave/Orange Money: numéro de transaction<br>
                                            Pour carte: numéro d'autorisation
                                        </div>
                                    </div>
                                    
                                    <!-- Instructions selon la méthode -->
                                    <div id="instructionsWave" class="alert alert-info">
                                        <h6><i class="bi bi-info-circle me-2"></i>Instructions pour Wave</h6>
                                        <ol class="mb-0">
                                            <li>Ouvrez l'application Wave</li>
                                            <li>Allez dans "Payer"</li>
                                            <li>Saisissez le numéro: <strong>+221 70 857 85 45</strong></li>
                                            <li>Montant: <strong><fmt:formatNumber value="${montant}" type="number"/> FCFA</strong></li>
                                            <li>Note: <strong>Demande #${demande.idDemande}</strong></li>
                                        </ol>
                                    </div>
                                    
                                    <div id="instructionsOrange" class="alert alert-info d-none">
                                        <h6><i class="bi bi-info-circle me-2"></i>Instructions pour Orange Money</h6>
                                        <ol class="mb-0">
                                            <li>Composez <strong>#144#</strong></li>
                                            <li>Sélectionnez "Paiement de factures"</li>
                                            <li>Code marchand: <strong>ECIVIL</strong></li>
                                            <li>Montant: <strong><fmt:formatNumber value="${montant}" type="number"/> FCFA</strong></li>
                                            <li>Référence: <strong>${demande.idDemande}</strong></li>
                                        </ol>
                                    </div>
                                    
                                    <div id="instructionsCarte" class="alert alert-info d-none">
                                        <h6><i class="bi bi-info-circle me-2"></i>Instructions pour Carte Bancaire</h6>
                                        <p class="mb-0">
                                            Vous serez redirigé vers notre passerelle de paiement sécurisée.
                                            Aucune information de carte n'est stockée sur nos serveurs.
                                        </p>
                                    </div>
                                    
                                    <!-- Bouton de paiement -->
                                    <div class="d-grid gap-2">
                                        <button type="submit" class="btn text-white border-0 py-3" 
                                                style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                                            <i class="bi bi-lock-fill me-2"></i>
                                            Payer <fmt:formatNumber value="${montant}" type="number"/> FCFA
                                        </button>
                                        
                                        <a href="${pageContext.request.contextPath}/mes-demandes" 
                                           class="btn btn-outline-brown">
                                            <i class="bi bi-arrow-left me-2"></i>Retour à mes demandes
                                        </a>
                                    </div>
                                </form>
                            </div>
                        </div>
                        
                        <!-- Sécurité -->
                        <div class="text-center mt-4">
                            <small class="text-brown-light">
                                <i class="bi bi-shield-check me-1"></i>
                                Paiement 100% sécurisé - SSL/TLS encrypté
                            </small>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
// Gestion de l'affichage des instructions selon la méthode de paiement
document.addEventListener('DOMContentLoaded', function() {
    const methodRadios = document.querySelectorAll('input[name="methodePaiement"]');
    const instructions = {
        'wave': document.getElementById('instructionsWave'),
        'orange_money': document.getElementById('instructionsOrange'),
        'carte_bancaire': document.getElementById('instructionsCarte')
    };
    
    function showInstructions(selectedMethod) {
        // Cacher toutes les instructions
        Object.values(instructions).forEach(instruction => {
            instruction.classList.add('d-none');
        });
        
        // Afficher les instructions de la méthode sélectionnée
        if (instructions[selectedMethod]) {
            instructions[selectedMethod].classList.remove('d-none');
        }
    }
    
    // Écouter les changements de méthode de paiement
    methodRadios.forEach(radio => {
        radio.addEventListener('change', function() {
            showInstructions(this.value);
        });
    });
    
    // Afficher les instructions initiales
    const initialMethod = document.querySelector('input[name="methodePaiement"]:checked').value;
    showInstructions(initialMethod);
    
    // Validation du formulaire
    document.getElementById('formPaiement').addEventListener('submit', function(e) {
        const reference = document.getElementById('referencePaiement').value.trim();
        if (!reference) {
            e.preventDefault();
            alert('Veuillez saisir la référence de transaction');
            return false;
        }
        
        // Afficher un indicateur de chargement
        const submitBtn = this.querySelector('button[type="submit"]');
        submitBtn.innerHTML = '<i class="bi bi-arrow-repeat spinner me-2"></i>Traitement en cours...';
        submitBtn.disabled = true;
    });
});
</script>

<style>
.btn-outline-brown {
    border-color: #5c3d2e;
    color: #5c3d2e;
}

.btn-outline-brown:hover {
    background-color: #5c3d2e;
    color: white;
}

.spinner {
    animation: spin 1s linear infinite;
}

@keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}
</style> 


<%-- <%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="container-fluid py-4">
    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
            <div class="card shadow-sm border-0 rounded-3" style="background: linear-gradient(135deg, #fefaf0 0%, #f8f4e9 100%);">
                <div class="card-header py-3 text-white text-center" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                    <h4 class="mb-0">
                        <i class="bi bi-credit-card me-2"></i>Paiement en Ligne
                    </h4>
                </div>
                
                <div class="card-body">
                    <c:if test="${not empty demande}">
                        
                        <!-- Récapitulatif de la demande -->
                        <div class="card mb-4 border-0 shadow-sm">
                            <div class="card-header text-white" style="background: linear-gradient(135deg, #5c3d2e 0%, #6d4c3a 100%);">
                                <h6 class="mb-0">
                                    <i class="bi bi-receipt me-2"></i>Récapitulatif de la Demande
                                </h6>
                            </div>
                            <div class="card-body bg-light">
                                <div class="row">
                                    <div class="col-md-6">
                                        <p><strong>Référence:</strong> #${demande.idDemande}</p>
                                        <p><strong>Document:</strong> ${demande.natureDocument}</p>
                                        <p><strong>Personne concernée:</strong> ${demande.prenom} ${demande.nom}</p>
                                    </div>
                                    <div class="col-md-6">
                                        <p><strong>Montant:</strong> 
                                            <span class="fw-bold text-success fs-5">
                                                <fmt:formatNumber value="${montant}" type="number"/> FCFA
                                            </span>
                                        </p>
                                        <p><strong>Statut:</strong> 
                                            <span class="badge text-white" style="background: linear-gradient(135deg, #d4af37 0%, #f4d03f 100%);">
                                                En attente de paiement
                                            </span>
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Formulaire de paiement -->
                        <div class="card border-0 shadow-sm">
                            <div class="card-header text-white" style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);">
                                <h6 class="mb-0">
                                    <i class="bi bi-wallet2 me-2"></i>Choisissez votre moyen de paiement
                                </h6>
                            </div>
                            <div class="card-body bg-light">
                                <form action="${pageContext.request.contextPath}/paiement" method="post" id="formPaiement">
                                    <input type="hidden" name="iddemande" value="${demande.idDemande}">
                                    
                                    <!-- Méthodes de paiement -->
                                    <div class="mb-4">
                                        <label class="form-label fw-bold text-brown-dark">Moyen de paiement</label>
                                        
                                        <div class="form-check mb-3">
                                            <input class="form-check-input" type="radio" name="methodePaiement" 
                                                   id="wave" value="wave" checked>
                                            <label class="form-check-label d-flex align-items-center" for="wave">
                                                <i class="bi bi-phone me-2 fs-4 text-primary"></i>
                                                <div>
                                                    <strong>Wave</strong>
                                                    <div class="text-muted small">Paiement mobile sécurisé</div>
                                                </div>
                                            </label>
                                        </div>
                                        
                                        <div class="form-check mb-3">
                                            <input class="form-check-input" type="radio" name="methodePaiement" 
                                                   id="orange" value="orange_money">
                                            <label class="form-check-label d-flex align-items-center" for="orange">
                                                <i class="bi bi-phone me-2 fs-4 text-warning"></i>
                                                <div>
                                                    <strong>Orange Money</strong>
                                                    <div class="text-muted small">Paiement mobile sécurisé</div>
                                                </div>
                                            </label>
                                        </div>
                                        
                                        <div class="form-check mb-3">
                                            <input class="form-check-input" type="radio" name="methodePaiement" 
                                                   id="carte" value="carte_bancaire">
                                            <label class="form-check-label d-flex align-items-center" for="carte">
                                                <i class="bi bi-credit-card me-2 fs-4 text-success"></i>
                                                <div>
                                                    <strong>Carte Bancaire</strong>
                                                    <div class="text-muted small">Visa, MasterCard - Paiement sécurisé</div>
                                                </div>
                                            </label>
                                        </div>
                                    </div>
                                    
                                    <!-- Champs dynamiques selon la méthode -->
                                    <div id="waveFields">
                                        <div class="mb-3">
                                            <label for="wavePhone" class="form-label">Numéro Wave</label>
                                            <input type="tel" class="form-control" id="wavePhone" name="phoneNumber" 
                                                   placeholder="Ex: +221 70 123 45 67">
                                            <div class="form-text">Votre numéro de téléphone associé à Wave</div>
                                        </div>
                                    </div>
                                    
                                    <div id="orangeFields" class="d-none">
                                        <div class="mb-3">
                                            <label for="orangePhone" class="form-label">Numéro Orange Money</label>
                                            <input type="tel" class="form-control" id="orangePhone" name="phoneNumber" 
                                                   placeholder="Ex: +221 77 123 45 67">
                                            <div class="form-text">Votre numéro de téléphone associé à Orange Money</div>
                                        </div>
                                    </div>
                                    
                                    <div id="carteFields" class="d-none">
                                        <!-- Intégration Stripe pour les cartes -->
                                        <div class="mb-3">
                                            <label for="card-element" class="form-label">Informations de la carte</label>
                                            <div id="card-element" class="form-control">
                                                <!-- Stripe Elements s'affichera ici -->
                                            </div>
                                            <div id="card-errors" role="alert" class="text-danger mt-2"></div>
                                        </div>
                                        <input type="hidden" name="stripeToken" id="stripeToken">
                                    </div>
                                    
                                    <!-- Instructions de sécurité -->
                                    <div class="alert alert-info">
                                        <h6><i class="bi bi-shield-check me-2"></i>Paiement 100% sécurisé</h6>
                                        <p class="mb-0">
                                            Vos informations de paiement sont cryptées et transmises de manière sécurisée.
                                            Nous ne stockons jamais vos coordonnées bancaires.
                                        </p>
                                    </div>
                                    
                                    <!-- Bouton de paiement -->
                                    <div class="d-grid gap-2">
                                        <button type="submit" class="btn text-white border-0 py-3" 
                                                style="background: linear-gradient(135deg, #2d5016 0%, #3a6620 100%);"
                                                id="submitBtn">
                                            <i class="bi bi-lock-fill me-2"></i>
                                            Payer <fmt:formatNumber value="${montant}" type="number"/> FCFA
                                        </button>
                                        
                                        <a href="${pageContext.request.contextPath}/mesdemandes" 
                                           class="btn btn-outline-brown">
                                            <i class="bi bi-arrow-left me-2"></i>Retour à mes demandes
                                        </a>
                                    </div>
                                </form>
                            </div>
                        </div>
                        
                        <!-- Logos de sécurité -->
                        <div class="text-center mt-4">
                            <div class="d-flex justify-content-center gap-4 mb-2">
                                <img src="/images/visa-secure.png" alt="Visa Secure" height="30">
                                <img src="/images/mastercard-securecode.png" alt="Mastercard SecureCode" height="30">
                                <img src="/images/pci-dss.png" alt="PCI DSS" height="30">
                            </div>
                            <small class="text-brown-light">
                                <i class="bi bi-shield-check me-1"></i>
                                Transactions cryptées SSL/TLS - Conforme PCI DSS
                            </small>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Intégration Stripe.js -->
<script src="https://js.stripe.com/v3/"></script>

<script>
// Gestion de l'affichage des champs selon la méthode de paiement
document.addEventListener('DOMContentLoaded', function() {
    const methodRadios = document.querySelectorAll('input[name="methodePaiement"]');
    const waveFields = document.getElementById('waveFields');
    const orangeFields = document.getElementById('orangeFields');
    const carteFields = document.getElementById('carteFields');
    
    // Configuration Stripe
    const stripe = Stripe('${stripePublicKey}'); // À définir dans votre servlet
    const elements = stripe.elements();
    const cardElement = elements.create('card', {
        style: {
            base: {
                fontSize: '16px',
                color: '#32325d',
            }
        }
    });
    
    function showFields(selectedMethod) {
        // Cacher tous les champs
        waveFields.classList.add('d-none');
        orangeFields.classList.add('d-none');
        carteFields.classList.add('d-none');
        
        // Afficher les champs de la méthode sélectionnée
        switch(selectedMethod) {
            case 'wave':
                waveFields.classList.remove('d-none');
                break;
            case 'orange_money':
                orangeFields.classList.remove('d-none');
                break;
            case 'carte_bancaire':
                carteFields.classList.remove('d-none');
                // Monter Stripe Elements
                cardElement.mount('#card-element');
                break;
        }
    }
    
    // Écouter les changements de méthode de paiement
    methodRadios.forEach(radio => {
        radio.addEventListener('change', function() {
            showFields(this.value);
        });
    });
    
    // Afficher les champs initiaux
    const initialMethod = document.querySelector('input[name="methodePaiement"]:checked').value;
    showFields(initialMethod);
    
    // Gestion des erreurs Stripe
    cardElement.on('change', function(event) {
        const displayError = document.getElementById('card-errors');
        if (event.error) {
            displayError.textContent = event.error.message;
        } else {
            displayError.textContent = '';
        }
    });
    
    // Validation du formulaire
    document.getElementById('formPaiement').addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const submitBtn = document.getElementById('submitBtn');
        const selectedMethod = document.querySelector('input[name="methodePaiement"]:checked').value;
        
        // Validation selon la méthode
        if (selectedMethod === 'wave' || selectedMethod === 'orange_money') {
            const phoneField = selectedMethod === 'wave' ? 
                document.getElementById('wavePhone') : document.getElementById('orangePhone');
            
            if (!phoneField.value.trim()) {
                alert('Veuillez saisir votre numéro de téléphone');
                return false;
            }
        }
        
        // Pour les cartes, créer le token Stripe
        if (selectedMethod === 'carte_bancaire') {
            submitBtn.innerHTML = '<i class="bi bi-arrow-repeat spinner me-2"></i>Traitement en cours...';
            submitBtn.disabled = true;
            
            try {
                const {token, error} = await stripe.createToken(cardElement);
                
                if (error) {
                    document.getElementById('card-errors').textContent = error.message;
                    submitBtn.innerHTML = '<i class="bi bi-lock-fill me-2"></i>Payer <fmt:formatNumber value="${montant}" type="number"/> FCFA';
                    submitBtn.disabled = false;
                    return false;
                } else {
                    document.getElementById('stripeToken').value = token.id;
                }
            } catch (err) {
                console.error('Erreur Stripe:', err);
                submitBtn.innerHTML = '<i class="bi bi-lock-fill me-2"></i>Payer <fmt:formatNumber value="${montant}" type="number"/> FCFA';
                submitBtn.disabled = false;
                return false;
            }
        }
        
        // Soumettre le formulaire
        this.submit();
    });
});

// Animation du spinner
const spinner = document.querySelector('.spinner');
if (spinner) {
    spinner.style.animation = 'spin 1s linear infinite';
}
</script>

<style>
.btn-outline-brown {
    border-color: #5c3d2e;
    color: #5c3d2e;
}

.btn-outline-brown:hover {
    background-color: #5c3d2e;
    color: white;
}

.spinner {
    animation: spin 1s linear infinite;
}

@keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
}

/* Style pour Stripe Elements */
.StripeElement {
    box-sizing: border-box;
    padding: 10px 12px;
    border: 1px solid #ced4da;
    border-radius: 4px;
    background-color: white;
}

.StripeElement--focus {
    border-color: #80bdff;
    outline: 0;
    box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
}

.StripeElement--invalid {
    border-color: #dc3545;
}

.StripeElement--webkit-autofill {
    background-color: #fefde5 !important;
}
</style> --%>
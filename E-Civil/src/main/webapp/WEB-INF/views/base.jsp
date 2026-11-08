<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="Content-Security-Policy" content="default-src 'self' https: data: 'unsafe-inline' 'unsafe-eval'">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="referrer" content="no-referrer-when-downgrade">
    <title>${title != null ? title : "Plateforme État Civil Sénégalais"}</title>
    <link href="https://unpkg.com/aos@2.3.1/dist/aos.css" rel="stylesheet">

    <!-- Bootstrap & Icons -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css" rel="stylesheet">
    
    <style>
        /* Général */
        body { 
            padding-top: 70px; 
            background-color: #f8f9fa; 
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
            color: #3b2a1f;
        }

        /* Navbar */
        .navbar { 
            background: linear-gradient(135deg, #5c3d2e 0%, #3b2a1f 100%) !important; 
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .navbar-brand, .nav-link { 
            color: #f8f9fa !important; 
            font-weight: 500;
        }
        .navbar-brand:hover, .nav-link:hover { 
            color: #e0b084 !important; 
        }
        .nav-link.active {
            color: #e0b084 !important;
            font-weight: 600;
        }

        /* Boutons */
        .btn-primary {
            background: linear-gradient(135deg, #5c3d2e 0%, #7a5a48 100%);
            border: none;
        }
        .btn-primary:hover {
            background: linear-gradient(135deg, #4a3123 0%, #6a4d3a 100%);
        }
        .btn-outline-light {
            border: 1px solid #e0b084;
            color: #e0b084;
        }
        .btn-outline-light:hover {
            background-color: #e0b084;
            color: #3b2a1f;
        }

        /* Cartes */
        .card {
            border: none;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            transition: transform 0.2s;
            background-color: #fff;
        }
        .card:hover {
            transform: translateY(-2px);
        }
        .card-title {
            color: #3b2a1f;
        }

        /* Dropdown */
        .dropdown-menu {
            border: none;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            border-radius: 10px;
        }
        .dropdown-item {
            border-radius: 5px;
            margin: 2px 5px;
            padding: 8px 12px;
        }
        .dropdown-item:hover {
            background-color: #f8f9fa;
        }
        .dropdown-item.text-danger:hover {
            background-color: #fff5f5;
            color: #dc3545 !important;
        }

        /* Alertes */
        .alert-success {
            background-color: #e0f7e9;
            color: #2e6049;
            border: none;
        }
        .alert-danger {
            background-color: #fce8e6;
            color: #7a1f1f;
            border: none;
        }

        /* Footer */
        footer { 
            background-color: #2c2118; 
            color: #e8e6e1; 
            margin-top: auto;
        }
        footer a {
            color: #e0b084;
            text-decoration: none;
        }
        footer a:hover {
            text-decoration: underline;
            color: #fff;
        }

        /* Accueil */
        .hero {
            background: linear-gradient(135deg, rgba(60,40,30,0.9), rgba(40,25,15,0.9)), 
                        url('https://upload.wikimedia.org/wikipedia/commons/thumb/f/fd/Dakar_Skyline.jpg/1200px-Dakar_Skyline.jpg') center/cover;
            color: #f8f9fa;
            padding: 100px 20px;
            border-radius: 15px;
        }
        .hero h1 {
            font-size: 3rem;
            font-weight: bold;
            color: #e0b084;
        }
        .hero p {
            font-size: 1.25rem;
        }
    </style>
</head>
<body>
    <!-- NAVBAR -->
    <nav class="navbar navbar-expand-lg fixed-top">
        <div class="container">
            <a class="navbar-brand fw-bold" href="${pageContext.request.contextPath}/">
                <i class="bi bi-archive me-2"></i>E-Civil Sénégal
            </a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarMenu">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarMenu">
                <ul class="navbar-nav me-auto">
                    <li class="nav-item">
                        <a class="nav-link ${param.page == 'accueil' ? 'active' : ''}" 
                           href="${pageContext.request.contextPath}/">Accueil</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${param.page == 'documents' ? 'active' : ''}" 
                           href="${pageContext.request.contextPath}/documents">Documents</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${param.page == 'demandes' ? 'active' : ''}" 
                           href="${pageContext.request.contextPath}/mesdemandes">Mes Demandes</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link ${param.page == 'contact' ? 'active' : ''}" 
                           href="${pageContext.request.contextPath}/contact">Contact</a>
                    </li>
                </ul>
                
                <!-- ESPACE UTILISATEUR -->
                <ul class="navbar-nav ms-auto">
                    <c:choose>
                        <c:when test="${not empty sessionScope.compte}">
                            <c:set var="userRole" value="${sessionScope.compte.role}" />
                            <c:choose>
                                <c:when test="${userRole == 'admin'}">
                                    <li class="nav-item">
                                        <a class="nav-link active" href="${pageContext.request.contextPath}/adminDashboard">
                                            <i class="bi bi-speedometer2 me-1"></i>Administration
                                        </a>
                                    </li>
                                </c:when>
                                <c:when test="${userRole == 'officier'}">
                                    <li class="nav-item">
                                        <a class="nav-link" href="${pageContext.request.contextPath}/officier">
                                            <i class="bi bi-pen me-1"></i>Officier
                                        </a>
                                    </li>
                                </c:when>
                                <c:when test="${userRole == 'delegue'}">
                                    <li class="nav-item">
                                        <a class="nav-link" href="${pageContext.request.contextPath}/delegue">
                                            <i class="bi bi-eye me-1"></i>Délégué
                                        </a>
                                    </li>
                                </c:when>
                                    
                                <c:when test="${userRole == 'chargé de renseignement.equals(userRole)'}">
                                    <li class="nav-item">
                                        <a class="nav-link" href="${pageContext.request.contextPath}/renseignement">
                                            <i class="bi bi-eye me-1"></i>Chargé de Renseignement
                                        </a>
                                    </li>
                                </c:when>
                                <c:otherwise>
                                    <li class="nav-item">
                                        <a class="nav-link" href="${pageContext.request.contextPath}/utilisateurlocal">
                                            <i class="bi bi-person me-1"></i>Mon Compte
                                        </a>
                                    </li>
                                </c:otherwise>
                            </c:choose>

                            <!-- DROPDOWN USER -->
                            <li class="nav-item dropdown">
                                <a class="nav-link dropdown-toggle" href="#" role="button" 
                                   data-bs-toggle="dropdown" aria-expanded="false">
                                    <i class="bi bi-person-circle me-1"></i>
                                    <c:choose>
                                        <c:when test="${not empty sessionScope.compte.personne and not empty sessionScope.compte.personne.prenom}">
                                            ${sessionScope.compte.personne.prenom}
                                        </c:when>
                                        <c:otherwise>${sessionScope.compte.email}</c:otherwise>
                                    </c:choose>
                                </a>
                                <ul class="dropdown-menu dropdown-menu-end">
                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/utilisateurlocal"><i class="bi bi-person me-2"></i>Mon Profil</a></li>
                                    <li><a class="dropdown-item" href="${pageContext.request.contextPath}/mesdemandes"><i class="bi bi-file-text me-2"></i>Mes Demandes</a></li>
                                    <li><hr class="dropdown-divider"></li>
                                    <li><a class="dropdown-item text-danger" href="#" onclick="confirmDeconnexion()"><i class="bi bi-box-arrow-right me-2"></i>Déconnexion</a></li>
                                </ul>
                            </li>
                        </c:when>
                        <c:otherwise>
                            <li class="nav-item"><a class="nav-link" href="${pageContext.request.contextPath}/connexion">Connexion</a></li>
                            <li class="nav-item"><a class="btn btn-outline-light ms-2" href="${pageContext.request.contextPath}/inscription">S'inscrire</a></li>
                        </c:otherwise>
                    </c:choose>
                </ul>
            </div>
        </div>
    </nav>

    <!-- MAIN -->
    <main class="container my-4 min-vh-100">
        <c:choose>
            <c:when test="${not empty pageContent}">
                <jsp:include page="${pageContent}" />
            </c:when>
            <c:otherwise>
                <div class="hero text-center" data-aos="fade-up">
                    <h1>Plateforme État Civil Sénégalais</h1>
                    <p class="lead">Une gestion moderne et centralisée des documents d'état civil, accessible à tous les citoyens et à la diaspora.</p>
                    <a href="${pageContext.request.contextPath}/documents" class="btn btn-primary btn-lg mt-3">
                        <i class="bi bi-file-text me-2"></i>Consulter les documents
                    </a>
                </div>
            </c:otherwise>
        </c:choose>
    </main>

    <!-- FOOTER -->
    <footer class="text-center text-lg-start pt-4">
        <div class="container">
            <div class="row">
                <div class="col-md-4 mb-3">
                    <h5><i class="bi bi-archive me-2"></i>État Civil Sénégalais</h5>
                    <p class="mb-0">Service public de délivrance des actes d'état civil</p>
                </div>
                <div class="col-md-4 mb-3">
                    <h5>Contact</h5>
                    <ul class="list-unstyled">
                        <li><i class="bi bi-geo-alt me-2"></i>Dakar, Sénégal</li>
                        <li><i class="bi bi-telephone me-2"></i>+221 70 857 85 45</li>
                        <li><i class="bi bi-envelope me-2"></i>contact@etatcivil.sn</li>
                    </ul>
                </div>
                <div class="col-md-4 mb-3">
                    <h5>Liens utiles</h5>
                    <ul class="list-unstyled">
                        <li><a href="#">Mentions légales</a></li>
                        <li><a href="#">Politique de confidentialité</a></li>
                        <li><a href="#">Aide en ligne</a></li>
                    </ul>
                </div>
            </div>
            <hr class="my-4">
            <div class="text-center">&copy; 2025 République du Sénégal - Tous droits réservés.</div>
        </div>
    </footer>

    <!-- Scripts -->
    <script src="https://unpkg.com/aos@2.3.1/dist/aos.js"></script>
    <script> AOS.init(); </script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        function confirmDeconnexion() {
            if (confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) {
                window.location.href = '${pageContext.request.contextPath}/deconnexion';
            }
        }
    </script>
</body>
</html>

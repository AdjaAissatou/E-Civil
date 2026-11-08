<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Erreur</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div class="container mt-5">
        <div class="alert alert-danger">
            <h4>Une erreur est survenue</h4>
            <p>${error}</p>
            <a href="${pageContext.request.contextPath}/" class="btn btn-primary">Retour à l'accueil</a>
        </div>
    </div>
</body>
</html>
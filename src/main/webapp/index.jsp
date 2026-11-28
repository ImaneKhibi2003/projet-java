<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Page d'accueil provisoire --%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Anti-gaspillage alimentaire</title>
</head>
<body style="font-family:sans-serif;max-width:720px;margin:20px auto;padding:0 16px;">
<jsp:include page="/WEB-INF/views/layout/nav.jsp"/>
<h1>Anti-gaspillage alimentaire</h1>
<jsp:include page="/WEB-INF/views/layout/messages.jsp"/>
<p>Des commerces de votre ville vendent leurs invendus à prix réduit. Réservez un panier et récupérez-le avant la fermeture.</p>
<p><a href="${pageContext.request.contextPath}/paniers">Voir les paniers disponibles</a></p>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Page provisoire (A4.1), à remplacer par la version de B (B4.2) --%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Inscription - Anti-gaspillage</title>
</head>
<body style="font-family:sans-serif;max-width:420px;margin:40px auto;">
<h1>Créer un compte</h1>
<jsp:include page="/WEB-INF/views/layout/messages.jsp"/>
<form method="post" action="${pageContext.request.contextPath}/register">
    <p><label>Nom<br>
        <input type="text" name="nom" value="<c:out value='${nom}'/>" required maxlength="100" style="width:100%;"></label></p>
    <p><label>Prénom<br>
        <input type="text" name="prenom" value="<c:out value='${prenom}'/>" required maxlength="100" style="width:100%;"></label></p>
    <p><label>Email<br>
        <input type="email" name="email" value="<c:out value='${email}'/>" required style="width:100%;"></label></p>
    <p><label>Mot de passe (8 caractères minimum)<br>
        <input type="password" name="motDePasse" required minlength="8" style="width:100%;"></label></p>
    <p><label>Confirmer le mot de passe<br>
        <input type="password" name="confirmation" required minlength="8" style="width:100%;"></label></p>
    <p><button type="submit">S'inscrire</button></p>
</form>
<p>Déjà inscrit ? <a href="${pageContext.request.contextPath}/login">Se connecter</a></p>
</body>
</html>

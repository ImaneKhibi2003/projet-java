<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Page provisoire (A4.1), à remplacer par la version de B (B4.2) --%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Connexion - Anti-gaspillage</title>
</head>
<body style="font-family:sans-serif;max-width:420px;margin:40px auto;">
<h1>Connexion</h1>
<jsp:include page="/WEB-INF/views/layout/messages.jsp"/>
<form method="post" action="${pageContext.request.contextPath}/login">
    <p><label>Email<br>
        <input type="email" name="email" value="<c:out value='${email}'/>" required style="width:100%;"></label></p>
    <p><label>Mot de passe<br>
        <input type="password" name="motDePasse" required style="width:100%;"></label></p>
    <p><button type="submit">Se connecter</button></p>
</form>
<p>Pas encore de compte ? <a href="${pageContext.request.contextPath}/register">S'inscrire</a></p>
<p style="color:#888;font-size:.85em;">Comptes de test : admin@antigaspi.ma, sara@antigaspi.ma, boulangerie@antigaspi.ma (mot de passe : password123)</p>
</body>
</html>

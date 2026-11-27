<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Page d'accueil provisoire (A4.1) : elle sera remplacée par la liste des paniers --%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Anti-gaspillage alimentaire</title>
</head>
<body style="font-family:sans-serif;max-width:600px;margin:40px auto;">
<h1>Anti-gaspillage alimentaire</h1>
<jsp:include page="/WEB-INF/views/layout/messages.jsp"/>
<c:choose>
    <c:when test="${not empty sessionScope.utilisateur}">
        <p>Connecté en tant que
            <strong><c:out value="${sessionScope.utilisateur.prenom}"/> <c:out value="${sessionScope.utilisateur.nom}"/></strong>
            (rôle : <c:out value="${sessionScope.utilisateur.role}"/>)</p>
        <p><a href="${pageContext.request.contextPath}/logout">Se déconnecter</a></p>
    </c:when>
    <c:otherwise>
        <p><a href="${pageContext.request.contextPath}/login">Se connecter</a> |
           <a href="${pageContext.request.contextPath}/register">S'inscrire</a></p>
    </c:otherwise>
</c:choose>
</body>
</html>

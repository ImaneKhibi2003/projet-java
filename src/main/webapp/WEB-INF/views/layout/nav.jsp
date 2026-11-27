<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Menu provisoire (A4.2), à remplacer par la version de B (B4.1, B4.12).
     Les liens du menu fonctionnent au fur et à mesure de l'avancement des servlets. --%>
<nav style="display:flex;gap:16px;align-items:center;padding:10px 0;border-bottom:1px solid #ddd;margin-bottom:20px;">
    <a href="${pageContext.request.contextPath}/paniers"><strong>Anti-gaspillage</strong></a>
    <a href="${pageContext.request.contextPath}/paniers">Paniers</a>
    <c:if test="${sessionScope.utilisateur.role == 'CLIENT'}">
        <a href="${pageContext.request.contextPath}/client/reservations">Mes réservations</a>
    </c:if>
    <c:if test="${sessionScope.utilisateur.role == 'COMMERCANT'}">
        <a href="${pageContext.request.contextPath}/commercant/dashboard">Tableau de bord</a>
    </c:if>
    <c:if test="${sessionScope.utilisateur.role == 'ADMIN'}">
        <a href="${pageContext.request.contextPath}/admin/dashboard">Administration</a>
    </c:if>
    <span style="margin-left:auto;">
        <c:choose>
            <c:when test="${not empty sessionScope.utilisateur}">
                <c:out value="${sessionScope.utilisateur.prenom}"/> (<c:out value="${sessionScope.utilisateur.role}"/>)
                | <a href="${pageContext.request.contextPath}/logout">Déconnexion</a>
            </c:when>
            <c:otherwise>
                <a href="${pageContext.request.contextPath}/login">Connexion</a>
                | <a href="${pageContext.request.contextPath}/register">Inscription</a>
            </c:otherwise>
        </c:choose>
    </span>
</nav>

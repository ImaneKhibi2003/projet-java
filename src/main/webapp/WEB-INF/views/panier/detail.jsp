<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- Page provisoire (A4.3), à remplacer par la version de B (B4.4) --%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${panier.titre}"/> - Anti-gaspillage</title>
</head>
<body style="font-family:sans-serif;max-width:720px;margin:20px auto;padding:0 16px;">
<jsp:include page="/WEB-INF/views/layout/nav.jsp"/>
<jsp:include page="/WEB-INF/views/layout/messages.jsp"/>

<p><a href="${pageContext.request.contextPath}/paniers">&laquo; Retour à la liste</a></p>
<h1><c:out value="${panier.titre}"/></h1>

<p style="font-size:1.1em;">
    <s style="color:#888;"><fmt:formatNumber value="${panier.prixInitial}" minFractionDigits="2" maxFractionDigits="2"/> DH</s>
    <strong style="font-size:1.4em;color:#2e7d32;">
        <fmt:formatNumber value="${panier.prixReduit}" minFractionDigits="2" maxFractionDigits="2"/> DH</strong>
    <span style="background:#ff9800;color:#fff;padding:2px 6px;border-radius:4px;">
        -<fmt:formatNumber value="${(1 - panier.prixReduit / panier.prixInitial) * 100}" maxFractionDigits="0"/>%</span>
</p>

<c:if test="${not empty panier.description}">
    <p><c:out value="${panier.description}"/></p>
</c:if>

<table style="border-collapse:collapse;margin:16px 0;">
    <tr><td style="padding:4px 16px 4px 0;color:#555;">Commerce</td>
        <td><strong><c:out value="${panier.commerce.nom}"/></strong></td></tr>
    <tr><td style="padding:4px 16px 4px 0;color:#555;">Adresse</td>
        <td><c:out value="${panier.commerce.adresse}"/>, <c:out value="${panier.commerce.ville}"/></td></tr>
    <c:if test="${not empty panier.commerce.telephone}">
        <tr><td style="padding:4px 16px 4px 0;color:#555;">Téléphone</td>
            <td><c:out value="${panier.commerce.telephone}"/></td></tr>
    </c:if>
    <tr><td style="padding:4px 16px 4px 0;color:#555;">Catégorie</td>
        <td><c:out value="${panier.categorie}"/></td></tr>
    <tr><td style="padding:4px 16px 4px 0;color:#555;">Retrait</td>
        <td>du <c:out value="${fn:replace(panier.dateDebutRetrait, 'T', ' ')}"/>
            au <c:out value="${fn:replace(panier.dateFinRetrait, 'T', ' ')}"/></td></tr>
    <tr><td style="padding:4px 16px 4px 0;color:#555;">Disponibilité</td>
        <td><strong><c:out value="${panier.quantiteRestante}"/></strong> sur
            <c:out value="${panier.quantiteTotale}"/> restant(s)</td></tr>
</table>

<c:choose>
    <c:when test="${reservable}">
        <form method="post" action="${pageContext.request.contextPath}/client/reserver">
            <input type="hidden" name="panierId" value="${panier.id}">
            <label>Quantité
                <select name="quantite">
                    <c:forEach begin="1" end="${maxQuantite}" var="q">
                        <option value="${q}">${q}</option>
                    </c:forEach>
                </select></label>
            <button type="submit" style="padding:8px 18px;background:#2e7d32;color:#fff;border:0;border-radius:4px;cursor:pointer;">
                Réserver</button>
        </form>
    </c:when>
    <c:otherwise>
        <button type="button" disabled style="padding:8px 18px;">Réserver</button>
        <span style="color:#b71c1c;margin-left:10px;"><c:out value="${messageReservation}"/></span>
        <c:if test="${empty sessionScope.utilisateur}">
            <a href="${pageContext.request.contextPath}/login">Se connecter</a>
        </c:if>
    </c:otherwise>
</c:choose>
</body>
</html>

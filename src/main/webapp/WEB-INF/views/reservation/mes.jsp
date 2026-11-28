<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- Page provisoire (A4.5), à remplacer par la version de B (B4.5) --%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Mes réservations - Anti-gaspillage</title>
</head>
<body style="font-family:sans-serif;max-width:1000px;margin:20px auto;padding:0 16px;">
<jsp:include page="/WEB-INF/views/layout/nav.jsp"/>
<h1>Mes réservations</h1>
<jsp:include page="/WEB-INF/views/layout/messages.jsp"/>

<c:if test="${empty reservations}">
    <p>Vous n'avez aucune réservation. <a href="${pageContext.request.contextPath}/paniers">Voir les paniers</a></p>
</c:if>

<c:if test="${not empty reservations}">
<div style="overflow-x:auto;">
<table style="border-collapse:collapse;width:100%;">
    <thead>
    <tr style="background:#f1f1f1;text-align:left;">
        <th style="padding:8px;">Panier</th>
        <th style="padding:8px;">Commerce</th>
        <th style="padding:8px;">Qté</th>
        <th style="padding:8px;">Total</th>
        <th style="padding:8px;">Retrait jusqu'au</th>
        <th style="padding:8px;">Statut</th>
        <th style="padding:8px;">Code de retrait</th>
        <th style="padding:8px;"></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${reservations}" var="r">
        <tr style="border-bottom:1px solid #ddd;">
            <td style="padding:8px;">
                <a href="${pageContext.request.contextPath}/panier?id=${r.panier.id}"><c:out value="${r.panier.titre}"/></a>
                <br><small style="color:#777;">réservé le
                    <c:out value="${fn:substring(fn:replace(r.dateReservation, 'T', ' '), 0, 16)}"/></small>
            </td>
            <td style="padding:8px;">
                <c:out value="${r.panier.commerce.nom}"/><br>
                <small style="color:#777;"><c:out value="${r.panier.commerce.adresse}"/>, <c:out value="${r.panier.commerce.ville}"/></small>
            </td>
            <td style="padding:8px;"><c:out value="${r.quantite}"/></td>
            <td style="padding:8px;">
                <fmt:formatNumber value="${r.panier.prixReduit * r.quantite}" minFractionDigits="2" maxFractionDigits="2"/> DH</td>
            <td style="padding:8px;">
                <c:out value="${fn:substring(fn:replace(r.panier.dateFinRetrait, 'T', ' '), 0, 16)}"/></td>
            <td style="padding:8px;">
                <c:choose>
                    <c:when test="${r.statut == 'RESERVEE'}">
                        <span style="background:#1976d2;color:#fff;padding:2px 8px;border-radius:10px;">Réservée</span></c:when>
                    <c:when test="${r.statut == 'RETIREE'}">
                        <span style="background:#2e7d32;color:#fff;padding:2px 8px;border-radius:10px;">Retirée</span></c:when>
                    <c:when test="${r.statut == 'ANNULEE'}">
                        <span style="background:#757575;color:#fff;padding:2px 8px;border-radius:10px;">Annulée</span></c:when>
                    <c:otherwise>
                        <span style="background:#e65100;color:#fff;padding:2px 8px;border-radius:10px;">Expirée</span></c:otherwise>
                </c:choose>
            </td>
            <td style="padding:8px;">
                <c:choose>
                    <c:when test="${r.statut == 'RESERVEE'}">
                        <span style="font-family:monospace;font-size:1.4em;font-weight:bold;letter-spacing:2px;
                                     background:#fff8e1;border:2px dashed #ff9800;padding:4px 10px;border-radius:6px;">
                            <c:out value="${r.codeRetrait}"/></span>
                    </c:when>
                    <c:otherwise>
                        <span style="font-family:monospace;color:#999;"><c:out value="${r.codeRetrait}"/></span>
                    </c:otherwise>
                </c:choose>
            </td>
            <td style="padding:8px;">
                <c:if test="${r.statut == 'RESERVEE'}">
                    <form method="post" action="${pageContext.request.contextPath}/client/annuler"
                          onsubmit="return confirm('Annuler cette réservation ?');">
                        <input type="hidden" name="reservationId" value="${r.id}">
                        <button type="submit">Annuler</button>
                    </form>
                </c:if>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</div>
<p style="color:#555;">Présentez le code de retrait au commerçant pour récupérer votre panier.</p>
</c:if>
</body>
</html>

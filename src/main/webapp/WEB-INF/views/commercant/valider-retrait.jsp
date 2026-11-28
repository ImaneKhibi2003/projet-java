<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- Page provisoire (A4.8), à remplacer par la version de B (B4.8) --%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Valider un retrait - Anti-gaspillage</title>
</head>
<body style="font-family:sans-serif;max-width:900px;margin:20px auto;padding:0 16px;">
<jsp:include page="/WEB-INF/views/layout/nav.jsp"/>
<h1>Valider un retrait</h1>
<jsp:include page="/WEB-INF/views/layout/messages.jsp"/>

<form method="post" action="${pageContext.request.contextPath}/commercant/retrait"
      style="display:flex;gap:12px;align-items:end;margin:20px 0;">
    <label>Code de retrait du client<br>
        <input type="text" name="codeRetrait" maxlength="6" required autofocus autocomplete="off"
               pattern="[A-Za-z0-9]{6}" title="6 caractères (lettres et chiffres)"
               style="font-family:monospace;font-size:1.6em;letter-spacing:4px;text-transform:uppercase;width:9em;padding:6px;">
    </label>
    <button type="submit" style="padding:10px 20px;background:#2e7d32;color:#fff;border:0;border-radius:4px;cursor:pointer;">
        Valider le retrait</button>
</form>

<h2>Retraits d'aujourd'hui</h2>
<c:if test="${empty retraitsDuJour}">
    <p style="color:#555;">Aucun retrait validé aujourd'hui.</p>
</c:if>
<c:if test="${not empty retraitsDuJour}">
    <table style="border-collapse:collapse;width:100%;">
        <thead>
        <tr style="background:#f1f1f1;text-align:left;">
            <th style="padding:8px;">Heure</th>
            <th style="padding:8px;">Code</th>
            <th style="padding:8px;">Panier</th>
            <th style="padding:8px;">Qté</th>
            <th style="padding:8px;">Client</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${retraitsDuJour}" var="t">
            <tr style="border-bottom:1px solid #ddd;">
                <td style="padding:8px;"><c:out value="${fn:substring(fn:replace(t.dateRetrait, 'T', ' '), 11, 16)}"/></td>
                <td style="padding:8px;font-family:monospace;"><c:out value="${t.reservation.codeRetrait}"/></td>
                <td style="padding:8px;"><c:out value="${t.reservation.panier.titre}"/></td>
                <td style="padding:8px;"><c:out value="${t.reservation.quantite}"/></td>
                <td style="padding:8px;">
                    <c:out value="${t.reservation.utilisateur.prenom}"/> <c:out value="${t.reservation.utilisateur.nom}"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</c:if>
</body>
</html>

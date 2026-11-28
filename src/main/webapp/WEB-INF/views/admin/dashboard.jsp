<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- Page provisoire (A4.9), à remplacer par la version de B (B4.9) --%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Administration - Anti-gaspillage</title>
    <style>
        .carte { flex: 1; min-width: 140px; border: 1px solid #ddd; border-radius: 8px; padding: 14px; text-align: center; }
        .carte .valeur { font-size: 1.7em; font-weight: bold; color: #2e7d32; }
        th, td { padding: 8px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #f1f1f1; }
        .badge { color: #fff; padding: 2px 8px; border-radius: 10px; font-size: .85em; }
    </style>
</head>
<body style="font-family:sans-serif;max-width:1100px;margin:20px auto;padding:0 16px;">
<jsp:include page="/WEB-INF/views/layout/nav.jsp"/>
<h1>Administration</h1>
<jsp:include page="/WEB-INF/views/layout/messages.jsp"/>

<div style="display:flex;gap:12px;flex-wrap:wrap;margin:20px 0;">
    <div class="carte"><div class="valeur"><c:out value="${stats.nbUtilisateurs}"/></div>Utilisateurs</div>
    <div class="carte"><div class="valeur"><c:out value="${stats.nbCommerces}"/></div>Commerces</div>
    <div class="carte"><div class="valeur"><c:out value="${stats.nbPaniers}"/></div>Paniers</div>
    <div class="carte"><div class="valeur"><c:out value="${stats.nbReservations}"/></div>Réservations</div>
    <div class="carte"><div class="valeur"><c:out value="${stats.nbRetraits}"/></div>Retraits</div>
    <div class="carte"><div class="valeur"><c:out value="${stats.invendusSauves}"/></div>Invendus sauvés</div>
    <div class="carte"><div class="valeur"><fmt:formatNumber value="${stats.chiffreAffairesTotal}" minFractionDigits="2" maxFractionDigits="2"/> DH</div>Chiffre d'affaires</div>
</div>

<h2>Utilisateurs</h2>
<p><a href="${pageContext.request.contextPath}/admin/utilisateurs?action=nouveau"
      style="padding:8px 16px;background:#2e7d32;color:#fff;border-radius:4px;text-decoration:none;">+ Nouvel utilisateur</a></p>

<div style="overflow-x:auto;">
<table style="border-collapse:collapse;width:100%;">
    <thead><tr><th>Nom</th><th>Email</th><th>Rôle</th><th>Inscrit le</th><th>Actions</th></tr></thead>
    <tbody>
    <c:forEach items="${utilisateurs}" var="u">
        <c:set var="moi" value="${u.id == sessionScope.utilisateur.id}"/>
        <tr>
            <td><c:out value="${u.prenom}"/> <c:out value="${u.nom}"/>
                <c:if test="${moi}"><small style="color:#777;">(vous)</small></c:if></td>
            <td><c:out value="${u.email}"/></td>
            <td>
                <c:choose>
                    <c:when test="${u.role == 'ADMIN'}"><span class="badge" style="background:#6a1b9a;">Admin</span></c:when>
                    <c:when test="${u.role == 'COMMERCANT'}"><span class="badge" style="background:#ef6c00;">Commerçant</span></c:when>
                    <c:otherwise><span class="badge" style="background:#1976d2;">Client</span></c:otherwise>
                </c:choose>
            </td>
            <td><c:out value="${fn:substring(fn:replace(u.dateInscription, 'T', ' '), 0, 10)}"/></td>
            <td style="white-space:nowrap;">
                <a href="${pageContext.request.contextPath}/admin/utilisateurs?action=modifier&amp;id=${u.id}">Modifier</a>

                <form method="post" action="${pageContext.request.contextPath}/admin/utilisateurs" style="display:inline;margin-left:8px;">
                    <input type="hidden" name="action" value="role">
                    <input type="hidden" name="id" value="${u.id}">
                    <select name="role" <c:if test="${moi}">disabled</c:if>>
                        <option value="CLIENT" <c:if test="${u.role == 'CLIENT'}">selected</c:if>>Client</option>
                        <option value="COMMERCANT" <c:if test="${u.role == 'COMMERCANT'}">selected</c:if>>Commerçant</option>
                        <option value="ADMIN" <c:if test="${u.role == 'ADMIN'}">selected</c:if>>Admin</option>
                    </select>
                    <button type="submit" <c:if test="${moi}">disabled</c:if>>Changer</button>
                </form>

                <form method="post" action="${pageContext.request.contextPath}/admin/utilisateurs"
                      style="display:inline;margin-left:8px;"
                      onsubmit="return confirm('Supprimer cet utilisateur ?');">
                    <input type="hidden" name="action" value="supprimer">
                    <input type="hidden" name="id" value="${u.id}">
                    <button type="submit" <c:if test="${moi}">disabled</c:if>>Supprimer</button>
                </form>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</div>
</body>
</html>

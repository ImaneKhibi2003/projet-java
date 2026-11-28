<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- Page provisoire (A4.6), à remplacer par la version de B (B4.6) --%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tableau de bord - Anti-gaspillage</title>
    <style>
        .carte { flex: 1; min-width: 150px; border: 1px solid #ddd; border-radius: 8px; padding: 14px; text-align: center; }
        .carte .valeur { font-size: 1.8em; font-weight: bold; color: #2e7d32; }
        th, td { padding: 8px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #f1f1f1; }
        .badge { color: #fff; padding: 2px 8px; border-radius: 10px; font-size: .85em; }
    </style>
</head>
<body style="font-family:sans-serif;max-width:1000px;margin:20px auto;padding:0 16px;">
<jsp:include page="/WEB-INF/views/layout/nav.jsp"/>
<jsp:include page="/WEB-INF/views/layout/messages.jsp"/>

<c:if test="${not empty commerce}">
    <h1>Tableau de bord - <c:out value="${commerce.nom}"/></h1>

    <div style="display:flex;gap:12px;flex-wrap:wrap;margin:20px 0;">
        <div class="carte"><div class="valeur"><c:out value="${stats.nbPaniers}"/></div>Paniers publiés</div>
        <div class="carte"><div class="valeur"><c:out value="${stats.nbReservations}"/></div>Réservations reçues</div>
        <div class="carte"><div class="valeur"><fmt:formatNumber value="${stats.chiffreAffaires}" minFractionDigits="2" maxFractionDigits="2"/> DH</div>Chiffre d'affaires</div>
        <div class="carte"><div class="valeur"><c:out value="${stats.invendusSauves}"/></div>Invendus sauvés</div>
    </div>

    <p>
        <a href="${pageContext.request.contextPath}/commercant/paniers?action=nouveau"
           style="padding:8px 16px;background:#2e7d32;color:#fff;border-radius:4px;text-decoration:none;">+ Publier un panier</a>
        <a href="${pageContext.request.contextPath}/commercant/retrait" style="margin-left:12px;">Valider un retrait</a>
    </p>

    <h2>Mes paniers</h2>
    <c:if test="${empty paniers}"><p style="color:#555;">Vous n'avez publié aucun panier.</p></c:if>
    <c:if test="${not empty paniers}">
    <div style="overflow-x:auto;">
    <table style="border-collapse:collapse;width:100%;">
        <thead><tr><th>Titre</th><th>Prix</th><th>Stock</th><th>Retrait jusqu'au</th><th>Statut</th><th></th></tr></thead>
        <tbody>
        <c:forEach items="${paniers}" var="p">
            <tr>
                <td><c:out value="${p.titre}"/></td>
                <td><s style="color:#888;"><fmt:formatNumber value="${p.prixInitial}" minFractionDigits="2" maxFractionDigits="2"/></s>
                    <fmt:formatNumber value="${p.prixReduit}" minFractionDigits="2" maxFractionDigits="2"/> DH</td>
                <td><c:out value="${p.quantiteRestante}"/> / <c:out value="${p.quantiteTotale}"/></td>
                <td><c:out value="${fn:substring(fn:replace(p.dateFinRetrait, 'T', ' '), 0, 16)}"/></td>
                <td>
                    <c:choose>
                        <c:when test="${p.statut == 'DISPONIBLE'}"><span class="badge" style="background:#2e7d32;">Disponible</span></c:when>
                        <c:when test="${p.statut == 'EPUISE'}"><span class="badge" style="background:#757575;">Épuisé</span></c:when>
                        <c:otherwise><span class="badge" style="background:#e65100;">Expiré</span></c:otherwise>
                    </c:choose>
                </td>
                <td style="white-space:nowrap;">
                    <a href="${pageContext.request.contextPath}/commercant/paniers?action=modifier&amp;id=${p.id}">Modifier</a>
                    <form method="post" action="${pageContext.request.contextPath}/commercant/paniers"
                          style="display:inline;" onsubmit="return confirm('Supprimer ce panier ?');">
                        <input type="hidden" name="action" value="supprimer">
                        <input type="hidden" name="id" value="${p.id}">
                        <button type="submit" style="margin-left:8px;">Supprimer</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    </div>
    </c:if>

    <h2 style="margin-top:30px;">Réservations reçues</h2>
    <c:if test="${empty reservations}"><p style="color:#555;">Aucune réservation pour le moment.</p></c:if>
    <c:if test="${not empty reservations}">
    <div style="overflow-x:auto;">
    <table style="border-collapse:collapse;width:100%;">
        <thead><tr><th>Date</th><th>Panier</th><th>Client</th><th>Qté</th><th>Code</th><th>Statut</th></tr></thead>
        <tbody>
        <c:forEach items="${reservations}" var="r">
            <tr>
                <td><c:out value="${fn:substring(fn:replace(r.dateReservation, 'T', ' '), 0, 16)}"/></td>
                <td><c:out value="${r.panier.titre}"/></td>
                <td><c:out value="${r.utilisateur.prenom}"/> <c:out value="${r.utilisateur.nom}"/></td>
                <td><c:out value="${r.quantite}"/></td>
                <td style="font-family:monospace;"><c:out value="${r.codeRetrait}"/></td>
                <td>
                    <c:choose>
                        <c:when test="${r.statut == 'RESERVEE'}"><span class="badge" style="background:#1976d2;">Réservée</span></c:when>
                        <c:when test="${r.statut == 'RETIREE'}"><span class="badge" style="background:#2e7d32;">Retirée</span></c:when>
                        <c:when test="${r.statut == 'ANNULEE'}"><span class="badge" style="background:#757575;">Annulée</span></c:when>
                        <c:otherwise><span class="badge" style="background:#e65100;">Expirée</span></c:otherwise>
                    </c:choose>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    </div>
    </c:if>
</c:if>
</body>
</html>

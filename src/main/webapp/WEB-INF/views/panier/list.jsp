<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- Page provisoire (A4.2), à remplacer par la version de B (B4.3) --%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Paniers disponibles - Anti-gaspillage</title>
</head>
<body style="font-family:sans-serif;max-width:1000px;margin:20px auto;padding:0 16px;">
<jsp:include page="/WEB-INF/views/layout/nav.jsp"/>
<h1>Paniers disponibles</h1>
<jsp:include page="/WEB-INF/views/layout/messages.jsp"/>

<form method="get" action="${pageContext.request.contextPath}/paniers"
      style="display:flex;gap:12px;flex-wrap:wrap;align-items:end;margin-bottom:20px;">
    <label>Catégorie<br>
        <select name="categorie">
            <option value="">Toutes</option>
            <c:forEach items="${categories}" var="cat">
                <option value="<c:out value='${cat}'/>" <c:if test="${cat == categorie}">selected</c:if>>
                    <c:out value="${cat}"/></option>
            </c:forEach>
        </select></label>
    <label>Prix maximum (DH)<br>
        <input type="number" name="prixMax" min="0" step="0.01" value="<c:out value='${prixMaxSaisi}'/>"></label>
    <label>Trier par<br>
        <select name="tri">
            <option value="date" <c:if test="${tri == 'date'}">selected</c:if>>Fin de retrait la plus proche</option>
            <option value="prix" <c:if test="${tri == 'prix'}">selected</c:if>>Prix croissant</option>
        </select></label>
    <button type="submit">Filtrer</button>
    <a href="${pageContext.request.contextPath}/paniers">Réinitialiser</a>
</form>

<p><c:out value="${total}"/> panier(s) trouvé(s)</p>

<c:if test="${empty paniers}">
    <p>Aucun panier ne correspond à votre recherche.</p>
</c:if>

<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:16px;">
    <c:forEach items="${paniers}" var="p">
        <div style="border:1px solid #ccc;border-radius:8px;padding:14px;">
            <h3 style="margin:0 0 6px;"><c:out value="${p.titre}"/></h3>
            <p style="margin:0;color:#555;">
                <c:out value="${p.commerce.nom}"/> - <c:out value="${p.commerce.ville}"/></p>
            <p style="margin:8px 0;">
                <s style="color:#888;"><fmt:formatNumber value="${p.prixInitial}" minFractionDigits="2" maxFractionDigits="2"/> DH</s>
                <strong style="font-size:1.2em;color:#2e7d32;">
                    <fmt:formatNumber value="${p.prixReduit}" minFractionDigits="2" maxFractionDigits="2"/> DH</strong>
                <span style="background:#ff9800;color:#fff;padding:2px 6px;border-radius:4px;font-size:.85em;">
                    -<fmt:formatNumber value="${(1 - p.prixReduit / p.prixInitial) * 100}" maxFractionDigits="0"/>%</span>
            </p>
            <p style="margin:4px 0;">Reste : <strong><c:out value="${p.quantiteRestante}"/></strong>
                sur <c:out value="${p.quantiteTotale}"/></p>
            <p style="margin:4px 0;color:#555;">Retrait jusqu'au
                <c:out value="${fn:replace(p.dateFinRetrait, 'T', ' ')}"/></p>
            <p style="margin:10px 0 0;">
                <a href="${pageContext.request.contextPath}/panier?id=${p.id}">Voir le détail</a></p>
        </div>
    </c:forEach>
</div>

<c:if test="${nbPages > 1}">
    <p style="margin-top:20px;">
        <c:if test="${page > 1}">
            <a href="${pageContext.request.contextPath}/paniers?<c:out value='${filtres}'/>&amp;page=${page - 1}">&laquo; Précédent</a>
        </c:if>
        <c:forEach begin="1" end="${nbPages}" var="i">
            <c:choose>
                <c:when test="${i == page}"><strong>[${i}]</strong></c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/paniers?<c:out value='${filtres}'/>&amp;page=${i}">${i}</a>
                </c:otherwise>
            </c:choose>
        </c:forEach>
        <c:if test="${page < nbPages}">
            <a href="${pageContext.request.contextPath}/paniers?<c:out value='${filtres}'/>&amp;page=${page + 1}">Suivant &raquo;</a>
        </c:if>
    </p>
</c:if>
</body>
</html>

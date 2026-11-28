<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Page provisoire (A4.7), à remplacer par la version de B (B4.7) --%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Panier - Anti-gaspillage</title>
    <style>
        .champ { margin-bottom: 14px; }
        .champ label { display: block; font-weight: bold; margin-bottom: 4px; }
        .champ input, .champ textarea { width: 100%; padding: 6px; box-sizing: border-box; }
        .erreur { color: #b71c1c; font-size: .9em; }
    </style>
</head>
<body style="font-family:sans-serif;max-width:640px;margin:20px auto;padding:0 16px;">
<jsp:include page="/WEB-INF/views/layout/nav.jsp"/>
<h1><c:choose>
    <c:when test="${modeEdition}">Modifier le panier</c:when>
    <c:otherwise>Publier un panier</c:otherwise>
</c:choose></h1>
<jsp:include page="/WEB-INF/views/layout/messages.jsp"/>

<form method="post" action="${pageContext.request.contextPath}/commercant/paniers" novalidate>
    <input type="hidden" name="action" value="enregistrer">
    <c:if test="${modeEdition}">
        <input type="hidden" name="id" value="<c:out value='${panier.id}'/>">
    </c:if>

    <div class="champ">
        <label for="titre">Titre</label>
        <input type="text" id="titre" name="titre" maxlength="150" value="<c:out value='${panier.titre}'/>">
        <c:if test="${not empty erreurs.titre}"><span class="erreur"><c:out value="${erreurs.titre}"/></span></c:if>
    </div>

    <div class="champ">
        <label for="description">Description (facultative)</label>
        <textarea id="description" name="description" rows="3" maxlength="500"><c:out value="${panier.description}"/></textarea>
        <c:if test="${not empty erreurs.description}"><span class="erreur"><c:out value="${erreurs.description}"/></span></c:if>
    </div>

    <div class="champ">
        <label for="categorie">Catégorie</label>
        <input type="text" id="categorie" name="categorie" maxlength="50" value="<c:out value='${panier.categorie}'/>">
        <c:if test="${not empty erreurs.categorie}"><span class="erreur"><c:out value="${erreurs.categorie}"/></span></c:if>
    </div>

    <div style="display:flex;gap:12px;">
        <div class="champ" style="flex:1;">
            <label for="prixInitial">Prix initial (DH)</label>
            <input type="number" id="prixInitial" name="prixInitial" min="0" step="0.01" value="<c:out value='${panier.prixInitial}'/>">
            <c:if test="${not empty erreurs.prixInitial}"><span class="erreur"><c:out value="${erreurs.prixInitial}"/></span></c:if>
        </div>
        <div class="champ" style="flex:1;">
            <label for="prixReduit">Prix réduit (DH)</label>
            <input type="number" id="prixReduit" name="prixReduit" min="0" step="0.01" value="<c:out value='${panier.prixReduit}'/>">
            <c:if test="${not empty erreurs.prixReduit}"><span class="erreur"><c:out value="${erreurs.prixReduit}"/></span></c:if>
        </div>
        <div class="champ" style="flex:1;">
            <label for="quantiteTotale">Quantité</label>
            <input type="number" id="quantiteTotale" name="quantiteTotale" min="1" step="1" value="<c:out value='${panier.quantiteTotale}'/>">
            <c:if test="${not empty erreurs.quantiteTotale}"><span class="erreur"><c:out value="${erreurs.quantiteTotale}"/></span></c:if>
        </div>
    </div>

    <div style="display:flex;gap:12px;">
        <div class="champ" style="flex:1;">
            <label for="dateDebutRetrait">Retrait à partir du</label>
            <input type="datetime-local" id="dateDebutRetrait" name="dateDebutRetrait" value="<c:out value='${panier.dateDebutRetrait}'/>">
            <c:if test="${not empty erreurs.dateDebutRetrait}"><span class="erreur"><c:out value="${erreurs.dateDebutRetrait}"/></span></c:if>
        </div>
        <div class="champ" style="flex:1;">
            <label for="dateFinRetrait">Retrait jusqu'au</label>
            <input type="datetime-local" id="dateFinRetrait" name="dateFinRetrait" value="<c:out value='${panier.dateFinRetrait}'/>">
            <c:if test="${not empty erreurs.dateFinRetrait}"><span class="erreur"><c:out value="${erreurs.dateFinRetrait}"/></span></c:if>
        </div>
    </div>

    <p>
        <button type="submit" style="padding:8px 18px;background:#2e7d32;color:#fff;border:0;border-radius:4px;cursor:pointer;">
            <c:choose><c:when test="${modeEdition}">Enregistrer les modifications</c:when><c:otherwise>Publier le panier</c:otherwise></c:choose>
        </button>
        <a href="${pageContext.request.contextPath}/commercant/dashboard" style="margin-left:12px;">Annuler</a>
    </p>
</form>
</body>
</html>

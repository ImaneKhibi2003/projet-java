<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Page provisoire (A4.10), à remplacer par la version de B --%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Utilisateur - Anti-gaspillage</title>
    <style>
        .champ { margin-bottom: 14px; }
        .champ label { display: block; font-weight: bold; margin-bottom: 4px; }
        .champ input, .champ select { width: 100%; padding: 6px; box-sizing: border-box; }
    </style>
</head>
<body style="font-family:sans-serif;max-width:560px;margin:20px auto;padding:0 16px;">
<jsp:include page="/WEB-INF/views/layout/nav.jsp"/>
<h1><c:choose>
    <c:when test="${modeEdition}">Modifier l'utilisateur</c:when>
    <c:otherwise>Nouvel utilisateur</c:otherwise>
</c:choose></h1>
<jsp:include page="/WEB-INF/views/layout/messages.jsp"/>

<form method="post" action="${pageContext.request.contextPath}/admin/utilisateurs">
    <c:choose>
        <c:when test="${modeEdition}">
            <input type="hidden" name="action" value="modifier">
            <input type="hidden" name="id" value="<c:out value='${idEdition}'/>">
        </c:when>
        <c:otherwise>
            <input type="hidden" name="action" value="creer">
        </c:otherwise>
    </c:choose>

    <div class="champ"><label for="nom">Nom</label>
        <input type="text" id="nom" name="nom" required maxlength="100" value="<c:out value='${valeurs.nom}'/>"></div>
    <div class="champ"><label for="prenom">Prénom</label>
        <input type="text" id="prenom" name="prenom" required maxlength="100" value="<c:out value='${valeurs.prenom}'/>"></div>
    <div class="champ"><label for="email">Email</label>
        <input type="email" id="email" name="email" required value="<c:out value='${valeurs.email}'/>"></div>

    <c:if test="${not modeEdition}">
        <div class="champ"><label for="motDePasse">Mot de passe (8 caractères minimum)</label>
            <input type="password" id="motDePasse" name="motDePasse" required minlength="8"></div>

        <div class="champ"><label for="role">Rôle</label>
            <select id="role" name="role" onchange="basculerCommerce()">
                <option value="CLIENT" <c:if test="${valeurs.role == 'CLIENT'}">selected</c:if>>Client</option>
                <option value="COMMERCANT" <c:if test="${valeurs.role == 'COMMERCANT'}">selected</c:if>>Commerçant</option>
                <option value="ADMIN" <c:if test="${valeurs.role == 'ADMIN'}">selected</c:if>>Admin</option>
            </select></div>

        <fieldset id="commerce" style="margin-bottom:14px;display:none;">
            <legend>Commerce du commerçant</legend>
            <div class="champ"><label for="commerceNom">Nom du commerce</label>
                <input type="text" id="commerceNom" name="commerceNom" maxlength="150" value="<c:out value='${valeurs.commerceNom}'/>"></div>
            <div class="champ"><label for="commerceAdresse">Adresse</label>
                <input type="text" id="commerceAdresse" name="commerceAdresse" maxlength="255" value="<c:out value='${valeurs.commerceAdresse}'/>"></div>
            <div class="champ"><label for="commerceVille">Ville</label>
                <input type="text" id="commerceVille" name="commerceVille" maxlength="100" value="<c:out value='${valeurs.commerceVille}'/>"></div>
            <div class="champ"><label for="commerceCategorie">Catégorie</label>
                <input type="text" id="commerceCategorie" name="commerceCategorie" maxlength="50" value="<c:out value='${valeurs.commerceCategorie}'/>"></div>
            <div class="champ"><label for="commerceTelephone">Téléphone (facultatif)</label>
                <input type="text" id="commerceTelephone" name="commerceTelephone" maxlength="20" value="<c:out value='${valeurs.commerceTelephone}'/>"></div>
        </fieldset>
        <script>
            function basculerCommerce() {
                var role = document.getElementById('role').value;
                document.getElementById('commerce').style.display = (role === 'COMMERCANT') ? 'block' : 'none';
            }
            basculerCommerce();
        </script>
    </c:if>

    <p>
        <button type="submit" style="padding:8px 18px;background:#2e7d32;color:#fff;border:0;border-radius:4px;cursor:pointer;">
            <c:choose><c:when test="${modeEdition}">Enregistrer</c:when><c:otherwise>Créer l'utilisateur</c:otherwise></c:choose>
        </button>
        <a href="${pageContext.request.contextPath}/admin/dashboard" style="margin-left:12px;">Annuler</a>
    </p>
</form>
</body>
</html>

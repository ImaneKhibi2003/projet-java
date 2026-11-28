<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Page d'erreur générique (A4.11). Volontairement sans menu : elle doit s'afficher même si la session pose problème.
     La personne B peut la styliser (B4.10) en gardant les attributs :
     codeErreur, titreErreur, messageErreur, referenceErreur, lienRetour, libelleRetour --%>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${codeErreur}"/> - <c:out value="${titreErreur}"/></title>
</head>
<body style="font-family:sans-serif;max-width:560px;margin:60px auto;padding:0 16px;text-align:center;">
<div style="font-size:5em;font-weight:bold;color:#2e7d32;line-height:1;"><c:out value="${codeErreur}"/></div>
<h1 style="margin:10px 0;"><c:out value="${titreErreur}"/></h1>
<p style="color:#444;font-size:1.1em;"><c:out value="${messageErreur}"/></p>
<c:if test="${not empty referenceErreur}">
    <p style="color:#777;">Référence de l'incident : <strong style="font-family:monospace;"><c:out value="${referenceErreur}"/></strong></p>
</c:if>
<p style="margin-top:30px;">
    <a href="<c:out value='${lienRetour}'/>"
       style="padding:10px 20px;background:#2e7d32;color:#fff;border-radius:4px;text-decoration:none;">
        <c:out value="${libelleRetour}"/></a>
</p>
</body>
</html>

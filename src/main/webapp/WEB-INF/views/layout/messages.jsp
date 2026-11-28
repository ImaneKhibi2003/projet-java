<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Messages flash : provisoire (A4.1), à remplacer par la version de B (B4.1) --%>
<c:if test="${not empty flashSucces}">
    <div class="alert alert-succes" style="padding:10px;margin:10px 0;background:#d4edda;color:#155724;border-radius:4px;">
        <c:out value="${flashSucces}"/>
    </div>
</c:if>
<c:if test="${not empty flashErreur}">
    <div class="alert alert-erreur" style="padding:10px;margin:10px 0;background:#f8d7da;color:#721c24;border-radius:4px;">
        <c:out value="${flashErreur}"/>
    </div>
</c:if>

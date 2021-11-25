<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>

<petclinic:layout pageName="achievements">
    <h2>Achievements</h2>

    <table id="AchievementsTable" class="table table-striped">
        <thead>
        <tr>
            <th style="width: 150px;">Description</th>
            <th style="width: 200px;">Condition</th>
            <th style="width: 120px">Last Change</th>
            <th style="width: 120px">Action</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach items="${achievements}" var="achievement">
            <tr>
                <td>
                    <c:out value="${achievement.condition}"/>
                </td>
                <td>
                    <c:out value="${achievement.lastChange}"/>
                </td>   
                <td>
                    <c:out value="${achievement.description}"/>
                </td>   
                <td>
                    <spring:url value="/achievements/{achievementsId}/delete" var="deleteUrl">
                        <spring:param name = "achievementsId" value = "${achievement.id}"/>
                    </spring:url>
                    <a href="${fn:escapeXml(deleteUrl)}" class="btn btn-default">Delete</a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</petclinic:layout>
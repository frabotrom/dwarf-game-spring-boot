<%@ tag trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ attribute name="pageName" required="true" %>
<%@ attribute name="customScript" required="false" fragment="true"%>
<%@attribute name="customHeader" fragment="true" required="false" %>

<!doctype html>
<html>
<petclinic:htmlHeader>
	<jsp:attribute name="customHeader">
        <jsp:invoke fragment="customHeader"/>
    </jsp:attribute>
    <jsp:body>
    </jsp:body>
</petclinic:htmlHeader>

<body>
<petclinic:bodyHeader menuName="${pageName}"/>

<div class="container-fluid">
    <div class="container xd-container">
	<c:if test="${not empty message}" >
	<div class="alert alert-${not empty messageType ? messageType : 'info'}" role="alert">
  		<c:out value="${message}"></c:out>
   		<button type="button" class="close" data-dismiss="alert" aria-label="Close">
    		<span aria-hidden="true">&times;</span>
  		</button> 
	</div>
	</c:if>

        <jsp:doBody/>

       
    </div>
</div>

<jsp:invoke fragment="customScript" />

</body>

</html>

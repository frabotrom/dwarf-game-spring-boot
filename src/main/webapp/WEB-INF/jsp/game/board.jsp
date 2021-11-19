<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="game" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="board tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="cartasMontaña" tagdir="/WEB-INF/tags" %>

<!-- %@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %-->  

<petclinic:layout pageName="board">


    <h2><fmt:message key="Board Example"/></h2>
    
    <p>	
    <h2><c:out value="${now}"/></h2>

    <div class="row">
        <div class="col-md-12">
            <game:board board="${board}"/>
            <!--
            <c:forEach items="${board.cards}" var="card">
            	<game:card size="100" card="${card}"/>
            	
            </c:forEach> 
            -->
        </div>
    </div>
</petclinic:layout>
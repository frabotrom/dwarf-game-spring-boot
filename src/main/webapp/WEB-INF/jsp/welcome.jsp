<%@ page session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="petclinic" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<petclinic:layout pageName="home">
    <div class="row">
        <div class="bodyBackground">
            <div class="col-sm-12">
                <a class="btn btn-welcome"
                    href='<spring:url value="/Aboutus" htmlEscape="true"/>'>About Us</a>
            </div>
            <div id="fondo1"></div>
        </div>
    </div>
    <script>
        function redirectOnClickLogin() {
            document.location = "/login";
        }
    </script>
    <script>
        function redirectOnClickRegister() {
            document.location = "/usersDwarf/register";
        }
    </script>

    <br><br>

    <div class="row">

        <div class="col-md-8">
        <h2>Top 3 Players</h2>
            <table class="table table-striped" style="width: 75%;">
                <tr>
                    <th>Gold:</th>
                    <td><c:out value="${p1} -- ${p1wg} victorias"/></td>
                </tr>
                <tr>
                    <th>Silver:</th>    
                    <td><c:out value="${p2} -- ${p2wg} victorias"/></td>
                </tr>
                <tr>
                    <th>Bronce:</th>
                    <td><c:out value="${p3} -- ${p3wg} victorias"/></td>
                </tr>
            </table>
        </div>  

        <div class="col-sm-4">
            <iframe width="430" height="260" src="https://www.youtube.com/embed/EVpoFB6eA3I?start=11&version=3&loop=1&playlist=EVpoFB6eA3I&mute=1" title="YouTube video player" 
            frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
        </div>

    </div>

<br><br><br>

    <div class="row">
        <div class="col-md-8">
        <h2>Global Statistics</h2>
        <table class="table table-striped" style="width: 75%;" >
            <tr>
                <th>Global time played:</th>
                <td><c:out value="${ttp}"/></td>
            </tr>
            <tr>
                <th>Global games played:</th>
                <td><c:out value="${tgp}"/></td>
            </tr>
            <tr>
                <th>Global games won:</th>
                <td><c:out value="${tgw}"/></td>
            </tr>
            <tr>
                <th>Global iron mined:</th>
                <td><c:out value="${ti}"/></td>
            </tr>
            <tr>
                <th>Global gold mined:</th>
                <td><c:out value="${to}"/></td>
            </tr>
            <tr>
                <th>Global steel forged:</th>
                <td><c:out value="${ts}"/></td>
            </tr>
            <tr>
                <th>Global objects acquired:</th>
                <td><c:out value="${tob}"/></td>
            </tr>
            <tr>
                <th>Global medal acquired:</th>
                <td><c:out value="${tm}"/></td>
            </tr>
        </table>
        </div>
    </div>

</petclinic:layout>
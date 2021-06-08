<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<html>
    <body>
        <h1>Test ${idx}</h1>
        <form action="/quiz?test=${idx}" method="post">
            <c:forEach items="${test.getQuestions()}" var="q" varStatus="loop">
                <div>
                  <label>${q.question}</label><br>
                  <c:forEach items="${q.getAnswers()}" var="a">
                      <div>
                        <input type="checkbox" id="${idx}-${loop.index}" name="${loop.index}" value="${a}" >
                        <label for="${loop.index}">${a}</label><br>
                      </div>
                  </c:forEach>
                </div>
            </c:forEach>
            <c:if test="${idx eq testSize}">
                <input type="submit" value="Submit">
            </c:if>
            <c:if test="${idx lt testSize}">
                <input type="submit" value="Next">
            </c:if>
        </form>
    </body>
</html>

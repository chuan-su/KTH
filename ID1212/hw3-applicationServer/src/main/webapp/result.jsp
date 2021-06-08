<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<html>
    <body>
        <h1>Result</h1>
        <c:forEach items="${quiz.getTests()}" var="t" varStatus="loop">
            <h3>Test ${loop.index + 1}</h3>
            <c:forEach items="${t.getQuestions()}" var="q">
                <div>
                  <label>${q.question}</label><br>
                  <p><span>Correct Answer: ${q.getCorrectAnswer()}</span></p>
                  <p><span>Your Answer: ${q.getAnswer()}</span></p>
                </div>
            </c:forEach>
        </c:forEach>
    </body>
</html>
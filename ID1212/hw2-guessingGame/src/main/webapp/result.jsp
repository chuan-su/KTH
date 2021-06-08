<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<html>
    <body>
        <c:if test="${guess.getGuess() lt guess.getAnswer()}">
            <p>Nope, guess higher. you have made ${guess.getNumberOfGuess()} guess (es)</p>
            <p>What is your guess?</p>
            <form action="/guess" method="post">
              <input type="text" name="guess" />
              <input type="submit" value="submit"/>
            </form>
        </c:if>
        <c:if test="${guess.getGuess() gt guess.getAnswer()}">
            <p>Nope, guess lower. you have made ${guess.getNumberOfGuess()} guess (es)</p>
            <p>What is your guess?</p>
            <form action="/guess" method="post">
              <input type="text" name="guess" />
              <input type="submit" value="submit"/>
            </form>
        </c:if>
        <c:if test="${guess.getGuess() eq guess.getAnswer()}">
            <p>you made it in ${guess.getNumberOfGuess()} guess(es). Press button to retry</p>
             <form action="/guess" method="post">
                  <input type="submit" value="New Game" />
            </form>
        </c:if>
    </body>
</html>

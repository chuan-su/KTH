package id1212;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class GameServlet extends HttpServlet {

  Quiz quiz;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    quiz = getQuiz();
    req.getRequestDispatcher("start.html").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    int i = Optional.ofNullable(req.getQueryString())
        .map(s -> s.split("="))
        .map(a -> a[1])
        .map(Integer::parseInt)
        .orElse(0);

    if (i > 0) {
      Test t = quiz.getTests().get(i -1);
      for (int q = 0; q < t.getQuestions().size(); q++) {
        String userAnswer = req.getParameter(String.valueOf(q));
        if (userAnswer != null) {
          Question question = t.getQuestions().get(q);
          question.setAnswer(Integer.parseInt(userAnswer));
        }
      }
    }

    if (i <= quiz.getTests().size() - 1) {
      req.setAttribute("test", quiz.getTests().get(i));
      req.setAttribute("idx", i +1);
      req.setAttribute("testSize", quiz.getTests().size());
      req.getRequestDispatcher("test.jsp").forward(req, resp);
    } else {
      req.setAttribute("quiz", quiz);
      req.getRequestDispatcher("result.jsp").forward(req, resp);
    }
  }

  static Quiz getQuiz() {
    Quiz quiz = new Quiz();

    Test test = new Test();
    test.getQuestions().add(new Question("How many soccer players should each team have on the field at the start of each match?", Arrays.asList(8, 9, 10, 11), 11));
    test.getQuestions().add(new Question("When Michael Jordan played for the Chicago Bulls, how many NBA Championships did he win?", Arrays.asList(5, 6,3,2), 6));

    Test test1 = new Test();
    test1.getQuestions().add(new Question("Holes in a standard round of golf?", Arrays.asList(18, 19, 20, 21), 18));
    test1.getQuestions().add(new Question("How many continents are there?", Arrays.asList(3, 4,5,6,7), 7));

//    test.getQuestions().add(new Question("In what year was the first-ever Wimbledon Championship held?", , List.of(1989, 1892, 1879, 1877), 1877));

    quiz.getTests().add(test);
    quiz.getTests().add(test1);

    return quiz;
  }
}

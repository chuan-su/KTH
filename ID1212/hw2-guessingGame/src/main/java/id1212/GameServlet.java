package id1212;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class GameServlet extends HttpServlet {
  private Map<String, Guess> game = new ConcurrentHashMap<>();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Cookie cookie = Optional.ofNullable(req.getCookies())
        .flatMap(cookies -> Stream.of(cookies)
            .filter(c -> "client-id".equals(c.getName()))
            .findFirst())
        .orElseGet(() -> {
          Cookie newCookie = new Cookie("client-id", UUID.randomUUID().toString());
          newCookie.setMaxAge(-1);
          return newCookie;
        });

    if (!game.containsKey(cookie.getValue())) {
      resp.addCookie(cookie);
      game.put(cookie.getValue(), new Guess());
    }

    System.out.println("GET -------- " + cookie.getValue());
    req.getRequestDispatcher("start.html").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Optional<Cookie> cookie = Stream.of(req.getCookies())
        .filter(c -> "client-id".equals(c.getName()))
        .findFirst();

    if (!cookie.isPresent() || !game.containsKey(cookie.get().getValue())) {
      resp.sendRedirect(req.getContextPath() + "/guess");
      return;
    }

    Guess g = game.get(cookie.get().getValue());

    g.setNumberOfGuess(g.getNumberOfGuess() + 1);
    g.setGuess(Integer.parseInt(req.getParameter("guess")));

    System.out.println("ANSWER -------- " + g.getAnswer() + " times: " + g.getNumberOfGuess() + " clientId " + cookie.get().getValue());

    if (g.getAnswer() == g.getGuess()) {
      game.remove(g);
      cookie.get().setMaxAge(0);
      resp.addCookie(cookie.get());
    }

    req.setAttribute("guess", g);
    req.getRequestDispatcher("result.jsp").forward(req, resp);
  }
}

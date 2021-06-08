package id1212;

import com.google.common.io.Resources;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GameServer {
  private Map<String, Guess> game = new ConcurrentHashMap<>();
  private final int port;

  public GameServer(int port) {
    this.port = port;
  }

  public void start() throws Exception {
    ServerSocket serverSocket = new ServerSocket(port);
    System.out.println("server started, listening on port" + port);
    while (true) {
      Socket connection = serverSocket.accept();
      System.out.println("connected " + connection);
      handleIncomingMessage(connection);
    }
  }

  private void handleIncomingMessage(Socket connection) throws Exception {
    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

    String requestLine = in.readLine();
    System.out.println(requestLine);
    String method = requestLine.split(" ")[0];

    String clientId = UUID.randomUUID().toString();

    parseRequest: for (;;) {
      requestLine = in.readLine();

      System.out.println(requestLine);
      if (requestLine == null || requestLine.isEmpty()) {
        break;
      }

      String[] tokens = requestLine.split(":");


      if ("Cookie".equals(tokens[0])) {
        String[] cookies = tokens[1].split(";");

        parseCookie: for(int i = 0; i < cookies.length; i++) {
          String[] cookiePair = cookies[i].split("=");
          if ("client-id".equals(cookiePair[0].trim())) {
            clientId = cookiePair[1].trim();
          }
        }
      }
    }

    if (!game.containsKey(clientId)) {
      game.put(clientId, new Guess());
    }

    if (method.equals("GET")) {
      String response = loadResource("start.txt");
      sendResponse(connection, response, clientId, "guess");
    }

    if (method.equals("POST")) {
      StringBuilder stringBuilder = new StringBuilder();
      while (in.ready()) {
        stringBuilder.append((char)in.read());
      }
      String answer = stringBuilder.toString().split("=")[1];

      Guess g = game.get(clientId);
      g.setNumberOfGuess(g.getNumberOfGuess() + 1);
      g.setGuess(Integer.parseInt(answer.trim()));

      System.out.println("Correct answer is" + g.getAnswer());

      String response;
      String path = "guess";
      if (g.getGuess() < g.getAnswer()) {
        response = loadResource("fail.txt");
        response = response.replace("{times}", String.valueOf(g.getNumberOfGuess()));
        response = response.replace("{level}", "higher");
      } else if (g.getGuess() > g.getAnswer()) {
        response = loadResource("fail.txt");
        response = response.replace("{times}", String.valueOf(g.getNumberOfGuess()));
        response = response.replace("{level}", "lower");
      } else {
        response = loadResource("success.txt");
        response = response.replace("{times}", String.valueOf(g.getNumberOfGuess()));
        game.put(clientId, new Guess());
      }
      sendResponse(connection, response, clientId, path);
    }
  }

  private void sendResponse(Socket socket,  String response, String clientId, String path) throws Exception {
    OutputStream out=new BufferedOutputStream(socket.getOutputStream());
    PrintStream writer = new PrintStream(out, true);
    writer.print(
        "HTTP/1.1 200 OK" + System.lineSeparator() +
            "Connection: keep-alive" + System.lineSeparator() +
            "Date: " + new Date() + System.lineSeparator() +
            "Set-Cookie: client-id=" + clientId + "; " + "Path=/" + path + System.lineSeparator() +
            "Content-Type: text/html" + System.lineSeparator()+
            "Content-length: " + response.length() + System.lineSeparator() + System.lineSeparator() +
            response
    );
    writer.close();
  }
  private String loadResource(String fileName) throws Exception {
    return Resources.toString(Resources.getResource(fileName), StandardCharsets.UTF_8);
  }

  public static void main(String... args) throws Exception {
    new GameServer(5000).start();
  }
}

package hw1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
  private final ExecutorService exec = Executors.newCachedThreadPool();
  private final List<Socket> connections = new ArrayList<>();

  private final int port;

  public ChatServer(int port) {
    this.port = port;
  }

  public void start() throws IOException {
    ServerSocket serverSocket = new ServerSocket(port);
    System.out.println("server started, listening on port" + port);
    while (true) {
      Socket connection = serverSocket.accept();
      System.out.println("connected " + connection);
      connections.add(connection);
      exec.execute(() -> handleIncomingMessage(connection));
    }
  }

  public void stop() {
    exec.shutdown();
  }

  private void handleIncomingMessage(Socket connection) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        broadcast(connection, inputLine);
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private void broadcast(Socket sender, String message) {
      for(Socket conn: connections) {
        if (conn.equals(sender)) {
          continue;
        }
        try {
          PrintWriter writer = new PrintWriter(conn.getOutputStream(), true);
          writer.println(message);
        } catch (IOException ex) {
          System.out.println("error broadcast " + conn);
          ex.printStackTrace();
        }
      }
  }


  public static void main(String... args) throws IOException {
    int port = Integer.parseInt(args[0]);
    new ChatServer(port).start();
  }
}

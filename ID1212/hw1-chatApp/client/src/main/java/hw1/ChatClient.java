package hw1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient {

  private final String host;
  private final int port;

  private final ExecutorService exec = Executors.newSingleThreadExecutor();

  public ChatClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void start() {
    try (Socket socket = new Socket(host, port);
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true))
    {
      System.out.println("connected to chat server " + socket);

      exec.execute(() -> handleReply(socket));

      // read user input and send to chat server
      String userInput;
      while ((userInput = stdIn.readLine()) != null) {
        out.println(userInput); // send message
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private void handleReply(Socket socket) {
    try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        System.out.println("recv : " + inputLine);
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  public static void main(String... args) throws Exception {
    String host = args[0];
    int port = Integer.parseInt(args[1]);
    new ChatClient(host, port).start();
  }
}

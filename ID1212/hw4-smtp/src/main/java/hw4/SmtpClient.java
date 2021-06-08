package hw4;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SmtpClient {

  private static final String KTH_USER = "chuans";
  private static final String KTH_PASSWORD = "Wangyifei1990127";

  public static void main(String[] args) throws Exception {
    sendMail();
//    readMail();
  }

  private static void readMail() throws Exception {

    SSLSocket socket = (SSLSocket)(SSLSocketFactory.getDefault()).createSocket("webmail.kth.se", 993);


    final BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    String line;

    PrintWriter out = new PrintWriter(socket.getOutputStream(), false);
    out.print("AUTH LOGIN " + KTH_USER + " " + KTH_PASSWORD + "\r\n");
    out.print("tag LIST \"\" *\r\n");
    out.print("tag SELECT INBOX\r\n");
    out.print("tag FETCH 1 BODY[TEXT]\r\n");
    Thread.sleep(1000);
    out.flush();

    while((line = br.readLine()) != null) {
      System.out.println("SERVER: "+ line);
    }
  }

  private static void sendMail() throws Exception {

    SSLSocketFactory sf = ((SSLSocketFactory) SSLSocketFactory.getDefault());

    Socket socket = new Socket("smtp.kth.se", 587);

    PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), false);

    final BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out.print("EHLO smtp.kth.se\r\n");
    out.flush();

    for (int i = 0; i <9; i++) {
      System.out.println("SERVER: "+ br.readLine());
    }

    out.print("STARTTLS\r\n");
    out.flush();
    System.out.println("SERVER: "+ br.readLine());


    // TLS -----------------
    System.out.println();
    System.out.println("TLS ------------");
    System.out.println();

    SSLSocket s = (SSLSocket) (sf.createSocket(
        socket, socket.getInetAddress().getHostAddress(), socket.getPort(), true));
    s.setUseClientMode(true);

    s.setEnabledProtocols(s.getSupportedProtocols());
    s.setEnabledCipherSuites(s.getSupportedCipherSuites());
    socket = s;

    s.startHandshake();

    PrintWriter out2 = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), false);

    final BufferedReader br2 = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    out2.print("EHLO smtp.kth.se\r\n");
    out2.flush();

    for (int i = 0; i <9; i++) {
      System.out.println("SERVER: "+ br2.readLine());
    }


    System.out.println();
    System.out.println("AUTH LOGIN ------------");
    System.out.println();

    String userName = Base64.getEncoder().encodeToString(KTH_USER.getBytes(StandardCharsets.UTF_8));
    String password = Base64.getEncoder().encodeToString(KTH_PASSWORD.getBytes(StandardCharsets.UTF_8));

    out2.print("AUTH LOGIN" +"\r\n");
    out2.flush();
    System.out.println(br2.readLine());

    out2.print(userName+"\r\n");
    out2.flush();
    System.out.println(br2.readLine());

    out2.print(password+"\r\n");
    out2.flush();
    System.out.println(br2.readLine());


    System.out.println();
    System.out.println("SEND MAIL ------------");
    System.out.println();

    out2.print("MAIL FROM:<chuans@kth.se>" + "\r\n");
    out2.flush();
    System.out.println(br2.readLine());

    out2.print("RCPT TO:<chuan.su@outlook.com>" + "\r\n");
    out2.flush();
    System.out.println(br2.readLine());
    out2.print("DATA\r\n");
    out2.flush();

    out2.print("Subject: Smtp Socket ID1212\r\n");
    out2.print("ID1212 Network programming\r\n");
    out2.print(".\r\n");
    out2.flush();
    System.out.println(br2.readLine());
  }
}

package hw5;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;

public class RmiServer implements FetchMail {

  private static final String KTH_MAIL_USER = "chuans";
  private static final String KTH_MAIL_PASSWORD = "Wangyifei1990127";

  @Override
  public String fetchMail() {

    try {
      Properties properties = new Properties();
      properties.setProperty("mail.imap.ssl.enable", "true");
      properties.put("mail.imap.host", "webmail.kth.se");
      properties.put("mail.imap.port", "993");

      Session emailSession = Session.getDefaultInstance(properties);

      Store store = emailSession.getStore("imap");
      System.out.println("conneting..........");
      store.connect("webmail.kth.se", KTH_MAIL_USER, KTH_MAIL_PASSWORD);

      Folder emailFolder = store.getFolder("INBOX");

      emailFolder.open(Folder.READ_ONLY);
      System.out.println("messsage count " + emailFolder.getMessageCount());

      Message message = emailFolder.getMessage(1);

      message.writeTo(System.out);
      emailFolder.close(false);
      store.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return "Hello RMI";
  }

  public static void main(String... args) {

    try {
      Registry registry = LocateRegistry.getRegistry();

      RmiServer obj = new RmiServer();
      FetchMail stub = (FetchMail) UnicastRemoteObject.exportObject(obj, 0);

      registry.rebind("Hello", stub);
      System.out.println("Server ready");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

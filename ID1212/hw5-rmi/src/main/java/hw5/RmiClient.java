package hw5;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiClient {

  public static void main(String... args) throws Exception {
    try {
      Registry registry = LocateRegistry.getRegistry();
      FetchMail stub = (FetchMail) registry.lookup("Hello");

      System.out.println("stub " + stub);
      String response = stub.fetchMail();
      System.out.println("response: " + response);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

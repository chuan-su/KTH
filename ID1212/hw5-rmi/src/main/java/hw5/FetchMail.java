package hw5;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FetchMail extends Remote {
  String fetchMail() throws RemoteException;
}

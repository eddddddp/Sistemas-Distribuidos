package es.ubu.lsi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Madre extends Remote {
	/**
	 * Devuelve un texto con comentarios de madre.
	 *
	 * @return texto de saludo
	 * @throws RemoteException problema en acceso remoto
	 */
    String saludarMundo() throws RemoteException;
    
    String contradecirPadre() throws RemoteException;
    
    String mimarMundo() throws RemoteException;
    
} // Madre

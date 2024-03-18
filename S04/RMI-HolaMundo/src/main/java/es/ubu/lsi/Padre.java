package es.ubu.lsi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface remota.
 */
public interface Padre extends Remote {

	/**
	 * Devuelve un texto con comentarios de padre.
	 *
	 * @return texto para el cliente
	 * @throws RemoteException problema en acceso remoto
	 */
	String darNombre() throws RemoteException;

	String darApellido() throws RemoteException;

	String emanciparMundo() throws RemoteException;

} // Padre
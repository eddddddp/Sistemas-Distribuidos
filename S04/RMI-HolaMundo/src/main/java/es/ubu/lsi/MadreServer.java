package es.ubu.lsi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class MadreServer implements Madre {

	public String saludarMundo() throws RemoteException {
		return "Hola mi mundito bonito!";
	}

	public String contradecirPadre() throws RemoteException {
		return "No le digas eso al mundo que te doy...";
	}

	public String mimarMundo() throws RemoteException {
		return "No le hagas caso mi mundito querido.";
	}

	/**
	 * Método raíz.
	 *
	 * @param args argumentos
	 */
	public static void main(String args[]) {

		try {
			Servidor objM = new Servidor();

			// si no hereda de UnicastRemoteObject es necesario exportar
			Madre stubMadre = (Madre) UnicastRemoteObject.exportObject(objM, 0);

			// Liga el resguardo de objeto remoto en el registro
			Registry registro = LocateRegistry.getRegistry();
			registro.bind("Madre", stubMadre);

			System.out.println("Servidor madre preparado");
		} catch (Exception e) {
			System.err.println("Error en el servidor madre: " + e.toString());
			e.printStackTrace();
		}
	} // main

}

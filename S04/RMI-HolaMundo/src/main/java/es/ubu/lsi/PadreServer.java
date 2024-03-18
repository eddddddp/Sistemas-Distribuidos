package es.ubu.lsi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

/**
 * Servidor remoto Padre
 * 
 * @author Eduardo Manuel Cabeza Lopez
 *
 */
public class PadreServer implements Padre{

	/**
	 * 
	 */
	public String darNombre() {
		String[] nombres = {"Rodolfo", "Eustaquio", "Galleto", "Ramiro", "Teodoro"};
		Random rand = new Random();
		int numRand = rand.nextInt(4);
		return "Hola hijo, te llamarás " + nombres[numRand];
	}
	
	/**
	 * 
	 */
	public String darApellido() {
		String[] apellidos = {"Sandemetrio", "Sazón", "De Maria", "Sorda", "Armario"};
		Random rand = new Random();
		int numRand = rand.nextInt(4);
		return "Y tu apellido será " + apellidos[numRand];
	}

	/**
	 * 
	 */
	public String emanciparMundo() {		
		return "Vete y buscate la vida no te aguanto más, mundo!";
	}

	public static void main (String args[]) {
		try {
			Servidor objP = new Servidor();
			
			// No hereda de UnicastRemotyeObject. Exportamos
			Padre stubPadre = (Padre) UnicastRemoteObject.exportObject(objP, 0);
			
			// Ligar el reguardo de objeto remoto en el registro
			Registry registro = LocateRegistry.getRegistry();
			registro.bind("Padre",stubPadre);
			
			System.out.println("Servidor padre preparado");
		} catch (Exception  e) {
			System.err.println("Error en el servidor padre: " + e.getMessage());
		} 
	} // Main
} // PadreServer

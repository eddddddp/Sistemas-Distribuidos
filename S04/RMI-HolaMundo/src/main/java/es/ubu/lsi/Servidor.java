package es.ubu.lsi;
	
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Servidor remoto.
 *
 */	
public class Servidor implements HolaMundo {
	
	/**
	 * {@inheritDoc}.
	 *
	 * @return {@inheritDoc}
	 */
    public String decirHola() {
		return "Hola mundo!";
    }
    
    public String comentarioTiempo() {
    	return "Que buen dia hace sr. mundo!.";
    }
    
    public String decirAdios() {
    	return "Adios mundo!";
    }
	
	/**
	 * Método raíz.
	 *
	 * @param args argumentos
	 */
    public static void main(String args[]) {
	
		try {
		    Servidor servidorRemoto = new Servidor();
		    Padre padre = new PadreServer();
		    Madre madre = new MadreServer();
		    
		    // si no hereda de UnicastRemoteObject es necesario exportar
	    	HolaMundo stub = (HolaMundo) UnicastRemoteObject.exportObject(servidorRemoto, 0);
	    	Padre stubP = (Padre) UnicastRemoteObject.exportObject(padre, 0);
	    	Madre stubM = (Madre) UnicastRemoteObject.exportObject(madre, 0);
		    // Liga el resguardo de objeto remoto en el registro
	    	Registry registro = LocateRegistry.getRegistry();
	    	registro.bind("Hola", stub);
	    	registro.bind("Padre", stubP);
	    	registro.bind("Madre", stubM);
	
	    	System.out.println("Servidor preparado");
		}
		catch (Exception e) {
		    System.err.println("Excepción de servidor: " + e.toString());
		}
    } // main
    
} // Servidor
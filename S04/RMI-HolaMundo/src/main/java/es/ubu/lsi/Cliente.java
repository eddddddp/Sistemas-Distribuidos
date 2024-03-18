package es.ubu.lsi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Cliente remoto.
 */
public class Cliente {

	/**
	 * Constructor oculto,
	 */
    private Cliente() {}


	/**
	 * Método raíz.
	 *
	 * @param args host con el registro
	 */
    public static void main(String[] args) {

		String host = (args.length < 1) ? null : args[0];
		try {
		   Registry registry = LocateRegistry.getRegistry(host);
		   // Resuelve el objeto remoto (la referencia a...)
	 	   HolaMundo stub = (HolaMundo) registry.lookup("Hola");
	 	   Padre stubPadre = (Padre) registry.lookup("Padre");
	 	   Madre stubMadre = (Madre) registry.lookup("Madre");
	 	   System.out.println("Mundo: Hola!");
	 	   // Respuesta server
	 	   String respuesta = stub.decirHola();	 	   
	       System.out.println("Respuesta del servidor remoto: " + respuesta);
	       // Respuesta Padre
	       respuesta = stubPadre.darNombre();	       
	       System.out.println("Respuesta del servidor padre: " + respuesta);
	       respuesta = stubPadre.darApellido();	       
	       System.out.println("Respuesta del servidor padre: " + respuesta);
	       // Respuesta madre
	       respuesta = stubMadre.saludarMundo();	       
	       System.out.println("Respuesta del servidor madre: " + respuesta);
	       // Comentario del tiempo
	       respuesta = stub.comentarioTiempo();
	       System.out.println("Respuesta del servidor remoto: " + respuesta);
	       // Padre hecha a mundo
	       respuesta = stubPadre.emanciparMundo();
	       System.out.println("Respuesta del servidor padre: " + respuesta);
	       // Madre contradice
	       respuesta = stubMadre.contradecirPadre();	       
	       System.out.println("Respuesta del servidor madre: " + respuesta);
	       // Madre mima mundo
	       respuesta = stubMadre.mimarMundo();     
	       System.out.println("Respuesta del servidor madre: " + respuesta);
	       // Mundo se despide
	       System.out.println("Respuesta del cliente mundo: Adios a todos y gracias!");
	       // Despedida del servidor remoto
	       respuesta = stub.decirAdios();
	       System.out.println("Despedida del servidor remoto: " + respuesta);
		} 
		catch (Exception e) {
	    	System.err.println("Excepción en cliente: " + e.toString());
	    	e.printStackTrace();
		} // try
		
    } // main
    
} // Cliente
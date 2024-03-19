package es.ubu.lsi.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.ChatMessage.MessageType;

/**
 * Clase que implementa el cliente para el chat.
 * 
 * El envío y la recepción de mensajes se realiza sobre sockets TCP. Se crean y
 * envían los mensajes desde el hilo principal y se crea un hilo paralelo que
 * recibe los mensajes y los muestra en el terminal. Considera que el usuario
 * con nickname "admin" (se ignora el uso de mayúsculas) es el administrador del
 * servidor y podrá apagarlo de forma remota.
 * 
 * @author Eduardo Manuel Cabeza Lopez
 * @version 1.0.0-SNAPSHOT
 * 
 */
public class ChatClientImpl implements ChatClient {

	/**
	 * Socket del cliente.
	 */
	private Socket clientSocket;
	/**
	 * Puerto predeterminado (y fijo) del servidor.
	 */
	private final int PORT = 1500;
	/**
	 * OutputStream para serializar objetos y escribirlos en la salida.
	 */
	ObjectOutputStream output;
	/**
	 * InputStream para deserializar los objetos recibidos.
	 */
	ObjectInputStream input;
	/**
	 * Hostname del servidor.
	 */
	private String serverHost;
	/**
	 * Nickname del usuario del cliente.
	 */
	private String nickname;
	/**
	 * Identificador único asignado poor el servidor.
	 */
	private int id;
	/**
	 * Booleano que indica si el cliente sigue activo.
	 */
	private boolean isAlive;
	/**
	 * Scanner para introducir datos a través del terminal.
	 */
	private Scanner scanner;

	/**
	 * Método constructor del cliente.
	 * 
	 * Instancia un nuevo cliente.
	 * 
	 * @param server   Hostname del servidor
	 * @param nickname Nick del usuario del cliente
	 */
	public ChatClientImpl(String server, String nickname) {
		// Inicializar estado del cliente
		this.serverHost = server;
		this.nickname = nickname;
		this.isAlive = true;
		try {
			// Iniciar el socket y el ObjectOutputStream
			this.clientSocket = new Socket(this.serverHost, this.PORT);
			this.output = new ObjectOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) { // Capturar exceciones de clase IOException
			// Imprimir error
			System.err.println("Error: No se puede iniciar el cliente.");
			System.err.println(e.getMessage());
			// Finalizar con estado != 0
			System.exit(1);
		}
	}

	/**
	 * Método que devuelve el nickname del usuario del cliente.
	 * 
	 * @return nickname Nick del usuario
	 */
	private String getNickname() {
		return this.nickname;
	}

	/**
	 * Método que devuelve el socket del cliente.
	 * 
	 * @return clientSocket socket del cliente
	 */
	private Socket getClientSocket() {
		return this.clientSocket;
	}

	/**
	 * Método que asigna un scanner para el cliente. Se le debe pasar un scanner
	 * desde System.in.
	 * 
	 * @param scanner Objeto de tipo scanner
	 */
	private void setScanner(Scanner scanner) {
		this.scanner = scanner;
	}

	/**
	 * Método que devuelve el scanner del cliente.
	 * 
	 * @return scanner Scanner del cliente
	 */
	private Scanner getScanner() {
		return this.scanner;
	}

	/**
	 * Método que devuelve el ObjectOutputStream del cliente.
	 * 
	 * @return output ObjectOutputStream del cliente
	 */
	private ObjectOutputStream getOutput() {
		return this.output;
	}

	/**
	 * Método que asigna un ObjectInputStream al cliente.
	 * 
	 * @param input ObjectInputStream para el cliente
	 */
	private void setInput(ObjectInputStream input) {
		this.input = input;
	}

	/**
	 * Método que devuelve el InputObjectStream del cliente.
	 * 
	 * @return input InputObjectStream del cliente
	 */
	private ObjectInputStream getInput() {
		return this.input;
	}

	/**
	 * Método que devuelve el id del cliente.
	 * 
	 * @return id Identificador del cliente
	 */
	private int getId() {
		return id;
	}

	/**
	 * Asigna un identificador al cliente.
	 * 
	 * @param id Identificador del cliente
	 */
	private void setId(int id) {
		this.id = id;
	}

	/**
	 * Asigna un valor al atributo isAlive del cliente.
	 * 
	 * @param b Nuevo valor para el atributo
	 */
	private void setIsAlive(boolean b) {
		this.isAlive = b;
	}

	/**
	 * Método que devuelve el valor del atributo isAlive.
	 * 
	 * @return
	 */
	private boolean getIsAlive() {
		return this.isAlive;
	}

	/**
	 * Método start. Inicia el cliente conectandolo al servidor. Crea un hilo
	 * adicional para la recepción e impresión de los mensajes recibidos. El hilo
	 * principal se encarga de recibir la entrada del usuario y de enviar los
	 * mensajes al servidor.
	 * 
	 */
	@Override
	public boolean start() {
		try {
			// Conectar con el servidor. Crea hilo adicional
			connect();
			// Iniciar y asignar scanner para entrada del usuario
			setScanner(new Scanner(System.in));
			// Cadena para almacenar la entrada del usuario
			String msgText;
			// Bucle para entrada de usuario y envio de mensajes al servidor
			while (getIsAlive()) { // Mientras el atributo isAlive valga true
				// Se recoge la entrada del usuario
				msgText = getScanner().nextLine();
				// Caso LOGOUT
				if (msgText.equalsIgnoreCase("LOGOUT")) { // Si es un mensaje de LOGOUT
					// Creamos y enviamos un mensaje vacío con tipo LOGOUT
					ChatMessage msg = new ChatMessage(getId(), MessageType.LOGOUT, "");
					sendMessage(msg);
					// Establecer isAlive a falso para detener el bucle
					setIsAlive(false);
				} else if (msgText.equalsIgnoreCase("SHUTDOWN")) { // Si no, si es mensaje de SHUTDOWN
					if (getNickname().equalsIgnoreCase("ADMIN")) { // Si el usuario es el administrador
						// Crear y enviar mensaje vacío de tipo SHUTDOWN
						ChatMessage msg = new ChatMessage(getId(), MessageType.SHUTDOWN, "");
						sendMessage(msg);
						// Imprimir aviso
						System.out.println("Cerrando el cliente");
						// Establecer isAlive a falso para detener el bucle
						setIsAlive(false);
					} else { // Si no es administrador
						// Imprimir aviso e ignorar
						System.out.println("Solo el admin puede enviar el comando shutdown.");
					}
				} else { // Si no se considera que es de tipo MESSAGE
					// Crear y enviar el mensaje con el contenido introducido por el usuario
					ChatMessage msg = new ChatMessage(getId(), MessageType.MESSAGE, msgText);
					sendMessage(msg);
				}
			}
		} finally {
			// Al finalizar el bucle por cualquier vía se desconecta el cliente
			disconnect();
		}
		return true; // Retornar true
	}

	/**
	 * Método que envía un mensaje al servidor.
	 * 
	 * @param msg ChatMessage a enviar al servidor
	 */
	@Override
	public void sendMessage(ChatMessage msg) {
		try {
			// Serializar y enviar el objeto ChatMessage al servidor
			getOutput().writeObject(msg);
		} catch (IOException e) { // Recoger excepciones de clase IOException
			// Imprimir error
			System.err.println("Error: No se puede enviar el mensaje al servidor.");
			// Imprimir mensaje de la excepción
			System.err.println(e.getMessage());
			// Desconectar el cliente.
			disconnect();
		}
	}

	/**
	 * Método que desconecta el cliente.
	 * 
	 */
	@Override
	public void disconnect() {
		try {
			// Desconectar scanner si es no nulo
			if (getScanner() != null)
				getScanner().close();
			// Cerrar el ObjectOutputStream si es no nulo
			if (getOutput() != null)
				getOutput().close();
			// Cerrar el ObjectInputStream si es no nulo
			if (getInput() != null)
				getInput().close();
			// Cerrar el socket si es no nulo
			if (getClientSocket() != null)
				getClientSocket().close();
			// Establecer isAlive a falso indicando que el cliente no está activo
			setIsAlive(false);	
			//Finalizar con estado 0
			System.exit(0);
		} catch (IOException e) { // Capturar excepciones de clase IOException
			// Imprimir traza de la excepción
			e.printStackTrace();	
			// Finalizar con estado != 0
			System.exit(1);
		} 
	}

	/**
	 * Método que coencta con el servidor. Crea el hilo para la recepción e
	 * impresión de mensajes recibidos. Envía una petición al servidor con su
	 * nickname para su registro y recibe una respuesta con su identificador unico
	 * registrado en el servidor.
	 * 
	 */
	private void connect() {
		// Crear mensaje/petición al servidor para unirse al chat. Enviar mensaje con id
		// = 0 y con el contenido del mensaje el nickname para su registro en el
		// servidor.
		ChatMessage msg = new ChatMessage(0, MessageType.MESSAGE, getNickname());
		try {
			// Crear y asignar al cliente el ObjetcInputStream para recibir los objetos del
			// servidor
			setInput(new ObjectInputStream(getClientSocket().getInputStream()));
			// Enviar la petición al servidor
			sendMessage(msg);
			// Esperar la respuesta del servidor
			msg = (ChatMessage) getInput().readObject();
			// Imprimir la respuesta del servidor
			System.out.println(msg.getMessage());
			// Establecer la id del cliente obtenida del servidor
			setId(msg.getId());
			// Crear y lanzar el hilo que ejecuta el listener para la recepción de mensajes
			new Thread(new ChatClientListener()).start();
		} catch (IOException | ClassNotFoundException e) { // Capturar excepciones IOException y ClassNotFoundException
			// Imprimir error
			System.err.println("Error: No se puede obtener el mensaje del servidor.");
			// Desconectar cliente
			disconnect();
		}
	}

	/**
	 * Método principal. Recibe como argumentos el nickname del cliente o el
	 * hostname del servidor y el nickname del cliente. Si no se le pasa un hostname
	 * se usará localhost por defecto.
	 * 
	 * @param args Argumentos en línea de comandos
	 */
	public static void main(String[] args) {
		// Inicializar cadenas vacias para el hostname del servidor y el nickname
		String server = "";
		String nick = "";

		if (args.length == 1) { // Si se pasa una rgumento se supone nickname
			// establecer server como localhost
			server = "localhost";
			// establecer nick
			nick = args[0];
		} else if (args.length == 2) { // Si no, si se pasan dos argumentos
			// El primero es el hostname
			server = args[0];
			// El segundo es el nickname
			nick = args[1];
		} else { // En caso contrario
			// Imprimir error
			System.err.println("Se esperaban 1 o 2 argumentos: <nickname> o <serverhost> <nickname>");
			// Finalizar el programa
			System.exit(1);
		}
		// Instanciar el cliente con el hostname y el nickname e iniciar.
		new ChatClientImpl(server, nick).start();
	}

	/**
	 * Clase interna que implementa un listener para el cliente que recibe y muestra
	 * los mensajes recibidos del servidor. Implementa la interfaz Runnable para
	 * poder lanzarse con threads en paralelo.
	 * 
	 * @author Eduardo Manuel Cabeza Lopez
	 *
	 */
	class ChatClientListener implements Runnable {

		/**
		 * Método run que inicia el listener y se mantiene a la espera de mensajes del
		 * servidor mientras el cliente siga activo. Cuando recibe un mensaje lo muestra
		 * en el terminal.
		 */
		@Override
		public void run() {

			try {
				while (getIsAlive()) { // Mientras el cliente sigue activo
					// Obtener objeto recibido del servidor
					ChatMessage msg = (ChatMessage) getInput().readObject();
					// Imprimir mensaje recibido
					System.out.println(msg.getMessage());
					if (msg.getType() == MessageType.LOGOUT) { // Si es un mensaje de LOGOUT
						// Desconectar el cliente
						disconnect();						
					}

				}
			} catch (IOException e) { // Capturar excepciones de clase IOException
				// Desactivar el cliente
				setIsAlive(false);
				// Finalizar con estado != 0
				System.exit(1);
			} catch (ClassNotFoundException e) { // Capturar excepciones ClassNotFoundException
				// Imprimir error
				System.err.println("Error: No se puede recibir la respuesta del servidor.");
				// Finalizar con estado != 0
				System.exit(1);
			}
		}
	}
}
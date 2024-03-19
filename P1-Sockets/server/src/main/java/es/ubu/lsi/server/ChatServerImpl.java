package es.ubu.lsi.server;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.ChatMessage.MessageType;

/**
 * Clase que implementa el servidor para el chat.
 * 
 * El envío y recepción de mensajes se realizan sobre sockets TCP. Se instancia
 * un Thread para cada cliente, donde se reciben los mensajes correspondientes.
 * El servidor reenvia los mensajes a todos los usuarios conectados. El hilo
 * principal se mantiene a la espera de peticiones de clientes.
 * 
 * @author Eduardo Manuel Cabeza Lopez
 * @version 1.0.0-SNAPSHOT
 *
 */
public class ChatServerImpl implements ChatServer {

	/**
	 * Puerto fijo para el socket del servidor
	 */
	private static final int PORT = 1500;

	/**
	 * Último identificador proporcionado a un cliente
	 */
	private static int id = 0;

	/**
	 * HashMap que almacena pares nickname-Thread para identificar los hilos de cada
	 * cliente
	 */
	Map<String, ServerThreadForClient> users = new HashMap<String, ServerThreadForClient>();

	/**
	 * HashMap que almacena pares id-nickname para identificar usuarios en el
	 * servidor
	 */
	Map<Integer, String> userIds = new HashMap<Integer, String>();

	/**
	 * Hashmap que almacena pares nickname-boolean donde le booleano indica si se
	 * encuentra bloqueado(true) o no (false). Si un usuario no está en la lista
	 * nunca ha sido bloqueado. Los usuarios bloqueados se mantienen en la lista
	 * mientras el servidor siga en ejecución.
	 */
	Map<String, Boolean> usersBanned = new HashMap<String, Boolean>();

	/**
	 * SimpleDateFormater usado para obtener la hora a la que se produce cada evento
	 * en el servidor
	 */
	private static SimpleDateFormat timeFormat;

	/**
	 * Booleano que indica si el servidor está activo
	 */
	private boolean isAlive;

	/**
	 * Socket del servidor
	 */
	ServerSocket serverSocket;

	/**
	 * Método constructor. Instancia un servidor de chat
	 */
	public ChatServerImpl() {
		// Activar servidor y crear el timeFormat
		this.isAlive = true;
		timeFormat = new SimpleDateFormat("HH:mm");
	}

	/**
	 * Método que inicia el servidor. Inicia el servidor y se mantiene a la espera
	 * de peticiones de clientes. Cuando recibe una petición, la acepta y lanza un
	 * hilo para el cliente y los registra.
	 */
	public void startup() {
		try {
			// Crear y asignar el socket del servidor
			setServerSocket(new ServerSocket(getPort()));
			// Imprimir mensaje
			System.out.println("Servidor escuchando por el puerto " + getPort() + " a las " + getTime());
		} catch (IOException e) { // Capturar excepciones IOException
			// Imprimir error
			System.err.println("Error: No se puede conectar el servidor.");
			// Finalizar con estado != 0
			System.exit(1);
		}
		while (getIsAlive()) { // Mientras el servidor esté activo
			try {
				// Aceptar la conexión y obtener el socket del cliente
				Socket client = getServerSocket().accept();
				// Se crea el hilo en el servidor para el cliente
				ServerThreadForClient clientThread = new ServerThreadForClient(client);
				// Se lanza el hilo
				clientThread.start();
			} catch (IOException e) { // Capturar excepciones IOException
				// Imprimir error
				System.err.println("Error: No se puede aceptar la conexión con el cliente.");
			}
		}
	}

	/**
	 * Método que apaga el servidor. Cierra todos los clientes en el servidor y
	 * después cierra su socket.
	 */
	public void shutdown() {
		// Desactivar el servidor
		setIsAlive(false);
		try {
			// Cerrar todos los clientes
			for (ServerThreadForClient client : getUsers().values()) {
				client.closeClient();
			}
			// Cerrar el socket del servidor si es no nulo
			if (getServerSocket() != null) {
				getServerSocket().close();
			}
			// Finalizar con estado 0
			System.exit(0);
		} catch (IOException e) { // Capturar excepciones IOException
			// Imprimir error
			System.err.println("Error: Fallo al apagar el servidor.");
			// Finalizar con estado != 0
			System.exit(1);
		}
	}

	/**
	 * Método que realzia el broadcast de los mensajes recibidos por los clientes.
	 * Crea y envía un mensaje para cada uno de los clientes conectados al servidor.
	 */
	public void broadcast(ChatMessage message) {
		// Obtener nick del remitente
		String nick = getNickname(message.getId());
		// Si está bloqueado lo ignoramos
		if (getusersBanned().getOrDefault(nick, false)) {
			return;
		}
		// Para cada cliente, crear y enviar un mensaje con el contenido y tipo de
		// message
		for (ServerThreadForClient client : getUsers().values()) {
			// Crear mensaje
			ChatMessage newMsg = new ChatMessage(message.getId(), message.getType(),
					getNickname(message.getId()) + " " + getTime() + ": " + message.getMessage());
			try {
				// Enviar mensaje
				client.output.writeObject(newMsg);
			} catch (IOException e) { // Capturas excepciones IOException
				// Imprimir error
				System.err.println("Error: No ha sido posible enviar el mensaje al cliente.");
				// Eliminar el cliente con el que se ha perdido la comunicación
				remove(client.getClientId());
			}
		}
	}

	/**
	 * Método que devuelve el nickname de un usuario dado un identificador.
	 * 
	 * @param id Identificador del cliente
	 * @return nickname Nickname del cliente
	 */
	public String getNickname(int id) {
		return getUserIds().get(id);
	}

	/**
	 * Método que elimina un cliente del servidor.
	 * 
	 * @param id Identificador del cliente
	 */
	@Override
	public void remove(int id) {
		// Obtener nickname del cliente
		String nick = getNickname(id);
		if (nick != null) { // Si no es nulo
			// Obtenemos el cliente y lo eliminamos del Hashmap de hilos y clientes
			ServerThreadForClient client = getUsers().remove(nick);
			if (client != null) { // Si no es nulo
				// Cerrar el hilo del cliente en el servidor
				client.closeClient();
				// Eliminar del HashMap de nicknames y identificadores
				getUserIds().remove(id);
				// Imprimir mensajes en el servidor
				System.out.println("El usuario " + nick + " ha sido eliminado del chat a las " + getTime());
				System.out.println("Usuarios conectados actualmente: " + getUsers().size());
			}
		} else { // Si no se encuentra al usuario
			// Imprimir mensaje en el servidor
			System.out.println("No existe el usuario " + nick);
		}
	}

	/**
	 * Método que devuelve la hora con precisión de minutos con el formato hh:mm
	 * 
	 * @return time Hora actual
	 */
	public String getTime() {
		return timeFormat.format(new Date());
	}

	/**
	 * Método principal.
	 * 
	 * @param args No se esperan argumentos en línea de comandos.
	 */
	public static void main(String[] args) {
		// Instanciar el servidor e iniciarlo
		new ChatServerImpl().startup();
	}

	/**
	 * Método synchronized que devuelve el siguiente identificador.Se usa el método
	 * sincronizado para evitar problemas de concurrencia al tener varios hilos
	 * intentando acceder al mismo atributo.
	 * 
	 * @return id Identificador para el próximo cliente
	 */
	private synchronized int getNextId() {
		id += 1;
		return id;
	}

	/**
	 * Método que devuelve el puerto del servidor.
	 * 
	 * @return PORT Puerto del servidor
	 */
	private static int getPort() {
		return PORT;
	}

	/**
	 * Método que devuelve true si el servidor está activo o false en caso
	 * contrario.
	 * 
	 * @return isALive Valor del atribuo isAlive
	 */
	private boolean getIsAlive() {
		return this.isAlive;
	}

	/**
	 * Establece un nuevo valor para el atributo booleano isAlive.
	 * 
	 * @param b Nuevo valor de isALive
	 */
	private void setIsAlive(boolean b) {
		this.isAlive = b;
	}

	/**
	 * Método que devuelve el HashMap de nicknames de clientes e hilos
	 * 
	 * @return users HashMap con clientes y hilos
	 */
	private Map<String, ServerThreadForClient> getUsers() {
		return this.users;
	}

	/**
	 * Método que devuelve el HashMap con identificadores y nicknames de clientes
	 * 
	 * @return userIds HashMap de identificadores y nicknames
	 */
	private Map<Integer, String> getUserIds() {
		return this.userIds;
	}

	/**
	 * Método que devuelve el HashMap de usuarios bloqueados.
	 * 
	 * @return usersBanned HashMap de clientes bloqueados
	 */
	private Map<String, Boolean> getusersBanned() {
		return this.usersBanned;
	}

	/**
	 * Método que asigna un ServerSocket al al servidor.
	 * 
	 * @param serverSocket Socket del servidor
	 */
	private void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	/**
	 * Método que devuelve el socket del servidor.
	 * 
	 * @return serverSocket Socket del servidor
	 */
	private ServerSocket getServerSocket() {
		return this.serverSocket;
	}

	/**
	 * Clase interna que extiende de Thread para lanzar hilos por cada conexión que
	 * se acepta con un cliente.
	 * 
	 * @author Eduardo Manuel Cabeza Lopez
	 *
	 */
	class ServerThreadForClient extends Thread {

		/**
		 * Identificador del cliente
		 */
		private int clientId;

		/**
		 * Booleano que indica si el hilo del cliente está activo
		 */
		private boolean threadAlive;

		/**
		 * Nickname del cliente asociado al hilo
		 */
		private String nickname;

		/**
		 * Socket del cliente asociado el hilo
		 */
		private Socket threadSocket;

		/**
		 * ObjectInputStream del cliente en el servidor
		 */
		private ObjectInputStream input;

		/**
		 * ObjectOutputStream del cliente en el servidor
		 */
		private ObjectOutputStream output;

		/**
		 * Método constructor. Instancia un hilo para un cliente en el servidor.
		 * 
		 * @param socket Socket del cliente asociado al hilo
		 */
		public ServerThreadForClient(Socket socket) {
			// Inicializar el estado del hilo
			this.threadSocket = socket; // Asignar socket
			this.threadAlive = true; // Activar estado del hilo
			try {
				// Crear ObjectOutputStream
				output = new ObjectOutputStream(socket.getOutputStream());
				// Crear ObjectInputStream
				input = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) { // Capturar excepciones IOException
				// Imprimir error
				System.err.println("Error: No ha sido posible crear un hilo para el nuevo cliente.");
			}
		}

		/**
		 * Método que incia el cliente en el servidor. Se mantiene a la espera de un
		 * mensaje del cliente y lo reenvía en caso de ser un mensaje normal. En caso
		 * contrario toma la acción determinada por el tipo de mensaje.
		 */
		@Override
		public void run() {
			try {
				// Conectar el cliente
				connect();
				while (getThreadAlive()) { // Mientras el hilo esté activo
					// Recibir mensaje
					ChatMessage message = (ChatMessage) input.readObject();
					// Realizar acciónes en fnción del tipo del mensaje
					switch (message.getType()) {
					case MESSAGE: // Si es de tipo MESSAGE
						// Procesar mensaje
						processMsg(message);
						break;
					case LOGOUT: // Si es de tipo LOGOUT
						// Eliminar el cliente
						remove(getClientId());
						// Cerrar el hilo del cliente
						closeClient();
						// Imprimir mensaje en el servidor
						System.out.println("Se ha desconectado el usuario " + getNickname() + " a las " + getTime());
						// Desactivar el hilo
						setThreadAlive(false);
						break;
					case SHUTDOWN: // Si es mensaje de tipo SHUTDOWN
						// Si el usuario es administrador
						if (getNickname().equalsIgnoreCase("ADMIN")) {
							// Informar a todos los participantes
							broadcast(new ChatMessage(getClientId(), MessageType.LOGOUT, "Cerrando el servidor."));
							// Imprimir mensaje en el servidor
							System.out.println("Apagando el servidor.");
							// Apagar el servidor
							ChatServerImpl.this.shutdown();
							return;
						} else { // Si no es administrador
							// Informar al usuario que no dispone de permiso para apagar el servidor
							System.out.println("Solo el administrador puede apagar el servidor.");
						}
						break;
					default:
						break;
					}
				}
			} catch (ClassNotFoundException | IOException e) { // Capturar excepciones ClassNotFoundException y
																// IOException
				// Imprimir error
				System.err.println("Error: Conexión perdida con el usuario " + getNickname());
				// Eliminar el cliente con el que no se puede comunicar
				remove(getClientId());
				// Cerrar el cliente con el que no se puede comunicar
				closeClient();
			}
		}

		/**
		 * Método que devuelve el ObjectOutputStream del cliente
		 * 
		 * @return output ObjectOutputStream del cliente
		 */
		private ObjectOutputStream getOutput() {
			return this.output;
		}

		/**
		 * Método que devuelve el ObjectInputStream del cliente
		 * 
		 * @return input ObjectInputStream del cliente
		 */
		private ObjectInputStream getInput() {
			return this.input;
		}

		/**
		 * Método que devuelve el socket del cliente
		 * 
		 * @return threadSocket Socket del cliente
		 */
		private Socket getThreadSocket() {
			return this.threadSocket;
		}

		/**
		 * Método que obtiene el nickname del cliente asociado al hilo
		 * 
		 * @return nickname Nickname del cliente
		 */
		private String getNickname() {
			return this.nickname;
		}

		/**
		 * Método que asigna un nickname de usuario al hilo del cliente en el servidor
		 * 
		 * @param nickname Nickname del usuario
		 */
		private void setNickname(String nickname) {
			this.nickname = nickname;
		}

		/**
		 * Método que devuelve el identificador del cliente asociado al hilo
		 * 
		 * @return clientId Identificador del cliente
		 */
		private int getClientId() {
			return this.clientId;
		}

		/**
		 * Método que asigna el identificador del cliente al hilo asociado
		 * 
		 * @param id Identificador del cliente
		 */
		private void setClientId(int id) {
			this.clientId = id;
		}

		/**
		 * Método que devuelve el valor del atributo threadAlive que indica si el hilo
		 * está activo.
		 * 
		 * @return threadAlive Valor del atributo threadAlive
		 */
		private boolean getThreadAlive() {
			return this.threadAlive;
		}

		/**
		 * Método que modifica el valor del atributo threadAlive con el valor booleano
		 * pasado como argumento.
		 * 
		 * @param b Nuevo valor de threadAlive
		 */
		private void setThreadAlive(boolean b) {
			this.threadAlive = b;
		}

		/**
		 * Método que procesa mensajes de tipo MESSAGE. Comprueba si el remitente se
		 * encuentra bloqueado, en cuyo caso ignora el mensaje. Si no, comprueba si se
		 * trata de un comando válido y toma la acción adecuada en cada caso. Si es un
		 * comando, ejecuta el método correspondiente y, en caso contrario, se difunde
		 * el mensaje.
		 * 
		 * @param message Objeto de clase ChatMessage a procesar
		 */
		private void processMsg(ChatMessage message) {
			// Si remitente esta bloqueado ignoramos su mensaje
			if (getusersBanned().getOrDefault(getNickname(), false)) {
				return;
			}
			// Separar cada palabra del mensaje en una cadena distinta dentro de un array
			String[] words = message.getMessage().split(" ");

			if (words.length == 2) { // Si se tiene 2 cadenas en el mensaje
				// Obtener primera cadena
				String first = words[0];
				// Obtener segunda cadena
				String second = words[1];
				switch (first) { // Decidir acción en función de la primera palabra
				case "ban": // Si es ban
					// Se supone la segunda palabra el nickname del usuario a bloquear
					// Se bloquea al usuario
					bannUser(second, true);
					break;
				case "unban": // Si es unban
					// Se supone que la segunda palabra es el nickname del usuario a desbloquear
					// Desbloquear usuario
					bannUser(second, false);
					break;
				default:
					// Si no es comando de bloqueo/desbloqueo se trata de un mensaje
					// Difundir el mensaje
					broadcast(message);
					break;
				}
			} else { // Si se tiene otro número de palabras
				// Difundir el mensaje
				broadcast(message);
			}
		}

		/**
		 * Método que conecta el cliente con el servidor al instanciar el thread del
		 * cliente en el servidor. El método enía un mensaje de bienvenida al cliente
		 * después de que el servidor acepte la conexión con el cliente. Recibe el
		 * mensaje de petición que se envía desde el cliente con el nickname, el cual se
		 * registra en el servidor. Se genera un identificador para el cliente y se le
		 * envía con el mensaje de bienvenida.
		 * 
		 * @throws IOException            Excepciones por problemas de conexión entre el
		 *                                cliente y el servidor
		 * @throws ClassNotFoundException Excepciones en caso de recibir un objeto de
		 *                                clase inesperada
		 */
		private void connect() throws IOException, ClassNotFoundException {
			// Obtener el mensaje de petición del cliente
			ChatMessage loginMessage = (ChatMessage) getInput().readObject();
			if (loginMessage.getType() == MessageType.MESSAGE) { // Si es de tipo MESSAGE
				// Establecemos el nickname que se recibe en el cuerpo del mensaje
				setNickname(loginMessage.getMessage());
				// Registramos el usuario y el hilo asociado en el HashMap correspondiente
				getUsers().put(getNickname(), this);
				// Generamos y registramos el identificador del cliente
				setClientId(getNextId());
				getUserIds().put(id, getNickname());
				// Enviar mensaje de bienvenida al cliente
				sendWelcome();
				// Imprimir en el servidor el número de clientes conectados
				System.out.println("Clientes conectados actualmente: " + getUsers().size());
			} else { // Si no es de tipo MESSAGE
				// Imprimir error. No se esperan peticiones con mensajes LOGOUT o SHUTDOWN
				System.err.println("Error: Mensaje de tipo inesperado. Abortando la conexión con el cliente.");
				// Eliminar y cerrar el cliente en el servidor
				remove(getClientId());
				closeClient();
				return;
			}
		}

		/**
		 * Método qeu envía el mensaje de bienvenida al nuevo cliente que se conecta al
		 * servidor *
		 */
		private void sendWelcome() {
			try {
				// Crear el cuerpo del mensaje
				String welcomeMessage = String.format("Bienvenido al chat %s. Te hemos asignado el ID %d",
						getNickname(), getClientId());
				// Crear y enviar el mensaje al nuevo usuario
				getOutput().writeObject(new ChatMessage(getClientId(), MessageType.MESSAGE, welcomeMessage));
				// Imprimir mensaje en el servidor
				System.out.println(getNickname() + " se ha conectado al chat.");
				// Informar a todos los usuarios de la nueva incorporación al chat
				broadcast(new ChatMessage(getClientId(), MessageType.MESSAGE, "Se ha conectado al chat."));
			} catch (IOException e) { // Capturar excepciones IOException
				// Imprimir error
				System.err.println("Error: No se puede enviar el mensaje de bienvenida a " + getNickname());
				// Eliminar y cerrar el clietne con el que no podemos comunicarnos
				remove(getClientId());
				closeClient();
			}
		}

		/**
		 * Método que cierra la conexión con el cliente. Cierra, en caso de que estén
		 * activos, el ObjectInputStream del cliente, el ObjectOutputStream del cliente,
		 * el socket del cliente y marca el thread como inactivo en threadAlive.
		 */
		private void closeClient() {
			try {
				// Desactivar hilo
				setThreadAlive(false);
				// Cerrar ObjectInputStream si es no nulo
				if (getInput() != null)
					getInput().close();
				// Cerrar ObjectOutputStream si es no nulo
				if (getOutput() != null)
					getOutput().close();
				// Cerrar el socket del cliente si es no nulo
				if (getThreadSocket() != null)
					getThreadSocket().close();
			} catch (IOException e) { // Capturar excepciones IOException
				// Imprimir error
				System.err.println("Error: No se ha podido desconectar correctamente al usuario " + getNickname());
			}
		}

		/**
		 * Método que bloquea o desbloquea usuarios en función del valor del argumento
		 * ban. Si vale true se bloquea al usuario y si vale false se desbloquea.
		 * 
		 * 
		 * @param nickname Nickname del usuario a bloquear/desbloquear
		 * @param ban      Opción para bloquear/desbloquear
		 */
		private void bannUser(String username, boolean ban) {
			// Insertar usuario a lista de bloqueados o modificar su estado si ya se
			// encuentra en la lista
			getusersBanned().put(username, ban);
			if (ban == true) { // Si se quiere bloquear
				// Imprimir mensaje en el servidor
				System.out.println(username + " ha bloqueado a " + getNickname() + " a las " + getTime());
				// Informar a los participantes del bloqueo
				broadcast(new ChatMessage(getClientId(), MessageType.MESSAGE, " ha bloqueado a " + username));
			} else { // Si se quiere desbloquear
				// Imprimir mensaje en el servidor
				System.out.println(username + " ha sido desbloqueado por " + getNickname() + " a las " + getTime());
				// Informar a todos los participantes del desbloqueo
				broadcast(new ChatMessage(getClientId(), MessageType.MESSAGE, " ha desbloqueado a " + username));
			}
		}

	}

}
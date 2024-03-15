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
 * 
 * @author Eduardo Manuel Cabeza Lopez
 *
 */
public class ChatServerImpl implements ChatServer {

	private static final int PORT = 1500;
	private static int id = 0;
	Map<String, ServerThreadForClient> users = new HashMap<String, ServerThreadForClient>();
	Map<Integer, String> userIds = new HashMap<Integer, String>();
	Map<String, Boolean> usersBanned = new HashMap<String, Boolean>();
	private static SimpleDateFormat timeFormat;
	private boolean isAlive;
	ServerSocket serverSocket;

	public ChatServerImpl() {
		this.isAlive = true;
		timeFormat = new SimpleDateFormat("HH:mm");
	}

	public void startup() {
		try {
			setServerSocket(new ServerSocket(getPort()));
			System.out.println("Servidor escuchando por el puerto " + getPort() + " a las " + getTime());
		} catch (IOException e) {
			System.err.println("Error: No se puede conectar el servidor.");
			System.exit(1);
		}
		while (getIsAlive()) {
			try {

				Socket client = getServerSocket().accept();
				ServerThreadForClient clientThread = new ServerThreadForClient(client);
				clientThread.start();
			} catch (IOException e) {
				System.err.println("Error: No se puede aceptar la conexión con el cliente.");
			}
		}
	}

	public void shutdown() {
		setIsAlive(false);
		try {
			for (ServerThreadForClient client : getUsers().values()) {
				client.closeClient();
			}
			if (getServerSocket() != null) {
				getServerSocket().close();
			}
		} catch (IOException e) {
			System.err.println("Error: Fallo al apagar el servidor.");
		}
	}

	public void broadcast(ChatMessage message) {
		String nick = getNickname(message.getId());
		if (getusersBanned().getOrDefault(nick, false)) {
			return;
		}

		for (ServerThreadForClient client : getUsers().values()) {
			ChatMessage newMsg = new ChatMessage(message.getId(), message.getType(),
					getNickname(message.getId()) + "-" + getTime() + ": " + message.getMessage());
			try {
				client.output.writeObject(newMsg);
			} catch (IOException e) {
				System.err.println("Error: No ha sido posible enviar el mensaje al cliente.");
				remove(client.getClientId());
			}
		}
	}

	public String getNickname(int id) {
		return getUserIds().get(id);
	}

	@Override
	public void remove(int id) {

		String nick = getNickname(id);
		if (nick != null) {
			ServerThreadForClient client = getUsers().remove(nick);
			if (client != null) {
				client.closeClient();
				getUserIds().remove(id);

				System.out.println("El usuario" + nick + " ha sido eliminado del chat a las " + getTime());
				System.out.println("Usuarios conectados actualmente: " + getUsers().size());
			}
		} else {
			System.out.println("No existe el usuario " + nick);
		}
	}

	public String getTime() {
		return timeFormat.format(new Date());
	}

	public static void main(String[] args) {
		new ChatServerImpl().startup();
	}

	private synchronized int getNextId() {
		return id++;
	}

	private static int getPort() {
		return PORT;
	}

	private boolean getIsAlive() {
		return this.isAlive;
	}

	private void setIsAlive(boolean b) {
		this.isAlive = b;
	}

	private Map<String, ServerThreadForClient> getUsers() {
		return this.users;
	}

	private Map<Integer, String> getUserIds() {
		return this.userIds;
	}

	private Map<String, Boolean> getusersBanned() {
		return this.usersBanned;
	}

	private void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	private ServerSocket getServerSocket() {
		return this.serverSocket;
	}

	class ServerThreadForClient extends Thread {

		private int clientId;
		private boolean threadAlive;
		private String nickname;
		private Socket threadSocket;
		private ObjectInputStream input;
		private ObjectOutputStream output;

		public ServerThreadForClient(Socket socket) {
			this.threadSocket = socket;
			this.threadAlive = true;
			try {
				output = new ObjectOutputStream(socket.getOutputStream());
				input = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e) {
				System.err.println("Error: No ha sido posible crear un hilo para el nuevo cliente.");
			}
		}

		@Override
		public void run() {
			try {
				connect();
				while (getThreadAlive()) {
					ChatMessage message = (ChatMessage) input.readObject();
					switch (message.getType()) {
					case MESSAGE:
						// Cliente envía un mensaje
						processMsg(message);
						break;
					case LOGOUT:
						// Cliente solicita la desconexión
						remove(getClientId());
						closeClient();
						System.out.println("Se ha desconectado el usuario" + getNickname() + " a las " + getTime());
						setThreadAlive(false);
						break;
					case SHUTDOWN:
						if (getNickname().equalsIgnoreCase("ADMIN")) {
							System.out.println("Apagando el servidor.");
							ChatServerImpl.this.shutdown();
							return;
						} else {
							System.out.println("Solo el administrador puede apagar el servidor.");
						}
						break;
					default:
						break;
					}
				}

			} catch (ClassNotFoundException | IOException e) {
				System.err.println("Error: Conexión perdida con el usuario " + getNickname());
				remove(getClientId());
				closeClient();
			}
		}

		private ObjectOutputStream getOutput() {
			return this.output;
		}

		private ObjectInputStream getInput() {
			return this.input;
		}

		private Socket getThreadSocket() {
			return this.threadSocket;
		}

		private String getNickname() {
			return this.nickname;
		}

		private void setNickname(String nickname) {
			this.nickname = nickname;
		}

		private int getClientId() {
			return this.clientId;
		}

		private void setClientId(int id) {
			this.clientId = id;
		}

		private boolean getThreadAlive() {
			return this.threadAlive;
		}

		private void setThreadAlive(boolean b) {
			this.threadAlive = b;
		}

		private void processMsg(ChatMessage message) {
			// Si remitente esta bloqueado ignoramos su mensaje
			if (getusersBanned().getOrDefault(getNickname(), false)) {
				return;
			}

			String[] words = message.getMessage().split(" ");

			if (words.length == 2) { // Con long 2 podría ser un comando
				String first = words[0];
				String second = words[1];
				switch (first) {
				case "ban":
					bannUser(second, true);
					break;
				case "unban":
					bannUser(second, false);
					break;
				default:
					break;
				}
			} else {
				broadcast(message);
			}
		}

		private void connect() throws IOException, ClassNotFoundException {

			ChatMessage loginMessage = (ChatMessage) getInput().readObject();
			if (loginMessage.getType() == MessageType.MESSAGE) {
				setNickname(loginMessage.getMessage());
				getUsers().put(getNickname(), this);
				setClientId(getNextId());
				getUserIds().put(id, getNickname());
				sendWelcome();
				System.out.println("Clientes conectados actualmente: " + getUsers().size());
			} else {
				System.err.println("Error: Mensaje de tipo inesperado. Abortando la conexión con el cliente.");
				closeClient();
				return;
			}
		}

		private void sendWelcome() {
			try {
				String welcomeMessage = String.format("Bienvenido al chat %s. Te hemos asignado el ID %d",
						getNickname(), getClientId());
				getOutput().writeObject(new ChatMessage(getClientId(), MessageType.MESSAGE, welcomeMessage));
				System.out.println(getNickname() + " se ha conectado al chat.");
			} catch (IOException e) {
				System.err.println("ERROR: Could not send initial connection message to client " + getNickname());
			}
		}

		private void closeClient() {
			try {
				setThreadAlive(false);
				if (getInput() != null)
					getInput().close();
				if (getOutput() != null)
					getOutput().close();
				if (getThreadSocket() != null)
					getThreadSocket().close();
			} catch (IOException e) {
				System.err.println("Error: No se ha podido desconectar correctamente al usuario " + getNickname());
			}
		}

		private void bannUser(String nickname, boolean banOption) {
			getusersBanned().put(nickname, banOption);
			if (banOption == true) {
				System.out.println(nickname + " ha sido bloqueado por " + getNickname() + " a las " + getTime());
				broadcast(new ChatMessage(getClientId(), MessageType.MESSAGE,
						nickname + " ha sido bloqueado por " + getNickname() + " a las " + getTime()));
			} else {
				System.out.println(nickname + " ha sido desbloqueado por " + getNickname() + " a las " + getTime());
				broadcast(new ChatMessage(getClientId(), MessageType.MESSAGE,
						nickname + " ha sido bloqueado por " + getNickname() + " a las " + getTime()));
			}
		}

	}

}
package es.ubu.lsi.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.ChatMessage.MessageType;
/**
 * 
 * @author Eduardo Manuel Cabeza Lopez
 *
 */
public class ChatClientImpl implements ChatClient {

	private Socket clientSocket;
	private final int PORT = 1500;
	ObjectOutputStream output;
	ObjectInputStream input;
	private String serverHost;
	private String nickname;
	private static int id;
	private boolean isAlive;
	private Scanner scanner;

	public ChatClientImpl(String server, String username) {
		this.serverHost = server;
		this.nickname = username;
		this.isAlive = true;
		try {

			this.clientSocket = new Socket(this.serverHost, this.PORT);
			this.output = new ObjectOutputStream(clientSocket.getOutputStream());
		} catch (IOException e) {
			System.err.println("Error: No se puede iniciar el cliente.");
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	private String getNickname() {
		return this.nickname;
	}

	private Socket getClientSocket() {
		return this.clientSocket;
	}

	private void setScanner(Scanner scanner) {
		this.scanner = scanner;
	}

	private Scanner getScanner() {
		return this.scanner;
	}

	private ObjectOutputStream getOutput() {
		return this.output;
	}

	private void setInput(ObjectInputStream input) {
		this.input = input;
	}

	private ObjectInputStream getInput() {
		return this.input;
	}

	private static int getId() {
		return id;
	}

	private static void setId(int newId) {
		id = newId;
	}

	private void setIsAlive(boolean b) {
		this.isAlive = b;
	}

	private boolean getIsAlive() {
		return this.isAlive;
	}

	@Override
	public boolean start() {
		try {
			connect();
			setScanner(new Scanner(System.in));
			String msgText;
			while (getIsAlive()) {
				msgText = getScanner().nextLine();
				if (msgText.equalsIgnoreCase("LOGOUT")) {
					ChatMessage msg = new ChatMessage(getId(), MessageType.LOGOUT, "");
					sendMessage(msg);

					break;
				} else if (msgText.equalsIgnoreCase("SHUTDOWN")) {
					if (getNickname().equalsIgnoreCase("ADMIN")) {
						System.out.println("Apagando el servidor.");
						ChatMessage msg = new ChatMessage(getId(), MessageType.SHUTDOWN, "");
						sendMessage(msg);
						break;
					} else {
						System.out.println("Solo el admin puede enviar el comando shutdown.");
					}
				} else {
					ChatMessage msg = new ChatMessage(getId(), MessageType.MESSAGE, msgText);
					sendMessage(msg);
				}
			}
		} finally {
			disconnect();
		}
		return true;
	}

	@Override
	public void sendMessage(ChatMessage msg) {
		try {
			getOutput().writeObject(msg);
		} catch (IOException e) {
			System.err.println("Error: No se puede enviar el mensaje al servidor.");
			System.err.println(e.getMessage());
			disconnect();
		}
	}

	@Override
	public void disconnect() {
		try {
			if (getScanner() != null)
				getScanner().close();
			if (getOutput() != null)
				getOutput().close();
			if (getClientSocket() != null)
				getClientSocket().close();
			setIsAlive(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void connect() {
		ChatMessage msg = new ChatMessage(0, MessageType.MESSAGE, getNickname());

		try {
			setInput(new ObjectInputStream(getClientSocket().getInputStream()));
			sendMessage(msg);
			msg = (ChatMessage) getInput().readObject();

			System.out.println(msg.getMessage());
			if (msg.getType() == MessageType.LOGOUT) {
				System.out.println("Desconectando el cliente.");
				disconnect();
				System.exit(0);
			}
			setId(msg.getId());
			new Thread(new ChatClientListener()).start();
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("Error: No se puede obtener el mensaje del servidor.");
			disconnect();
			System.exit(1);
		}
	}

	public static void main(String[] args) {

		String server = "";
		String nick = "";

		if (args.length == 1) {
			server = "localhost";
			nick = args[0];
		} else if (args.length == 2) {
			server = args[0];
			nick = args[1];
		} else {
			System.out.println("Se esperaban 1 o 2 argumentos: <nickname> o <serverhost> <nickname>");
			System.exit(1);
		}
		new ChatClientImpl(server, nick).start();
	}

	class ChatClientListener implements Runnable {

		// ObjectInputStream input;
		/*
		 * public ChatClientListener(ObjectInputStream input) { this.input = input; }
		 * 
		 * private ObjectInputStream getInput() { return this.input; }
		 */
		@Override
		public void run() {
			try {
				while (getIsAlive()) {
					ChatMessage msg = (ChatMessage) getInput().readObject();
					System.out.println(msg.getMessage());
				}
			} catch (IOException e) {
				System.err.println("Error: Se ha perdido la conexión con el servidor.");
				setIsAlive(false);
			} catch (ClassNotFoundException e) {
				System.err.println("Error: No se puede recibir la respuesta del servidor.");
			} finally {
				try {
					getInput().close();
					System.out.println("Cerrando el cliente");
					disconnect();
				} catch (IOException e) {
					System.err.println("Error: No se peude cerrar la conexión correctamente.");
					e.printStackTrace();
				}
			}
		}
	}
}
package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

/**
 * Interfaz que define la estructura del cliente de chat.
 * 
 * @author Eduardo Manuel Cabeza Lopez
 * @version 1.0.0
 *
 */
public interface ChatClient {

	/**
	 * Inicia el cliente conectandolo al servidor.
	 * Devuelve {@code true} si el cliente se inicia correctamente .
	 * 
	 * 
	 * @return {@code true} si se inicia correctamente
	 */
	public boolean start();
	
	/**
	 * Envía un mensaje al servidor para su difusión.
	 * 
	 * @param msg Mensaje que se envía al servidor
	 */
	public void sendMessage(ChatMessage msg);
	
	/**
	 * Desconecta el cliente del servidor y lo cierra.
	 */
	public void disconnect();
	
}

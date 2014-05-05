package de.cronosx.websocket;

/**
 * An OpenHandler that may be passed to a Websocket.
 * It will be called if the Websocket is opened and ready to send and receive
 * messages.
 * 
 * @author prior (Frederick Gnodtke)
 */
public interface OpenHandler {
	/**
	 * This method will be called if the respective Websocket is ready to send
	 * and receive messages.
	 */
	public void onOpen();
}

package de.cronosx.websocket;

/**
 * A MessageHandler that may be attached to Websocket and will then be called
 * when a new message was read.
 * 
 * @author prior (Frederick Gnodtke)
 */
public interface MessageHandler {
	/**
	 * This method will be called when a new message arrived at the socket.
	 * 
	 * @param message 
	 * The message that was read.
	 */
	public void onMessage(String message);
}

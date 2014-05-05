package de.cronosx.websocket;

/**
 * Add this CloseHandler to any instance of Websocket and it will be called if the
 * socket was closed.
 * 
 * @author prior (Frederick Gnodtke)
 */
public interface CloseHandler {
	/**
	 * This method will be called when the socket it was added to is closed.
	 * Please note that Websockets can be closed remotely and this method will
	 * also be called then.
	 */
	public void onClose();
}

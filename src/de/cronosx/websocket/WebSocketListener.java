package de.cronosx.websocket;

/**
 * @author prior
 * Handles a websocket
 * All events from the websocket will be passed here
 *
 */
public interface WebSocketListener
{
	/**
	 * Called from the websocket when a message was received
	 * <p>
	 * @param s string received
	 * @param origin websocket the string is origin from
	 */
	public void onMessage(String s, Websocket origin);
	
	/**
	 * Called when the websocket was obened, but before the handshake was done
	 * The websocket is not yet usable
	 * <p>
	 * @param origin websocket that was opened
	 */
	public void onOpen(Websocket origin);
	
	/**
	 * Called before the handshake takes place, after receiving a handshakerequest from the client
	 * The websocket is not yet usable
	 * <p>
	 * @param origin websocket that requested the handshake
	 */
	public void onHandshake(Websocket origin);
	
	/**
	 * Called after the initial handshake was successfully done
	 * The websocket may now be used in both directions
	 * <p>
	 * @param origin websocket that successfully did the handshake
	 */
	public void onHandshakeSuccessfull(Websocket origin);
	
	/**
	 * Called after the websocket was closed down
	 * Neither sending nor receiving a message will now be possible
	 * <p>
	 * @param origin websocket that was closed
	 */
	public void onClose(Websocket origin);
}
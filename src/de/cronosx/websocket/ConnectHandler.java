package de.cronosx.websocket;

/**
 * ConnectHandler that may be added to a WebsocketServer and will be called if
 * a new connection was made.
 * 
 * @author prior (Frederick Gnodtke)
 */
public interface ConnectHandler {
	/**
	 * This method will be called if a new connection was made by the WebsocketServer.
	 * 
	 * @param serverWebsocket 
	 * The newly opened socket. please note that it may not be ready to read- or write to/from it.
	 * You may instead want to add an OpenHandler to it.
	 */
	public void onConnect(ServerWebsocket serverWebsocket);
}

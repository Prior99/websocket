package de.cronosx.websocket;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * This is a websocket-server. It will listen on a specific port for new connections
 * and create new instances of ServerWebsocket and pass them to ConnectHandlers
 * that you may previously register.
 * 
 * @author prior (Frederick Gnodtke)
 */
public class WebsocketServer extends Thread {
	private ServerSocket server;
	private final int port;
	private final List<ConnectHandler> connectHandlers;
	private final List<ServerWebsocket> sockets;
	
	/**
	 * Will create a new instance of a WebsocketServer. The server will listen
	 * on the port you supplied as argument after you called listen() on it.
	 * You may add ConnectHandlers to it that will be called if a connection is
	 * made.
	 * 
	 * @param port 
	 * The port the server will be accepting connections on.
	 */
	public WebsocketServer(int port) {
		sockets = Collections.synchronizedList(new LinkedList<ServerWebsocket>());
		connectHandlers = Collections.synchronizedList(new LinkedList<ConnectHandler>());
		this.port = port;
	}
	
	/**
	 * Will add a new ConnectHandler to the list of registered ConnectHandlers.
	 * It will be called each time a new socket was opened supplying a reference
	 * to the respective socket.
	 * 
	 * @param handler 
	 */
	public void addConnectHandler(ConnectHandler handler) {
		synchronized(connectHandlers) {
			connectHandlers.add(handler);
		}
	}
	
	/**
	 * Will remove the supplied ConnectHandler from the list of registered 
	 * ConnectHandlers so it will no further be called if a new connection
	 * is made.
	 * 
	 * @param handler 
	 * The ConnectHandler to remove.
	 */
	public void removeConnectHandler(ConnectHandler handler) {
		synchronized(connectHandlers) {
			connectHandlers.remove(handler);
		}
	}
	
	/**
	 * Will make the server listen on the previously supplied port.
	 * 
	 * @throws IOException 
	 * If an IOExceptions occurs on opening the underlying ServerSocket.
	 */
	public void listen() throws IOException {
		this.server = new ServerSocket(port);
		this.start();
	}
	
	@Override
	public void run() {
		while(!isInterrupted()) {
			try {
				Socket socket = server.accept();
				final ServerWebsocket sws = new ServerWebsocket(socket);
				sockets.add(sws);
				synchronized(connectHandlers) {
					for(ConnectHandler handler : connectHandlers) {
						handler.onConnect(sws);
					}
				}
				sws.addCloseHandler(new CloseHandler() {
					@Override
					public void onClose()
					{
						synchronized(sockets) {
							sockets.remove(sws);
						}
					}
				});
			} 
			catch (IOException e) {
				break;
			}
		}
	}
	
	/**
	 * Will close this server. No more ongoing connections will be accepted.
	 * All currently opened connections will be closed.
	 */
	public void close() {
		try {
			synchronized(sockets) {
				for(ServerWebsocket sws : sockets) {
					sws.close();
				}
			}
			interrupt();
			server.close();
		} 
		catch(IOException e) {
			
		}
	}
}

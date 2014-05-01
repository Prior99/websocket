package de.cronosx.websocket;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class WebsocketServer extends Thread {
	private ServerSocket server;
	private final int port;
	private final List<ConnectHandler> connectHandlers;
	
	public WebsocketServer(int port) {
		connectHandlers = new LinkedList<ConnectHandler>();
		this.port = port;
	}
	
	public void addConnectHandler(ConnectHandler handler) {
		connectHandlers.add(handler);
	}
	
	public void removeConnectHandler(ConnectHandler handler) {
		connectHandlers.remove(handler);
	}
	
	public void listen() throws IOException {
		this.server = new ServerSocket(port);
		this.start();
	}
	
	@Override
	public void run() {
		while(!isInterrupted()) {
			try {
				Socket socket = server.accept();
				ServerWebsocket sws = new ServerWebsocket(socket);
				for(ConnectHandler handler : connectHandlers) {
					handler.onConnect(sws);
				}
			} 
			catch (IOException e) {
				break;
			}
		}
	}
	
	public void close() {
		try {
			interrupt();
			server.close();
		} 
		catch(IOException e) {
			
		}
	}
}

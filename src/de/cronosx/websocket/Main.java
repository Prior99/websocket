package de.cronosx.websocket;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class Main 
{
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		try {
			WebsocketServer server = new WebsocketServer(8000);
			server.addConnectHandler(new ConnectHandler() {

				@Override
				public void onConnect(ServerWebsocket serverWebsocket) {
					serverWebsocket.send("Ich grüße dich!");
					serverWebsocket.addMessageHandler(new MessageHandler() {
						@Override
						public void onMessage(String message) {
							System.out.println("Server Received: " + message);
							serverWebsocket.close();
							server.close();
						}
					});
				}
			});
			server.listen();
			final ClientWebsocket ws = new ClientWebsocket(new Socket("localhost", 8000));
			ws.addOpenHandler(new OpenHandler() {
				@Override
				public void onOpen() {
					System.out.println("Client: Opened!");
					ws.send("Hallo!!");
				}
			});
			ws.addCloseHandler(new CloseHandler() {
				@Override
				public void onClose() {
					System.out.println("Client: Closed!");
				}
			});
			ws.addMessageHandler(new MessageHandler() {
				@Override
				public void onMessage(String message) {
					System.out.println("Client Received: " + message);
					//ws.close();
				}
			});
			ws.listen();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

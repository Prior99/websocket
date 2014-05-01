package de.cronosx.websocket;

import java.io.IOException;
import java.net.Socket;

public class Main 
{
	
	public static void main(String[] args) {
		try {
			WebsocketServer server = new WebsocketServer(7000);
			server.addConnectHandler(new ConnectHandler() {
				@Override
				public void onConnect(final ServerWebsocket serverWebsocket) {
					serverWebsocket.addOpenHandler(new OpenHandler() {
						@Override
						public void onOpen() {
							System.out.println("Server: Opened new Websocket!");
						}
					});
					serverWebsocket.addCloseHandler(new CloseHandler() {
						@Override
						public void onClose() {
							System.out.println("Server: Closed Websocket!");
						}
					});
					serverWebsocket.addMessageHandler(new MessageHandler() {
						@Override
						public void onMessage(String message) {
							System.out.println("Server received: " + message);
							serverWebsocket.send("Echo: " + message);
						}
					});
				}
			});
			server.listen();
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < 100; i++) {
				sb.append("Hello, world! ");
			}
			final ClientWebsocket ws = new ClientWebsocket(new Socket("localhost", 7000));
			ws.addOpenHandler(new OpenHandler() {
				@Override
				public void onOpen() {
					System.out.println("Client: Opened!");
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
					ws.close();
				}
			});
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

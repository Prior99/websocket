package de.cronosx.websocket;

import java.io.IOException;
import java.net.Socket;

public class Main 
{
	
	public static void main(String[] args) {
		try {
			WebsocketClient wsc = new WebsocketClient(new Socket("cronosx.de", 5560));
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

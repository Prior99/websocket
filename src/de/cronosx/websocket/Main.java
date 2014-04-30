package de.cronosx.websocket;

import java.io.IOException;
import java.net.Socket;

public class Main 
{
	
	public static void main(String[] args) {
		try {
			WebsocketClient wsc = new WebsocketClient(new Socket("cronosx.de", 5560));
			wsc.send("{\"name\":\"Frederick\",\"password\":\"123\",\"_requestID\":\"LoginUser\",\"_type\":\"Request\",\"_responseID\":1}");
			wsc.send("{\"name\":\"Test\",\"password\":\"123\",\"_requestID\":\"Login\",\"_type\":\"Request\",\"_responseID\":0}");
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}

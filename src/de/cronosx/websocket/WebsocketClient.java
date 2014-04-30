/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cronosx.websocket;

import java.io.*;
import java.net.*;

/**
 *
 * @author prior
 */
public class WebsocketClient extends Websocket {
		
	public WebsocketClient(Socket socket) throws IOException {
		super(socket);
		sendHeader();
		readHeader();
		super.listen();
	}
	
	private void sendHeader() throws IOException {
		PrintWriter pw = new PrintWriter(socket.getOutputStream());
		pw.println("GET / HTTP/1.1");
		pw.println("Connection: Upgrade");
		pw.println("Host: " + socket.getInetAddress().getHostName() + ":" + socket.getPort());
		pw.println("Upgrade: websocket");
		pw.println("Sec-WebSocket-Key: eznftElk5opd/ouHA4lZLw==");
		pw.println("Sec-WebSocket-Version: 13");
		pw.println();
		pw.flush();
	}
	
	private void readHeader() throws IOException {
		BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String line;
		while((line = rd.readLine()) != null && !line.equals("")) {
			System.out.println(line);
		}
		System.out.println("Header finished!");
	}

	@Override
	protected boolean maskOutput() {
		return true;
	}
		
}

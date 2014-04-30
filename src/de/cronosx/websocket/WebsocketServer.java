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
public class WebsocketServer extends Websocket
{
	private final HTTP response;
	private final HTTP request;	

	public WebsocketServer(Socket socket) throws IOException
	{
		super(socket);
		request = new HTTP();
		response = new HTTP();
		readHeader();
		sendHeader();
	}
	
	private void readHeader() throws IOException {
		request.readHeader(socket.getInputStream());
	}
	
	private void sendHeader() {
		String host = request.get("Host");
		String key = request.get("Sec-WebSocket-Key");
		
		response.setRequest("HTTP/1.1 101 Switching Protocols");
		response.add("Connection", "Upgrade");
		response.add("Upgrade", "websocket");
		response.add("Sec-Websocket-Host", request.get("Host"));
		response.add("Sec-Websocket-Accept", generateHandshake(key));
	}

	@Override
	protected boolean maskOutput()
	{
		return false;
	}
	
}

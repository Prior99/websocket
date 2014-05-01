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
public class ClientWebsocket extends Websocket {
	private final HTTP response;
	private final HTTP request;	
	
	public ClientWebsocket(Socket socket) throws IOException {
		super(socket);
		request = new HTTP();
		response = new HTTP();
		sendHeader();
		readHeader();
		super.listen();
	}
	
	public HTTP getRequest() {
		return request;
	}
	
	public HTTP getResponse() {
		return response;
	}
	
	private void sendHeader() throws IOException {
		request.setRequest("GET / HTTP/1.1");
		request.add("Connection", "Upgrade");
		request.add("Host", socket.getInetAddress().getHostName() + ":" + socket.getPort());
		request.add("Upgrade", "websocket");
		request.add("Sec-WebSocket-Key", "eznftElk5opd/ouHA4lZLw==");
		request.add("Sec-WebSocket-Version", "13");
		request.writeHeader(socket.getOutputStream());
	}
	
	private void readHeader() throws IOException {
		response.readHeader(socket.getInputStream());
	}

	@Override
	protected boolean maskOutput() {
		return true;
	}
		
}

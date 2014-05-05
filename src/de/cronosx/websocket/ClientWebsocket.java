package de.cronosx.websocket;

import java.io.*;
import java.net.*;

/**
 * An implementation of Websocket that handles all corresponding necessary
 * HTTP-Headers for the clientside.
 * 
 * @author prior (Frederick Gnodtke)
 */
public class ClientWebsocket extends Websocket {
	private final HTTP response;
	private final HTTP request;	
	
	/**
	 * Will wrap a new instance of ClientWebsocket around this already opened Socket.
	 * Please call the listen()-method in order to open the socket, send request,
	 * read response and make receiving and sending of messages possible.
	 * 
	 * @param socket
	 * The socket to wrap around.
	 * @throws IOException 
	 * If the streams cannot be opened.
	 */
	public ClientWebsocket(Socket socket) throws IOException {
		super(socket);
		request = new HTTP();
		response = new HTTP();
	}
	
	/**
	 * Will send the HTTP-Request, read the response and open the Websocket.
	 * After calling this method the Websocket will start listening.
	 * Please use an OpenHandler in order to fetch the moment the socket is
	 * able to read and send messages.
	 * 
	 * @throws IOException 
	 * If the socket cannot be opened or an error occurs on reading or sending
	 * the HTTP-headers.
	 */
	public void listen() throws IOException {
		sendHeader();
		readHeader();
		start();
	}
	
	/**
	 * Will return the HTTP-Request that was sent to the server.
	 * Please note that it will only be safe to read this header after the 
	 * OpenHandler was called. It will be empty before that.
	 * 
	 * @return 
	 * The sent HTTP-Request.
	 */
	public HTTP getRequest() {
		return request;
	}
	
	/**
	 * Will return the HTTP-Response that was received from the server after 
	 * the request was sent.
	 * Please note that it will only be safe to read this header after the 
	 * OpenHandler was called. It will be empty before that.
	 * 
	 * @return 
	 * The received HTTP-Response.
	 */
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

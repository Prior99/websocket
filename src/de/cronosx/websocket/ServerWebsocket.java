package de.cronosx.websocket;

import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import sun.misc.BASE64Encoder;

public class ServerWebsocket extends Websocket
{
	private final HTTP response;
	private final HTTP request;	
	private final static String globalUniqueIdentifier = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
	
	public ServerWebsocket(Socket socket) throws IOException
	{
		super(socket);
		request = new HTTP();
		response = new HTTP();
		readHeader();
		sendHeader();
		start();
	}
	
	private void readHeader() throws IOException {
		request.readHeader(socket.getInputStream());
	}
	
	private void sendHeader() throws IOException {
		String host = request.get("Host");
		String key = request.get("Sec-WebSocket-Key");
		
		System.out.println("WEBSCOKET-KEY:"+key);
		System.out.println("HOST:"+host);
		
		response.setRequest("HTTP/1.1 101 Switching Protocols");
		response.add("Connection", "Upgrade");
		response.add("Upgrade", "websocket");
		response.add("Sec-Websocket-Host", host);
		response.add("Sec-Websocket-Accept", generateHandshake(key));
		response.writeHeader(socket.getOutputStream());
	}
	
	private static String generateHandshake(String key) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			return javax.xml.bind.DatatypeConverter.printBase64Binary(md.digest((key + globalUniqueIdentifier).getBytes()));
		} 
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	private static String byteToHexString(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < b.length; i++) {
			sb.append(Integer.toString(b[i] & 0xFF + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

	@Override
	protected boolean maskOutput()
	{
		return false;
	}
	
}

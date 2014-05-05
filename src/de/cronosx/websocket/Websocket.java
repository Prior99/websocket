package de.cronosx.websocket;

import java.io.*;
import java.net.*;
import java.util.*;

/*
 *       | FIN|  R1|  R2|  R3| OP1| OP2| OP3| OP4 
 *       | 128|  64|  32|  16|   8|   4|   2|   1
 * ------+----+----+----+----+----+----+----+----
 * String|   1|   0|   0|   0|   0|   0|   0|   1
 * Binary|   1|   0|   0|   0|   0|   0|   1|   0
 *                 ... Reserved ...
 * Close |   1|   0|   0|   0|   1|   0|   0|   0
 * Ping  |   1|   0|   0|   0|   1|   0|   0|   1
 * Pong  |   1|   0|   0|   0|   1|   0|   1|   0
 *                ... Reserved ...
 */

/**
 * This is a wrapped socket that parses the pure byte-input with the Websocketprotocol and returns the decoded code
 * and the other way around encrypts messages and passes them to the client
 * 
 * @author prior (Frederick Gnodtke)
 */
public abstract class Websocket extends Thread
{
	
	protected final Socket socket;
	private final InputStream inputStream;
	private final OutputStream outputStream;
	private final List<CloseHandler> closeHandlers;
	private final List<OpenHandler> openHandlers;
	private final List<MessageHandler> messageHandlers;
	
	/**
	 * Wraps a new Websocket around the supplied socket.
	 * 
	 * @param socket
	 * The socket to wrap the Websocket around.
	 * @throws IOException 
	 * If the streams cannot be opened.
	 */
	public Websocket(final Socket socket) throws IOException {
		closeHandlers = new LinkedList<CloseHandler>();
		openHandlers = new LinkedList<OpenHandler>();
		messageHandlers = new LinkedList<MessageHandler>();
		this.socket = socket;
		this.inputStream = socket.getInputStream();
		this.outputStream = socket.getOutputStream();
	}
	
	/**
	 * Adds a new CloseHandler to the list of CloseHandlers that will be called
	 * when the socket is closed.
	 * Keep in mind, that Websockets may be closed from the remote side.
	 * 
	 * @param handler 
	 * The Handler to add
	 */
	public void addCloseHandler(CloseHandler handler) {
		closeHandlers.add(handler);
	}
	
	/**
	 * Removes a Handler from the list of known CloseHandlers so it will no
	 * further be called if the socket is closed.
	 * 
	 * @param handler 
	 * The Handler to remove
	 */
	public void removeCloseHandler(CloseHandler handler) {
		closeHandlers.remove(handler);
	}
	
	/**
	 * Adds a new OpenHandler to the list of OpenHandlers that will be called
	 * when the socket is opened and can now send an receive messages.
	 * 
	 * @param handler 
	 * The Handler to add
	 */
	public void addOpenHandler(OpenHandler handler) {
		openHandlers.add(handler);
	}
	
	/**
	 * Removes a Handler from the list of known OpenHandlers so it will no
	 * further be called if the socket is opened.
	 * 
	 * @param handler 
	 * The Handler to remove
	 */
	public void removeOpenHandler(OpenHandler handler) {
		openHandlers.remove(handler);
	}
	
	/**
	 * Adds a new MessageHandler to the list of MessageHandlers that will be called
	 * when a new message is read from the socket.
	 * 
	 * @param handler 
	 * The Handler to add
	 */
	public void addMessageHandler(MessageHandler handler) {
		messageHandlers.add(handler);
	}
	
	/**
	 * Removes a Handler from the list of known MessageHandlers so it will no
	 * further be called if the socket received a new message.
	 * 
	 * @param handler 
	 * The Handler to remove
	 */
	public void removeMessageHandler(MessageHandler handler) {
		messageHandlers.remove(handler);
	}
	
	@Override
	public void run() {
		for(OpenHandler handler : openHandlers) {
			handler.onOpen();
		}
		while(!isInterrupted()) {
			try {
				String message = readMessage();
				if(message == null) {
					return;
				}
				else {
					for(MessageHandler handler : messageHandlers) {
						handler.onMessage(message);
					}
				}
				
			} 
			catch (IOException e) {
				shutdown();
				return;
			}
		}
	}
	
	private String readMessage() throws IOException {
		String message;
		/*
		 * Parsing the opcode
		 */
		int firstbyte = inputStream.read();
		if(firstbyte == -1) { //EOS reached. Socket will be closed, null will be returned
			shutdown();
			return null;
		}
		if((firstbyte & 128) == 0) { //If this not the final Fragment, we have a Multi-Framed Message which is currently not supported
			throw new UnsupportedOperationException("A multiframed message was received. Multiframed message are currently not implemented.");
		}
		int opcode = firstbyte & 15; //The last 4 Bits of the firstbyte are the opcode.
		if(opcode == 8) {// 8 is the opcode for a closing Frame. If this opcode is received, the socket will be closed.
			shutdown();
			return null;
		}
		else if(opcode == 2) {//This indicates a binary frame. Binary frames will not be supported in the current version of this implementation
			throw new UnsupportedOperationException("A binary message was received. Binary messages are currently not supported.");
		}
		else if(opcode == 1) {//This indicates a textual message.
			/*
			 * Parsing the length
			 */
			int secondbyte = inputStream.read();
			if(secondbyte == -1) { //EOS reached. Socket will be closed, null will be returned
				shutdown();
				return null;
			}
			long length;
			int lenIndicator = secondbyte & 127; //This only leaves the last 7 bits (as the first bit indicates the masking.
			if(lenIndicator <= 125) length = lenIndicator; //If the indicator is less than 126, that is the length
			else if(lenIndicator == 126) {//If the indicator is 126, the next 2 bytes indicate the length
				byte[] b = new byte[2];
				if(inputStream.read(b) == -1) { //EOS reached. Socket will be closed, null will be returned
					shutdown();
					return null;
				}
				length = byteToUInt(b);
			}
			else  {//So, the indicator is 127, which means that the next 8 bytes indicate the length
				byte[] b = new byte[8];
				if(inputStream.read(b) == -1) { //EOS reached. Socket will be closed, null will be returned
					shutdown();
					return null;
				}
				length = byteToUInt(b);
			}
			/*
			 * Parsing the mask
			 */
			byte[] mask = new byte[4];
			boolean masked = (secondbyte & 128) != 0;
			if(masked) { //If the message is masked, we will have to parse the mask
				if(inputStream.read(mask) == -1) { //EOS reached. Socket will be closed, null will be returned
					shutdown();
					return null;
				}
			}
			/*
			 * Reading the payload
			 */
			byte[] data = new byte[(int)length]; //Where the payload will be reead to.
			if(inputStream.read(data) == -1) { //EOS reached. Socket will be closed, null will be returned
				shutdown();
				return null;
			}
			if(masked) {
				for(int i = 0; i < length; i++) {
					data[i] ^= mask[i % 4];
				}
			}
			/*
			 * By now, the parsing has finished!
			 */
			return new String(data);
		}
		else { //A unknown opcode was sent. As for the RFC Specifivations of websocket this means that the socket has to be closed.
			shutdown();
			throw new UnsupportedOperationException("An unknown opcode was received. As for the specifications of RFC 6455 the socket has to be closed now.");
		}
	}
	
	/**
	 * All messages sent from the client to the server have to be masked!
	 * If this websocket is part of a client, this method has to return true
	 * @return Whether output messages will be masked.
	 */
	protected abstract boolean maskOutput();
	
	private static long byteToUInt(byte[] b) {
		long l = 0;
		for(int i = 0; i < b.length; i++) {
			//System.out.println(b[i]);
			l <<= 8;
			l |= b[i];
		}
		return l;
	}
	
	private static byte[] uintToByte(long l) {
		byte[] b = new byte[8];
		for(int i = b.length - 1; i >= 0; i--) {
			b[i] = (byte)(l & 0xFF);
			l >>= 8;
		}
		return b;
	}
	
	private void shutdown() {
		for(CloseHandler handler : closeHandlers) {
			handler.onClose();
		}
		try {
			interrupt();
			socket.close();
		}
		catch(IOException e) {
			
		}
	}
	
	/**
	 * Will softly and correctly close the socket.
	 * This message will send the corresponding closing-frame to the remote side
	 * so the Websocket is closed gently.
	 * Please use this method instead of closing the underlying socket directly.
	 */
	public void close() {
		int opcode = 128 | 8; // Indicates a closing frame
		int len = 0; //Indicates an empty message
		try {
			outputStream.write(opcode);
			outputStream.write(len);
			outputStream.flush();
		}
		catch(IOException e) {
			
		}
		shutdown();
	}
	
	/**
	 * Will send a message to the remote side.
	 * Please note that this implementation of Websockets is
	 *   a) considering each message as a textual messages, binary data will also
	 *      be send as a textual-frame (such as all browsers do)
	 *   b) not able to send messages larger than 2GBs
	 *   c) not able to send multi-framed messages.
	 * 
	 * @param string 
	 * The message to send.
	 */
	public void send(String string)  {
		try {
			byte[] bytes = string.getBytes();
			int opcode = 128 | 1; //Opcode for 'Final message and string'
			outputStream.write(opcode);
			int len1 = 0;
			if(maskOutput()) len1 |= 128; //If we have to mask the output, we will set this bit to 1
			if(bytes.length <= 125) {
				len1 |= bytes.length;
				outputStream.write(len1);
			}
			else if(bytes.length <= 1<<16) {
				len1 |= 126;
				byte[] length = new byte[2];
				length[0] = (byte) (bytes.length >> 8 & 0xFF); // Byte 1
				length[1] = (byte) (bytes.length      & 0xFF); // Byte 0
				outputStream.write(len1);
				outputStream.write(length);
			}
			else if(bytes.length <= 1<<31) { //Note: Theoretically websocket supports messages as long as unsigned integer of length of 64bit supports
				//But who the fuck would send 16,3 Exabyte via a websocket?
				//Let's just assume, that 2 Gigabytes should be enough for everybody.
				//If you intend to send messages with a payload larger than 2 Gigabytes please consider using a stream,
				//Framing your message or consulting a psychiatrist
				len1 |= 127;
				byte[] length = new byte[8];
				//Please note, that length[0...3] are left empty (0) as we will consider no message greater than 2 Gigabytes (as java cannot handle that huge numbers) 
				length[4] = (byte) (bytes.length >> 24 & 0xFF); // Byte 3
				length[5] = (byte) (bytes.length >> 16 & 0xFF); // Byte 2
				length[6] = (byte) (bytes.length >>  8 & 0xFF); // Byte 1
				length[7] = (byte) (bytes.length       & 0xFF); // Byte 0
				outputStream.write(len1);
				outputStream.write(length);	
			}
			else {
				throw new UnsupportedOperationException("Message exceeded maximum size of 2^31 bytes.");
			}
			if(maskOutput()) {
				byte[] mask = new byte[4];
				for(int i = 0; i < 4; i++) {
					mask[i] = (byte)(Math.random() * 255 - 128);
				}
				outputStream.write(mask);
				for(int i = 0; i < bytes.length; i++) {
					bytes[i] ^= mask[i % 4];
				}
			}
			outputStream.write(bytes);
			outputStream.flush();
		}
		catch(IOException e) {
			shutdown();
		}
	}
}
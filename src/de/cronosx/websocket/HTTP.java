package de.cronosx.websocket;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Handles basic parsing and creating of HTTP-headers.
 * 
 * @author prior (Frederick Gnodtke)
 */
public class HTTP
{
	private static enum Mode {
		read,
		write, 
		undefined
	}
	private Map<String, String> map;
	private int lines;
	private String request;
	private Mode mode;
	/**
	 * Instances a new empty HTTP-Request.
	 * You at least have to set the Header before you can send this Request.
	 * If you want to use this instance in order to read a header, you may then
	 * no further manipulate it.
	 */
	public HTTP() {
		mode = Mode.undefined;
		map = new HashMap<String, String>();
		lines = 0;
	}
	
	/**
	 * This method will read a HTTP-Header from an input-stream, parse it and store 
	 * the supplied data in its internal map to be accessed via the get() method.
	 * Please note that you may not manipulate the header anymore after it was read.
	 * 
	 * @param stream
	 * Stream to read header from
	 * 
	 * @throws IOException 
	 * If an IOException occurs while reading from the stream.
	 */
	public void readHeader(InputStream stream) throws IOException {
		if(mode == Mode.write)
			throw new UnsupportedOperationException("You may not read a header to a header that is to be written.");
		mode = Mode.read;
		BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
		String line;
		
		while((line = rd.readLine()) != null && !line.equals("")) {
			if(lines++ == 0) {
				request = line;
			}
			else {
				int index = line.indexOf(":");
				String key = line.substring(0, index).trim();
				String value = URLDecoder.decode(line.substring(index + 1, line.length()), "UTF-8").trim();
				map.put(key.toLowerCase(), value);
			}
		}
	}
	/**
	 * Will write this header into an output-stream.
	 * You may want to set the request-line itself and additional parameters 
	 * before using setRequest() and set()
	 * An empty line will be written at the end and mark the end of the HTTP-header.
	 * 
	 * @param stream 
	 * The stream to write to.
	 */
	public void writeHeader(OutputStream stream) {
		if(mode == Mode.read)
			throw new UnsupportedOperationException("You may not write to a header that was previously read.");
		mode = Mode.write;
		PrintWriter pw = new PrintWriter(stream);
		pw.println(request + "\r");
		for(String key : map.keySet()) {
			String s = map.get(key);
			if(s != null) {
				try {
					pw.println(key + ":" + URLEncoder.encode(s, "UTF-8") +"\r");
				} catch (UnsupportedEncodingException e) {
					
				}
			}
		}
		pw.println("\r");
		pw.flush();
	}
	
	/**
	 * Will add a new key-value-pair to the internal map that may then be written
	 * if the writeHeader()-method is called.
	 * Please note that you may not manipulate a header that was previously read.
	 * 
	 * @param key
	 * Key that stands left of the ":"
	 * @param value 
	 * The value part that corresponds to the key and stands right of the ":"
	 */
	public void add(String key, String value) {
		if(mode == Mode.read)
			throw new UnsupportedOperationException("You may not write to a header that was previously read.");
		mode = Mode.write;
		map.put(key.toLowerCase(), value);
	}
	
	/**
	 * Sets the request-line (e.g. "GET / HTTP/1.1")
	 * 
	 * @param request 
	 * The request to set.
	 */
	public void setRequest(String request) {
		if(mode == Mode.read)
			throw new UnsupportedOperationException("You may not write to a header that was previously read.");
		mode = Mode.write;
		this.request = request;
	}
	
	/**
	 * Return the request that was previously read or set.
	 * 
	 * @return 
	 * The request-line.
	 */
	public String getRequest() {
		return request;
	}
	
	/**
	 * Returns the value that corresponds to the supplied key. Please note that
	 * all keys will be handled case-insensitive.
	 * 
	 * @param key
	 * Key to fetch value from
	 * @return 
	 * The value that corresponds to the key or null if no such eky is known.
	 */
	public String get(String key) {
		key = key.toLowerCase();
		if(!map.containsKey(key)) {
			return null;
		}
		else {
			return map.get(key);
		}
	}
}

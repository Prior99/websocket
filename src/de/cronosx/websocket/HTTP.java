package de.cronosx.websocket;

import java.io.*;
import java.net.*;
import java.util.*;

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
	public HTTP() {
		mode = Mode.undefined;
		map = new HashMap<String, String>();
		lines = 0;
	}
	
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
				String key = line.substring(0, index);
				String value = URLDecoder.decode(line.substring(index + 1, line.length()));
				map.put(key, value);
			}
		}
	}
	
	public int getLines() {
		return lines;
	}
	
	public void writeHeader(OutputStream stream) {
		if(mode == Mode.read)
			throw new UnsupportedOperationException("You may not write to a header that was previously read.");
		mode = Mode.write;
		PrintWriter pw = new PrintWriter(stream);
		for(String key : map.keySet()) {
			pw.println(key + ":" + URLEncoder.encode(map.get(key)));
		}
		pw.println();
		pw.flush();
	}
	
	public void add(String key, String value) {
		if(mode == Mode.read)
			throw new UnsupportedOperationException("You may not write to a header that was previously read.");
		mode = Mode.write;
		map.put(key.toLowerCase(), value);
	}
	
	public void setRequest(String request) {
		if(mode == Mode.read)
			throw new UnsupportedOperationException("You may not write to a header that was previously read.");
		mode = Mode.write;
		this.request = request;
	}
	
	public String getRequest() {
		return request;
	}
	
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

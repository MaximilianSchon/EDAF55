package se.lth.cs.edaf55.proxycamera;

import java.net.*;
import java.io.*;

/**
 * 
 * Camera Proxy API for the AxisM3006V model.
 * 
 * @author Emma Nilsson-Nyman (emma.nyman@cs.lth.se)
 * @author Roger Henriksson (roger@cs.lth.se)
 * @author Niklas Jonsson (dat11njo@student.lu.se)
 * @author Mathias Haage
 */
public class AxisM3006V {
	public static final int TIME_ARRAY_SIZE = 8;
	public static final int IMAGE_BUFFER_SIZE = 128 * 1024;
	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 480;
	private String host;
	private int port;
	private Socket socket;
	private InputStream inp;
	private OutputStream outp;
	private boolean motionDetected;
	private byte[] latestTime;

	/**
	 * Constructor
	 */
	public AxisM3006V() {
	}

	/**
	 * Initialize the camera object.
	 */
	public void init() {
		latestTime = new byte[TIME_ARRAY_SIZE];
	}
	
	public void setProxy(String host, int port) {
		this.host = host;
		this.port = port;
	}

	/**
	 * Connect to the camera.
	 * 
	 * @return true if connected otherwise false.
	 */
	public boolean connect() {
		socket = null;
		try {
			socket = new Socket(host, port);
			inp = socket.getInputStream();
			outp = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return socket != null;
	}

	/**
	 * Read an image from the camera.
	 * 
	 * @param target Byte array to put data in.
	 * @param offset Offset from the start of the byte array.
	 * 
	 * @return The number of read bytes.
	 */
	public int getJPEG(byte[] target, int offset) {
		int len = 0;
		int read;

		try {
			outp.write(42);
			outp.flush();
			// Length of time array is not included in len
			len = inp.read();
			len = (len << 8) + inp.read();
			len = (len << 8) + inp.read();
			len = (len << 8) + inp.read();
			read = 0;
			while (TIME_ARRAY_SIZE > read) { // Read time
				read += inp.read(latestTime, read, TIME_ARRAY_SIZE - read);
			}
			read = 0;
			while (len > read) {
				read += inp.read(target, offset + read, len - read);
			}
			int value_pos = 68 + offset;
			motionDetected = target[value_pos] == '1';
		} catch (IOException e) {
			e.printStackTrace();
		}
		return len;
	}

	/**
	 * Put the capture time of the latest image in the specified target byte array, starting at
	 * offset. The resolution is milliseconds.
	 * 
	 * @param target the array to be written into
	 * @param offset the starting position
	 */
	public void getTime(byte[] target, int offset) {
		int minLength = TIME_ARRAY_SIZE + offset;
		if (target.length < minLength) {
			throw new IllegalArgumentException("Length of target is too short, is " + target.length +" should be atleast" + minLength);
		}
		System.arraycopy(latestTime, 0, target, offset, TIME_ARRAY_SIZE);
	}

	/**
	 * Return whether or not motion was detected in the latest image. This is
	 * taken from the cameras built in motion detection and is extracted from
	 * the JPEG header whenever a new JPEG is fetched from the camera.
	 */
	public boolean motionDetected() {
		return motionDetected;
	}
	
	/**
	 * Close the camera connection.
	 */
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Destroy the camera object.
	 */
	public void destroy() {
	}
}

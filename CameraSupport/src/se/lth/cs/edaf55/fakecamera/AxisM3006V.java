package se.lth.cs.edaf55.fakecamera;

import java.io.*;
import java.util.Vector;

/**
 * A camera substitute. Plays back a recording from the media jar file. Is
 * singleton.
 *
 */
public class AxisM3006V {
	public static final int TIME_ARRAY_SIZE = 8;
	public static final int IMAGE_BUFFER_SIZE = 128 * 1024;
	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 480;
	private static final int MAX = 247; // Number of images
	private static float rate = 12; // Movie rate, Hz
	private Vector<byte[]> fakes = new Vector<byte[]>();
	//private static boolean instantiated = false;
	private boolean motionDetected;
	private byte[] latestTime;

	public AxisM3006V() {
	}

	public void init() {
		//if (instantiated)
		//	throw new Error("M3006 already instantiated, must be singleton");
		readJpegs();
		//instantiated = true;
		motionDetected = false;
		latestTime = new byte[AxisM3006V.TIME_ARRAY_SIZE];
	}

	public void setProxy(String host, int port) {
	}

	/**
	 * Connects to the camera.
	 * 
	 * @return true if connected otherwise false.
	 */
	public boolean connect() {
		return true;
	}

	/**
	 * Reads an image from the camera and puts it in the array target starting
	 * at index offset The size of target needs to be at least
	 * offset+IMAGE_BUFFER_SIZE
	 * 
	 * Motion is detected if the image index is between 86 and 240, according to
	 * the realtime programming course webpage.
	 * 
	 * @param target
	 *            Byte array to put data in.
	 * @param offset
	 *            Offset from the start of the byte array.
	 * 
	 * @return The length of the image captured, 0 if no picture was captured
	 */
	public int getJPEG(byte[] target, int offset) {
		if (target.length < IMAGE_BUFFER_SIZE){
			throw new Error("Length of parameter one is too small");
		}
		int i = imageIndex();
		byte[] image = fakes.get(i);
		motionDetected = (i > 86 && i < 240);
		System.arraycopy(image, 0, target, offset, image.length);
		storeTime();
		return image.length;
	}

	
	/**
	 * Returns whether or not motion was detected in the latest image. This is
	 * taken from the image index in the jar-file. See getJPEG().
	 */

	public boolean motionDetected() {
		return motionDetected;
	}

	/**
	 * Puts the capture time of the latest image in the specified target byte array, starting at
	 * offset. The resolution is milliseconds.
	 * 
	 * @param target
	 *            the array to be written into
	 * @param offset
	 *            the starting position
	 */
	public void getTime(byte[] target, int offset) {
		int minLength = TIME_ARRAY_SIZE + offset;
		if (target.length < minLength) {
			throw new IllegalArgumentException("Length of target is too short, is " + target.length +" should be atleast" + minLength);
		}
		System.arraycopy(latestTime, 0, target, offset, TIME_ARRAY_SIZE);
	}

	/**
	 * Closes the camera connection.
	 */
	public void close() {
	}

	public void destroy() {
	}

	// -------------------------------------------------------- PRIVATE METHODS

	/**
	 * Reads images from the media jar file into the fakes Vector. Returns image
	 * max size + 1024
	 * 
	 */
	private void readJpegs() {
		// Read individual images from the jar file
		for (int i = 1; i <= MAX; i++) {
			byte[] jpeg = readJarFile(mediaName(i));
			fakes.addElement(jpeg);
		}
	}

	/**
	 * Media filename
	 */
	private String mediaName(int n) {
		StringBuffer sb = new StringBuffer();
		sb.append("/res/media/film");
		if (n < 10)
			sb.append("0");
		if (n < 100)
			sb.append("0");
		sb.append(Integer.toString(n));
		sb.append(".jpg");
		return sb.toString();
	}

	/**
	 * Reads an image from the jar file. If no image is found an error is
	 * thrown.
	 */
	private byte[] readJarFile(String filename) {
		final int BUF_SIZE = 1024;
		try {
			InputStream stream = this.getClass().getResourceAsStream(filename);

			// File size
			int fileSize = 0;
			byte[] part = new byte[BUF_SIZE];
			int partLen = 0;
			while ((partLen = stream.read(part, 0, BUF_SIZE)) != -1) {
				fileSize += partLen;
			}
			stream.close();

			// Read file
			stream = this.getClass().getResourceAsStream(filename);
			byte[] buf = new byte[fileSize];
			int len = 0;
			do {
				len += stream.read(buf, len, fileSize - len);
			} while (len != fileSize);
			stream.close();

			return buf;
		} catch (IOException e) {
			throw new Error("Error loading " + filename);
		}
	}

	/**
	 * Return image index based on system time. Endless loop. Blocks until the
	 * image is "taken".
	 * 
	 */
	private int imageIndex() {
		long stime = System.currentTimeMillis();

		// Millis between images
		float period = 1.0f / rate * 1000;

		// Total amount of millis per loop
		long loop = (long) (MAX * period);

		// Loop time
		long time = stime % loop;

		// Image index
		int index = (int) Math.floor(time / period);

		// Wait time
		long wtime = (long) (time - index * period);
		wtime = wtime < 0 ? wtime + loop : wtime;

		try {
			Thread.sleep(wtime);
		} catch (InterruptedException e) {
			throw new Error("Interrupted!");
		}
		return index;
	}
	
	private void storeTime() {
		long stime = System.currentTimeMillis();
		int index = 0;
		latestTime[index++] = (byte) ((stime & 0xff00000000000000L)>>56);
		latestTime[index++] = (byte) ((stime & 0x00ff000000000000L)>>48);
		latestTime[index++] = (byte) ((stime & 0x0000ff0000000000L)>>40);
		latestTime[index++] = (byte) ((stime & 0x000000ff00000000L)>>32);
		latestTime[index++] = (byte) ((stime & 0x00000000ff000000L)>>24);
		latestTime[index++] = (byte) ((stime & 0x0000000000ff0000L)>>16);
		latestTime[index++] = (byte) ((stime & 0x000000000000ff00L)>>8);
		latestTime[index++] = (byte) ((stime & 0x00000000000000ffL));
		
	}

}

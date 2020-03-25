package se.lth.cs.edaf55;

public interface Camera {
	public static final int TIME_ARRAY_SIZE = 8;
	public static final int IMAGE_BUFFER_SIZE = 128 * 1024;
	public static final int IMAGE_WIDTH = 640;
	public static final int IMAGE_HEIGHT = 480;

	// -------------------------------------------------------- PUBLIC METHODS
	
	/**
	 * Initialize resources used by the camera.
	 */
	public void init();
	
	/**
	 * Set the location of a proxy camera.
	 * @param host is host of proxy camera (argus-N)
	 * @param port is port of proxy camera
	 */
	public void setProxy(String host, int port);

	/**
	 * Connect to the camera.
	 * 
	 * @return true if connected otherwise false.
	 */
	public boolean connect();

	/**
	 * Read an image from the camera and put it in the array target starting
	 * at index offset. The size of target needs to be at least
	 * offset+IMAGE_BUFFER_SIZE.
	 * 
	 * @param target reference to byte array to write image into.
	 * @param offset offset from the start of the byte array.
	 * 
	 * @return the length of the image captured, 0 if no picture was captured
	 */
	public int getJPEG(byte[] target, int offset);

	/**
	 * Return true if motion was detected in the latest image.
	 */
	public boolean motionDetected();

	/**
	 * Copy the capture time of the latest image in the specified target byte array, starting at
	 * offset. The resolution is milliseconds.
	 * 
	 * @param target is the byte array to be written into
	 * @param offset is the array starting position
	 */
	public void getTime(byte[] target, int offset);

	/**
	 * Close the camera connection.
	 */
	public void close();
	
	/**
	 * Destroy the camera object, returning all used resources.
	 */
	public void destroy();
}

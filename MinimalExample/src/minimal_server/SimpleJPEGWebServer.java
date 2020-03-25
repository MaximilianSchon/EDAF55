package minimal_server;

/*
 * Real-time and concurrent programming
 *
 * Minimalistic HTTP server solution.
 *
 * Package created by Patrik Persson, maintained by klas@cs.lth.se
 * Adapted for Axis cameras by Roger Henriksson 
 */

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

import se.lth.cs.edaf55.proxycamera.AxisM3006V;
//import se.lth.cs.edaf55.fakecamera.AxisM3006V;

/**
 * Itsy bitsy teeny weeny web server. Always returns an image, regardless of the
 * requested file name.
 */
public class SimpleJPEGWebServer {

	/**
	 * An Axis camera that runs proxyserver. This has to match a proxyserver you
	 * have started on the given camera host.
	 */
	private static String CAMERA_HOST = "argus-6.student.lth.se";
	private static int CAMERA_PORT = 8888;

	/** Port number for web server. The client will connect on this port. */
	private static final int WEB_SERVER_PORT = 8080;

	// ----------------------------------------------------------- MAIN PROGRAM

	public static void main(String[] args) throws IOException {
		SimpleJPEGWebServer server = new SimpleJPEGWebServer();

		AxisM3006V camera = new AxisM3006V();
		camera.init();
		camera.setProxy(CAMERA_HOST, CAMERA_PORT);

		server.handleRequests(camera, WEB_SERVER_PORT);
	}

	// --------------------------------------------------------- PUBLIC METHODS

	/**
	 * This method handles client requests. Runs in an eternal loop that does the
	 * following:
	 * <UL>
	 * <LI>Waits for a client to connect
	 * <LI>Reads a request from that client
	 * <LI>Sends a JPEG image from the camera (if it's a GET request)
	 * <LI>Closes the socket, i.e. disconnects from the client.
	 * </UL>
	 */
	public void handleRequests(AxisM3006V camera, int port) throws IOException {

		ServerSocket serverSocket = new ServerSocket(port);
		System.out.println("HTTP server listening at port " + port + ", keenly awaiting client requests.");

		while (true) {
			try {
				// The 'accept' method waits for a client to connect, then
				// returns a socket connected to that client.
				Socket clientSocket = serverSocket.accept();

				// The socket is bi-directional: it has one input stream, and one output stream.
				// We use a Scanner to read from the input stream, and a PrintStream to write to
				// the output stream.
				Scanner scan = new Scanner(clientSocket.getInputStream());
				PrintStream out = new PrintStream(clientSocket.getOutputStream());

				String request = scan.nextLine();  // Read the request

				// The request is followed by some additional header lines,
				// followed by a blank line. Those header lines are ignored.
				String header = scan.nextLine();
				while (!header.equals("")) {
					header = scan.nextLine();
				}

				System.out.println("HTTP request '" + request + "' received");

				// Interpret the request. Complain about everything but GET.
				// Ignore the file name.
				if (request.startsWith("GET ")) {
					// Got a GET request. Respond with a JPEG image from the
					// camera. Tell the client not to cache the image
					out.println("HTTP/1.0 200 OK");
					out.println("Content-Type: image/jpeg");
					out.println("Pragma: no-cache");
					out.println("Cache-Control: no-cache");
					out.println(); // Empty line means 'end of headers'

					// Retrieve image data from the camera
					if (!camera.connect()) {
						System.out.println("Failed to connect to camera!");
						System.exit(1);
					}
					byte[] imageBuffer = new byte[AxisM3006V.IMAGE_BUFFER_SIZE];
					int len = camera.getJPEG(imageBuffer, 0);

					// Retrieve a timestamp as a sequence of bytes.
					byte[] timeBuffer = new byte[AxisM3006V.TIME_ARRAY_SIZE];
					camera.getTime(timeBuffer, 0);
					
					// This format is convenient for sending over the network,
					// but to use it, we need to convert it to a long.
					long time = ByteBuffer.wrap(timeBuffer).getLong();
					System.out.println("Image captured at t=" + time);

					out.write(imageBuffer, 0, len);

					camera.close();
				} else {
					// Got some other request. Respond with an error message.
					out.println("HTTP/1.0 501 Method not implemented");
					out.println("Content-Type: text/plain");
					out.println();
					out.println("No can do. Request '" + request + "' not understood.");

					System.out.println("Unsupported HTTP request!");
				}

				out.flush(); // Flush any remaining content
				clientSocket.close(); // Disconnect from the client
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
package minimal_client;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * A thread for loading an image from our simple web server. This thread loads
 * the image asynchronously, and then uses Platform.runLater() to make sure the
 * JavaFX UI is updated in its own thread.
 */
public class ImageLoaderThread extends Thread {
	/** Address of web server. The name "localhost" means "this machine". */
	private static final String WEB_SERVER_ADDR = "localhost";

	/** Port number for web server. The client will connect on this port. */
	private static final int WEB_SERVER_PORT = 8080;

	private ImageView imageView;

	/** Loads an image from the server and displays it in an ImageView. */
	public ImageLoaderThread(ImageView imageView) {
		this.imageView = imageView;
	}

	public void run() {
		try {
			Socket sock = new Socket(WEB_SERVER_ADDR, WEB_SERVER_PORT);

			// We can't use a Scanner here, since we need the raw InputStream
			// to construct an Image below.
			InputStream is = sock.getInputStream();
			PrintStream out = new PrintStream(sock.getOutputStream());

			// Send a simple request, always for "/image.jpg"
			out.println("GET /image.jpg HTTP/1.0");
			out.println(); // The request ends with an empty line

			// Read the first line of the response (status line). Since we can't use
			// scan.nextLine(), we need to introduce our own getLine() method.
			String responseLine = getLine(is);
			System.out.println("Server responded '" + responseLine + "'.");

			// Ignore the following header lines up to the final empty one.
			String header = getLine(is);
			while (!header.equals("")) {
				header = getLine(is);
			}

			// The JavaFX Image class can read image data from an InputStream.
			// This feature comes in handy here.
			Image image = new Image(is);
			sock.close();

			// Make sure the user interface is updated in JavaFX's own thread.
			Platform.runLater(() -> imageView.setImage(image));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read a line from InputStream 's', terminated by CRLF (bytes 13 and 10). The
	 * CRLF is not included in the returned string.
	 */
	private static String getLine(InputStream s) throws IOException {
		StringBuilder result = new StringBuilder();

		while (true) {
			int ch = s.read();
			if (ch <= 0 || ch == 10) {
				// Something < 0 means end of data (closed socket)
				// ASCII 10 (line feed) means end of line

				return result.toString();
			} else if (ch >= ' ') {
				result.append((char) ch);
			}
		}
	}
}

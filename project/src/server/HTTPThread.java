package server;


import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

// import se.lth.cs.edaf55.proxycamera.AxisM3006V;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import shared.ImageWithTimestamp;

import javax.imageio.ImageIO;

public class HTTPThread extends Thread {

    /**
     * An Axis camera that runs proxyserver. This has to match a proxyserver you
     * have started on the given camera host.
     */
    private static String CAMERA_HOST = "argus-2.student.lth.se";
    private static int CAMERA_PORT = 8888;

    /** Port number for web server. The client will connect on this port. */
    private static final int WEB_SERVER_PORT = 8080;

    private int port;
    private ServerMonitor serverMonitor;

    HTTPThread(ServerMonitor serverMonitor, int port) {
        this.serverMonitor = serverMonitor;
        this.port = port;
    }


    @Override
    public void run() {
        try {
        ServerSocket serverSocket = new ServerSocket(port);

        System.out.println("HTTP server listening at port " + port + ", keenly awaiting client requests.");

        while (true) {

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

                ImageWithTimestamp iwt = serverMonitor.getLatest();
                long time = iwt.getTime();
                Image image = iwt.getImage();
                System.out.println("Image captured at t=" + time);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage( image, null);
                ImageIO.write(bufferedImage, "jpg", out);
                    //  out.write(byteArrayOutputStream.toByteArray(), 0, len);
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
        }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

    }
}

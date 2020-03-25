package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConnectionManagerThread extends Thread {

    private ServerMonitor serverMonitor;
    private int port;
    private ServerSocketThread connection;
    private CameraThread cameraThread;
    private ModeThread modeThread;
    private String camera;
    private long delay;

    ConnectionManagerThread(ServerMonitor sm, int port, String camera, long delay) {
        this.port = port;
        serverMonitor = sm;
        this.camera = camera;
        this.delay = delay;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("We're live on port " + port + "!");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                if (connection != null) {
                    if (connection.isAlive()) {
                        reject(clientSocket);
                    } else {
                        accept(clientSocket);
                    }
                } else {
                    accept(clientSocket);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Bye!");
        }
    }

    private void accept(Socket clientSocket) {
        System.out.println("Incoming connection from: " + clientSocket.getInetAddress().getHostAddress());
        cameraThread = new CameraThread(serverMonitor, camera, delay);
        connection = new ServerSocketThread(serverMonitor, clientSocket, this);
        modeThread = new ModeThread(clientSocket, serverMonitor);
        cameraThread.start();
        connection.start();
        modeThread.start();
    }

    private void reject(Socket clientSocket) throws IOException {
    }

    public void disconnect() {
        this.connection.interrupt();
        this.connection = null;
        this.cameraThread.interrupt();
        this.cameraThread = null;
        this.modeThread.interrupt();
        this.modeThread = null;
    }
}
package client;

import shared.ImageWithTimestamp;
import shared.Mode;

import java.io.*;
import java.net.Proxy;
import java.net.Socket;

public class ClientSocketThread extends Thread {
    private transient String ip;
    private int port;
    private ClientMonitor monitor;
    private int placement;
    private Mode mode = Mode.IDLE;
    private boolean alive = true;
    private Socket sock;


    ClientSocketThread(ClientMonitor monitor, String ip, int port, int placement) {
        this.ip = ip;
        this.port = port;
        this.monitor = monitor;
        this.placement = placement;
        alive = true;
    }

    @Override
    public void run() {
        try {
            sock = new Socket(ip, port);

            ObjectInputStream is = new ObjectInputStream(sock.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
            out.writeUnshared(monitor.getMode());

            while (sock.isConnected()) {
                Object obj = is.readUnshared();
                    if (obj instanceof ImageWithTimestamp) {
                    ImageWithTimestamp image = (ImageWithTimestamp) obj;
                    monitor.putImage(placement, image);
                } else if (obj instanceof Mode) {
                    Mode mode = (Mode) obj;
                    monitor.setMode(mode);
                    this.mode = mode;
                    System.out.println("Got mode!");
                    }

                if (monitor.getMode() != mode) {
                    mode = monitor.getMode();
                    System.out.println("Sending mode: " + mode);
                    out.writeUnshared(mode);
                }
            }

            sock.close();

            System.out.println("Disconnecting from server...");
            this.join();

        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
            try {
                sock.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void kill() throws IOException {
        sock.close();
    }
}

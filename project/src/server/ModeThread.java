package server;

import shared.Mode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ModeThread extends Thread {

    private final ServerMonitor serverMonitor;
    private final Socket clientSocket;

    ModeThread(Socket clientSocket, ServerMonitor serverMonitor) {
        this.clientSocket = clientSocket;
        this.serverMonitor = serverMonitor;
    }

    @Override
    public void run() {
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            while (true) {
                Object obj = objectInputStream.readUnshared();
                if (obj instanceof Mode) {
                    Mode mode = (Mode) obj;
                    if (serverMonitor.getMode() == Mode.MOVIE && mode == Mode.AUTO){
                        serverMonitor.setMode(Mode.AUTO);
                    }
                    else serverMonitor.setMode(mode);
                    System.out.println("Got mode: " + mode);
                }
            }
        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

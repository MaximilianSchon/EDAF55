package server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerSocketThread extends Thread {

    private Socket clientSocket;
    private ServerMonitor serverMonitor;
    private ConnectionManagerThread parent;

    ServerSocketThread(ServerMonitor sm, Socket clientSocket, ConnectionManagerThread parent) {
        this.clientSocket = clientSocket;
        this.serverMonitor = sm;
        this.parent = parent;
    }

    @Override
    public void run() {
        try {

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            while (clientSocket.isConnected()) {
                //enable for lag/stutter
                //if (Math.random() > 0.99) Thread.sleep((long)(Math.random()*500) + 1000);

                objectOutputStream.writeUnshared(serverMonitor.getImage());
                if(serverMonitor.modeUpdated()) {
                    //System.out.println("gets here");
                    objectOutputStream.writeUnshared(serverMonitor.getMode());
                    serverMonitor.setModeUpdated(false);
                }
            }

            System.out.println("Client disconnected!");
            objectOutputStream.flush();
            objectOutputStream.close();
            clientSocket.close();
            parent.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                clientSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            parent.disconnect();
            System.out.println("Client disconnected!");
        }
    }
}

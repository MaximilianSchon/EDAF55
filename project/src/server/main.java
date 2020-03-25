package server;

public class main {
    public static void main(String[] args){
        int port = Integer.parseInt(args[0]);
        String camera = args[1];
        long delay = 0;
        if (args.length == 3){
            delay = Long.parseLong(args[2]);
        }
        ServerMonitor serverMonitor = new ServerMonitor();
        ConnectionManagerThread socketThread = new ConnectionManagerThread(serverMonitor, port, camera, delay);
        HTTPThread httpThread = new HTTPThread(serverMonitor, 43594);
        socketThread.start();
        httpThread.start();
    }
}

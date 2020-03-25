package client;


import shared.ImageWithTimestamp;
import shared.Mode;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Observable;

class ClientMonitor extends Observable {
    public final static int AUTO_SYNC = 0;
    public final static int SYNC = 1;
    public final static int ASYNC = 2;


    private Mode mode = Mode.IDLE;
    private HashMap<Integer, ArrayDeque<ImageWithTimestamp>> images;
    private HashMap<Integer, ClientSocketThread> connections;
    private long[] timestamp;
    private long[] delay;
    private int sync = AUTO_SYNC;

    ClientMonitor() {
        images = new HashMap<>();
        connections = new HashMap<>();
        timestamp = new long[2];
        timestamp[0] = -1;
        timestamp[1] = -1;
        delay = new long[2];
        delay[0] = 0;
        delay[1] = Long.MAX_VALUE;
    }

    synchronized ImageWithTimestamp getImage(int placement) throws InterruptedException {
        while (!images.containsKey(placement)) wait();
        while (images.get(placement).isEmpty()) wait();

        ImageWithTimestamp img = images.get(placement).poll();
        timestamp[placement] = img.getTime();
        delay[placement] = System.currentTimeMillis() - timestamp[placement];

        if (connections.size() < 2 || //one connection doesn't need sync
                sync == ASYNC || //forced async mode
                (sync == AUTO_SYNC && Math.abs(delay[0] - delay[1]) > 200) || //auto sync and delay too long
                timestamp[1 - placement] == -1) { // first pair of images haven't arrived yet
            images.values().forEach(e -> {
                while (e.size() > 1)
                e.poll();
            });
            setChanged();
            notifyObservers();
            return img; //no syncing
        }

        //otherwise we need sync
        long thisDelay = timestamp[0] - timestamp[1];
        long nowTime = System.currentTimeMillis();

        if (placement == 0 && thisDelay > 0)
            while (System.currentTimeMillis() < nowTime + thisDelay)
                wait(nowTime + thisDelay - System.currentTimeMillis());
        if (placement == 1 && thisDelay < 0)
            while (System.currentTimeMillis() < nowTime - thisDelay)
                wait(nowTime - thisDelay - System.currentTimeMillis());
        setChanged();
        notifyObservers();
        return img;
    }

    synchronized void putImage(int placement, ImageWithTimestamp image) {
        if (images.get(placement) != null) images.get(placement).add(image);
        notifyAll();
    }


    synchronized void connect(String ip, int port, int placement) {
        setChanged();
        notifyObservers();
        ClientSocketThread connection = new ClientSocketThread(this, ip, port, placement);
        connections.put(placement, connection);
        images.put(placement, new ArrayDeque<>());
        connection.start();
    }

    synchronized void disconnect(int placement) throws IOException {
        setChanged();
        notifyObservers();
        ClientSocketThread connection = connections.remove(placement);
        images.remove(placement).clear();
        connection.kill();
        timestamp[placement] = -1;
    }

    synchronized void setMode(Mode mode) {
        images.values().forEach(e -> e.clear());
        setChanged();
        notifyObservers();
        this.mode = mode;
    }

    synchronized Mode getMode() {
        return mode;
    }

    synchronized void setSync(int sync) {
        images.values().forEach(e -> e.clear());
        this.sync = sync;
    }

    synchronized boolean getSync() {
        return connections.size() >= 2 && sync != ASYNC && Math.abs(delay[0] - delay[1]) <= 200;
    }

    synchronized String getDelay(int placement) {
        return Long.toString(delay[placement]);
    }
}

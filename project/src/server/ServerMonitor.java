package server;

import shared.ImageWithTimestamp;
import shared.Mode;

import java.util.ArrayDeque;
import java.util.Queue;

public class ServerMonitor {
    private Queue<ImageWithTimestamp> images;
    private ImageWithTimestamp latest;
    private Mode mode = Mode.IDLE;
    private boolean newMode = false;

    ServerMonitor() {
        images = new ArrayDeque<>();
    }

    public synchronized ImageWithTimestamp getImage() throws InterruptedException {
        while (images.isEmpty()) wait();
        return images.poll();
    }

    synchronized void putImage(ImageWithTimestamp image) {
        images.offer(image);
        latest = image;
        notifyAll();
    }

    synchronized ImageWithTimestamp getLatest() throws InterruptedException {
        while (latest == null) wait();
        return latest;
    }

    synchronized boolean modeUpdated() {
        return newMode;
    }

    synchronized void setModeUpdated(boolean newMode) {
        this.newMode = newMode;
    }

    synchronized void setMode(Mode mode) {
        if (this.mode != mode) {
            this.mode = mode;
        }
    }

    synchronized Mode getMode() {
        return mode;
    }

    synchronized void clear() {
        latest = null;
        mode = Mode.IDLE;
        newMode = false;
        images = new ArrayDeque<>();
    }
}

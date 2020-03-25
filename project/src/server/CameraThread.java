package server;

import javafx.scene.image.Image;
import se.lth.cs.edaf55.proxycamera.AxisM3006V;
//import se.lth.cs.edaf55.fakecamera.AxisM3006V;
import shared.ImageWithTimestamp;
import shared.Mode;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

public class CameraThread extends Thread {

    private AxisM3006V camera;
    private ServerMonitor serverMonitor;
    private String ip;
    private long delay = 0;

    private CameraThread(ServerMonitor sm, String ip) {
        this.ip = ip;
        serverMonitor = sm;
        camera = new AxisM3006V();
    }

    CameraThread(ServerMonitor sm, String ip, long delay) {
        this(sm, ip);
        this.delay = delay;
    }

    @Override
    public void run() {
        System.out.println("Camera thread starting...");
        camera.init();
        camera.setProxy(ip, 8888);
        if (!camera.connect()) {
            System.out.println("Failed to connect to camera!");
            System.exit(1);
        }
        while (true) {

            byte[] imageBuffer = new byte[AxisM3006V.IMAGE_BUFFER_SIZE];
            int len = camera.getJPEG(imageBuffer, 0);

            // Retrieve a timestamp as a sequence of bytes.
            byte[] timeBuffer = new byte[AxisM3006V.TIME_ARRAY_SIZE];
            camera.getTime(timeBuffer, 0);

            if (camera.motionDetected() && serverMonitor.getMode() == Mode.AUTO) {
                serverMonitor.setMode(Mode.MOVIE);
                serverMonitor.setModeUpdated(true);
            }

            // This format is convenient for sending over the network,
            // but to use it, we need to convert it to a long.
            long time = ByteBuffer.wrap(timeBuffer).getLong() - delay;
//            System.out.println("Image captured at t=" + time);

            ByteArrayInputStream byteStream = new ByteArrayInputStream(imageBuffer);

            Image img = new Image(byteStream);

            ImageWithTimestamp iwt = new ImageWithTimestamp(img, time);

            if (serverMonitor.getMode() != Mode.MOVIE) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }

//            System.out.println("Image properties: ");
//            System.out.println("Size: " + iwt.getImage().getWidth() + " x " + iwt.getImage().getHeight());
//            System.out.println("Time: " + iwt.getTime());

            serverMonitor.putImage(iwt);
        }
    }

}

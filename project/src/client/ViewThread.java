package client;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import shared.ImageWithTimestamp;

import java.util.Date;

public class ViewThread extends Thread {
    private ClientMonitor monitor;
    private ImageView view;
    private int placement;
    private Label delayLabel;
    private Label timestampLabel;

    ViewThread(ClientMonitor monitor, ImageView view, Label delayLabel, Label timestampLabel, int placement) {
        this.monitor = monitor;
        this.view = view;
        this.placement = placement;
        this.delayLabel = delayLabel;
        this.timestampLabel = timestampLabel;
    }

    @Override
    public void run() {
        delayLabel.setVisible(true);
        timestampLabel.setVisible(true);
        while (true) {
            try {
                ImageWithTimestamp iwt = monitor.getImage(placement);
                Platform.runLater(() -> {
                    delayLabel.setText(monitor.getDelay(placement));
                    timestampLabel.setText(new Date(iwt.getTime()).toString());
                });
                Platform.runLater(() -> view.setImage(iwt.getImage()));
            } catch (InterruptedException e) {
                view.setImage(null);
                delayLabel.setVisible(false);
                timestampLabel.setVisible(false);
            }
        }
    }
}

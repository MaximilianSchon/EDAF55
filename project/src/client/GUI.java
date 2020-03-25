package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import se.lth.cs.edaf55.Camera;
import server.ServerMonitor;
import shared.Mode;

import javax.swing.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

public class GUI extends Application implements Observer {
    private ClientMonitor monitor;
    private HashMap<Integer, ViewThread> views;
    private ToggleGroup tg;
    private Label syncLabel;

    @Override
    public void start(Stage primaryStage) {
        monitor = new ClientMonitor();
        monitor.addObserver(this);
        views = new HashMap<>();

        /* Sync */
        syncLabel = new Label("Not synchronized");
        syncLabel.setTextFill(Color.web("FF0000"));
        RadioButton autoSyncButton = new RadioButton("Auto sync");
        RadioButton syncButton = new RadioButton("Sync");
        RadioButton asyncButton = new RadioButton("Async");
        ToggleGroup tg2 = new ToggleGroup();
        autoSyncButton.setOnAction(e -> monitor.setSync(ClientMonitor.AUTO_SYNC));
        syncButton.setOnAction(e -> monitor.setSync(ClientMonitor.SYNC));
        asyncButton.setOnAction(e -> monitor.setSync(ClientMonitor.ASYNC));
        autoSyncButton.setToggleGroup(tg2);
        syncButton.setToggleGroup(tg2);
        asyncButton.setToggleGroup(tg2);
        autoSyncButton.setSelected(true);


        /* Server buttons */
        Button connect1 = new Button("Connect server 1");
        Button connect2 = new Button("Connect server 2");
        Button disconnect1 = new Button("Disconnect server 1");
        Button disconnect2 = new Button("Disconnect server 2");

        /* IP box */
        TextField ip = new TextField("127.0.0.1:8080");

        /* Modes */
        RadioButton auto = new RadioButton("Auto");
        RadioButton idle = new RadioButton("Idle");
        RadioButton movie = new RadioButton("Movie");
        tg = new ToggleGroup();
        auto.setOnAction(e -> monitor.setMode(Mode.AUTO));
        idle.setOnAction(e -> monitor.setMode(Mode.IDLE));
        movie.setOnAction(e -> monitor.setMode(Mode.MOVIE));
        auto.setToggleGroup(tg);
        idle.setToggleGroup(tg);
        movie.setToggleGroup(tg);
        idle.setSelected(true);
        auto.setUserData(Mode.AUTO);
        idle.setUserData(Mode.IDLE);
        movie.setUserData(Mode.MOVIE);

        /* Delay */
        Label delay1 = new Label("");
        Label delay2 = new Label("");
        delay1.setTranslateY(220);
        delay2.setTranslateY(220);
        delay2.setTranslateX(280);
        delay1.setTranslateX(280);
        delay1.setTextFill(Color.web("#FFFF00"));
        delay2.setTextFill(Color.web("#FFFF00"));

        /* Timestamp */
        Label ts1 = new Label("");
        Label ts2 = new Label("");
        ts1.setTranslateY(-220);
        ts2.setTranslateY(-220);
        ts1.setTextFill(Color.web("#FFFF00"));
        ts2.setTextFill(Color.web("#FFFF00"));

        /* ImageViews */
        ImageView imageView1 = new ImageView();
        ImageView imageView2 = new ImageView();
        connect1.setOnAction(e -> connect(ip.getText(), 0, imageView1, delay1, ts1));
        connect2.setOnAction(e -> connect(ip.getText(), 1, imageView2, delay2, ts2));
        disconnect1.setOnAction(e -> {
            try {
                disconnect(0);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        disconnect2.setOnAction(e -> {
            try {
                disconnect(1);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        /* Layout */

        BorderPane root = new BorderPane();
        HBox bottomBox = new HBox();
        HBox viewBox = new HBox();
        HBox topBox = new HBox();
        StackPane pane1 = new StackPane();
        StackPane pane2 = new StackPane();
        topBox.getChildren().addAll(ip, syncLabel);
        topBox.setSpacing(400);
        pane1.getChildren().addAll(imageView1, delay1, ts1);
        pane2.getChildren().addAll(imageView2, delay2, ts2);
        viewBox.getChildren().addAll(pane1, pane2);
        bottomBox.getChildren().addAll(connect1, disconnect1, connect2, disconnect2, auto, idle, movie, autoSyncButton, syncButton, asyncButton);
        root.setCenter(viewBox);
        root.setBottom(bottomBox);
        root.setTop(topBox);

        /* Scene */
        Scene scene = new Scene(root, Camera.IMAGE_WIDTH * 2, Camera.IMAGE_HEIGHT + 55);
        primaryStage.setTitle("GUI");
        primaryStage.setScene(scene);
        primaryStage.show();

        }

    private void connect(String address, int placement, ImageView view, Label delayLabel, Label timestampLabel) {
        try {
            if (!address.contains(":")) throw new Exception("Wrong address format");
            String[] split = address.split(":");
            String ip = split[0];
            int port = Integer.parseInt(split[1]);
            monitor.connect(ip, port, placement);
            ViewThread viewThread = new ViewThread(monitor, view, delayLabel, timestampLabel, placement);
            views.put(placement, viewThread);
            viewThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disconnect(int placement) throws IOException {
        monitor.disconnect(placement);
        views.remove(placement).interrupt();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void update(Observable o, Object arg) {
       for (Toggle toggle: tg.getToggles()) {
           if (monitor.getMode() == toggle.getUserData()) Platform.runLater(() -> toggle.setSelected(true));
       }
        if (monitor.getSync()) {
            Platform.runLater(() -> {
                syncLabel.setText("Synchronized");
                syncLabel.setTextFill(Color.web("#00FF00"));
            });
        } else {
            Platform.runLater(() ->  {
                syncLabel.setText("Not synchronized");
                syncLabel.setTextFill(Color.web("#FF0000"));
            });
        }

    }
}

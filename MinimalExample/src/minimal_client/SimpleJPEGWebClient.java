package minimal_client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import se.lth.cs.edaf55.Camera;
import javafx.scene.image.ImageView;

/** A minimal GUI for viewing camera images. */
public class SimpleJPEGWebClient extends Application {

	/** Connect to web server, load an image, and display it in the given ImageView. */
	private void loadImage(ImageView imageView) {
		Thread loader = new ImageLoaderThread(imageView);
		loader.start();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {

		BorderPane root = new BorderPane();
		Button button = new Button("Load");
		ImageView imageView = new ImageView();
		
		button.setOnAction(e -> loadImage(imageView));

		root.setCenter(imageView);
		root.setBottom(button);
		
		Scene scene = new Scene(root, Camera.IMAGE_WIDTH, Camera.IMAGE_HEIGHT + 30);
		primaryStage.setTitle("Image Viewer Example");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public static void main(String[] args) {
		Application.launch(args);
	}
}

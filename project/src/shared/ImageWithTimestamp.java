package shared;


import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ImageWithTimestamp implements Serializable, Comparable {

    private transient long timestamp;
    private transient Image image;

    public ImageWithTimestamp(Image img, long timestamp) {
        image = img;
        this.timestamp = timestamp;
    }

    public Image getImage() {
        return image;
    }

    public long getTime() {
        return timestamp;
    }

    @Override
    public int compareTo(Object o) {
        return (int) (timestamp - ((ImageWithTimestamp) o).timestamp);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeLong(timestamp);
        ImageIO.write(SwingFXUtils.fromFXImage( image, null), "jpg", out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        timestamp = in.readLong();
        image = SwingFXUtils.toFXImage( ImageIO.read(in), null);
    }
}

package Network.IO.Visual;

import Network.Graph;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by micha on 08.12.2016.
 */
public class RasterBuilder extends AbstractGraphicsBuilder {
    public RasterBuilder(Graph graph, String name) {
        super(graph);
        this.name = name;
    }

    @Override
    public void export() {
        BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
        this.canvas = img.createGraphics();
        this.canvas.setPaint(Color.WHITE);
        this.canvas.fill(new Rectangle2D.Double(0,0,1000,1000));

        this.layerprehook = s -> {
        };
        this.layerposthook = s -> {
        };
        this.drawAllLayers();

        Path path = Paths.get(this.name + ".png");

        try {
            Files.createDirectories(path.getParent());
            ImageIO.write(img, "PNG", path.toFile());
            System.out.println("Wrote PNG file " + path);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

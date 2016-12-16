package Network.IO.Visual;

import Network.Graph;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by micha on 16.11.2016.
 */
public class SVGBuilder extends AbstractGraphicsBuilder {
    public SVGBuilder(Graph graph, String name) {
        super(graph);
        this.name = name;
    }

    @Override
    public void export() {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document doc = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
        SVGGraphics2D canvas = new SVGGraphics2D(doc);

        this.canvas = canvas;

        this.layerprehook = s -> {
        };
        this.layerposthook = s -> {
        };
        this.drawAllLayers();

        Path path = Paths.get(this.name + ".svg");
        try {
            Files.createDirectories(path.getParent());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), "UTF-8")) {
            canvas.stream(w, true);
            System.out.println("Wrote SVG file " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

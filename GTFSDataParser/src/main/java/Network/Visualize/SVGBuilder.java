package Network.Visualize;

import Network.Graph;
import Network.Node;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by micha on 16.11.2016.
 */
public class SVGBuilder {
    private Graph graph;
    private String name;
    private AffineTransform matrix;

    public SVGBuilder(Graph graph, String name) {
        this.graph = graph;
        this.name = name;
    }

    private void calcCoordinateRange() {
        DoubleSummaryStatistics westeast = this.graph.getNodes().stream().mapToDouble(Node::getLat).summaryStatistics();
        double west = westeast.getMin();
        double east = westeast.getMax();
        DoubleSummaryStatistics northsouth = this.graph.getNodes().stream().mapToDouble(Node::getLon).summaryStatistics();
        double north = northsouth.getMax();
        double south = northsouth.getMin();

        System.out.println("North: " + north + " South: " + south + " East: " + east + " West: " + west);

        double xscale = 800.0 / (Math.abs(east - west));
        double yscale = 800.0 / (Math.abs(north - south));

        double xtrans = -west;
        double ytrans = -south;

        AffineTransform subtractmat = new AffineTransform(1.0, 0.0, 0.0, 1.0, xtrans, ytrans);
        AffineTransform scalemat = new AffineTransform(xscale, 0.0, 0.0, yscale, 0.0, 0.0);
        AffineTransform bordertransmat = new AffineTransform(1.0, 0.0, 0.0, 1.0, 50.0, 50.0);

        this.matrix = new AffineTransform();
        this.matrix.setToIdentity();
        this.matrix.concatenate(bordertransmat);
        this.matrix.concatenate(scalemat);
        this.matrix.concatenate(subtractmat);

        //System.out.println("Affine Matrix: " + this.matrix);
    }

    private Point2D transformPoint(Point2D point) {
        return this.matrix.transform(point, new Point2D.Double());
    }

    private void drawEdges(Graphics2D canvas) {
        Map<Integer, Node> nodemap = new HashMap<>(this.graph.getNodes().size());
        this.graph.getNodes().forEach(x -> nodemap.put(x.getId(), x));
        canvas.setPaint(Color.BLUE);
        canvas.setStroke(new BasicStroke(2.0f));
        this.graph.getEdges().forEach(edge ->
                canvas.draw(new Line2D.Double(
                        this.transformPoint(nodemap.get(edge.getA()).getPoint()),
                        this.transformPoint(nodemap.get(edge.getB()).getPoint())
                ))
        );
    }

    private void drawNodes(Graphics2D canvas) {
        canvas.setPaint(Color.ORANGE);
        double radius = 5.0;
        AffineTransform circlecoord = new AffineTransform(1.0, 0.0, 0.0, 1.0, -radius / 2.0, -radius / 2.0);
        this.graph.getNodes().forEach(node -> { //78.5 is the area of a circle with radius 5
            /*
            double radius = Math.sqrt((78.5 + Math.pow(node.getNeighbours().size()*2,2)) / Math.PI);
            AffineTransform circlecoord = new AffineTransform(1.0, 0.0, 0.0, 1.0, -radius / 2.0, -radius / 2.0);
            */
            Point2D drawpoint = this.transformPoint(node.getPoint());
            circlecoord.transform(drawpoint, drawpoint);
            canvas.fill(new Ellipse2D.Double(drawpoint.getX(), drawpoint.getY(), radius, radius) {
            });
        });
    }

    private void testDraw(Graphics2D canvas) {
        canvas.setPaint(Color.BLUE);
        canvas.draw(new Line2D.Double(100, 100, 800, 800));
        canvas.setPaint(Color.ORANGE);
        canvas.fill(new Ellipse2D.Double(75, 75, 50, 50));
        canvas.fill(new Ellipse2D.Double(775, 775, 50, 50));
    }

    public void exportToSVG() {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document doc = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
        SVGGraphics2D svggen = new SVGGraphics2D(doc);
        //this.testDraw(svggen);
        this.calcCoordinateRange();
        this.drawEdges(svggen);
        this.drawNodes(svggen);

        Path path = Paths.get(this.name + ".svg");
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(path), "UTF-8")) {
            svggen.stream(w, true);
            System.out.println("Wrote SVG file " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

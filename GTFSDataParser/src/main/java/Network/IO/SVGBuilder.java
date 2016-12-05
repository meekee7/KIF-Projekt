package Network.IO;

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
import java.util.Random;

/**
 * Created by micha on 16.11.2016.
 */
public class SVGBuilder {
    private Graph graph;
    private String name;
    private CoordTransform transform;
    private Map<Integer, Node> nodemap;
    private Graphics2D canvas;

    public SVGBuilder(Graph graph, String name) {
        this.graph = graph;
        this.name = name;
    }

    private void drawEdges() {
        this.canvas.setPaint(Color.BLUE);
        this.canvas.setStroke(new BasicStroke(1.5f));
        this.graph.getEdges().forEach(edge ->
                this.canvas.draw(new Line2D.Double(
                        this.transform.transformPoint(this.nodemap.get(edge.getA()).getPoint()),
                        this.transform.transformPoint(this.nodemap.get(edge.getB()).getPoint())
                ))
        );
    }

    private void drawNodes() {
        this.canvas.setPaint(Color.ORANGE);
        double radius = 2.5;
        //AffineTransform circlecoord = new AffineTransform(1.0, 0.0, 0.0, 1.0, -radius, -radius);
        this.graph.getNodes().forEach(node -> { //78.5 is the area of a circle with radius 5
            /*
            double radius = Math.sqrt((78.5 + Math.pow(node.getNeighbours().size()*2,2)) / Math.PI);
            AffineTransform circlecoord = new AffineTransform(1.0, 0.0, 0.0, 1.0, -radius / 2.0, -radius / 2.0);
            */
            //Point2D drawpoint = this.transform.transformPoint(node.getPoint());
            //circlecoord.transform(drawpoint, drawpoint);
            //this.canvas.setPaint(Color.ORANGE);
            this.drawCircleAt(node.getPoint(), radius);
            //this.canvas.fill(new Ellipse2D.Double(drawpoint.getX(), drawpoint.getY(), radius * 2.0, radius * 2.0));
            //this.canvas.setPaint(Color.BLACK);
            //this.canvas.drawString(node.getName(), (float) drawpoint.getX() + 20.0f, (float) drawpoint.getY());
        });
    }

    private void drawNodeNames() {
        this.canvas.setPaint(Color.BLACK);
        this.graph.getNodes().forEach(node -> { //78.5 is the area of a circle with radius 5
            /*
            double radius = Math.sqrt((78.5 + Math.pow(node.getNeighbours().size()*2,2)) / Math.PI);
            AffineTransform circlecoord = new AffineTransform(1.0, 0.0, 0.0, 1.0, -radius / 2.0, -radius / 2.0);
            */
            Point2D drawpoint = this.transform.transformPoint(node.getPoint());
            //circlecoord.transform(drawpoint, drawpoint);
            //this.canvas.setPaint(Color.ORANGE);
            //this.canvas.fill(new Ellipse2D.Double(drawpoint.getX(), drawpoint.getY(), radius * 2.0, radius * 2.0));
            this.canvas.drawString(node.getName(), (float) drawpoint.getX() + 20.0f, (float) drawpoint.getY());
        });
    }

    private void drawCircleAt(Point2D p, double radius) {
        AffineTransform circlecoord = new AffineTransform(1.0, 0.0, 0.0, 1.0, -radius, -radius);
        Point2D drawpoint = this.transform.transformPoint(p);
        circlecoord.transform(drawpoint, drawpoint);
        this.canvas.fill(new Ellipse2D.Double(drawpoint.getX(), drawpoint.getY(), radius * 2.0, radius * 2.0));
    }

    private void drawLines() {
        this.canvas.setStroke(new BasicStroke(3.0f));
        this.graph.getLines().forEach(line -> {
            this.canvas.setPaint(line.getColourAWT());
            //this.canvas.setPaint(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            line.getStops().stream().reduce(null, (x, y) -> {
                if (x != null) {
                    this.canvas.draw(new Line2D.Double(
                            this.transform.transformPoint(x.getPoint()),
                            this.transform.transformPoint(y.getPoint())
                    ));
                }
                return y;
            });
            //line.getStartAndEnd().forEach(x -> this.drawCircleAt(x.getPoint(), 6.0));
        });
    }

    private void drawLineEnds() {
        this.canvas.setStroke(new BasicStroke(3.0f));
        this.graph.getLines().forEach(line -> {
            this.canvas.setPaint(line.getColourAWT());
            line.getStartAndEnd().forEach(x -> this.drawCircleAt(x.getPoint(), 6.0));
        });
    }

    public void exportToSVG() {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document doc = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
        SVGGraphics2D canvas = new SVGGraphics2D(doc);
        this.canvas = canvas;

        this.transform = new CoordTransform(this.graph);
        this.nodemap = new HashMap<>(this.graph.getNodes().size());
        this.graph.getNodes().forEach(x -> nodemap.put(x.getId(), x));

        this.drawEdges();
        this.drawLines();
        this.drawLineEnds();
        this.drawNodes();
        this.drawNodeNames();

        Path path = Paths.get(this.name + ".svg");
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(path), "UTF-8")) {
            canvas.stream(w, true);
            System.out.println("Wrote SVG file " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

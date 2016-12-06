package Network.IO;

import Network.Graph;
import Network.Line;
import Network.Node;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.*;
import java.awt.geom.*;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
        this.graph.getNodes().forEach(node -> { //78.5 is the area of a circle with radius 5
            /*
            double radius = Math.sqrt((78.5 + Math.pow(node.getNeighbours().size()*2,2)) / Math.PI);
            AffineTransform circlecoord = new AffineTransform(1.0, 0.0, 0.0, 1.0, -radius / 2.0, -radius / 2.0);
            */
            this.drawCircleAt(node.getPoint(), radius);
        });
    }

    private void drawNodeNames() {
        this.canvas.setPaint(Color.BLACK);
        this.graph.getNodes().forEach(node -> { //78.5 is the area of a circle with radius 5
            Point2D drawpoint = this.transform.transformPoint(node.getPoint());
            this.canvas.drawString(node.getName(), (float) drawpoint.getX() + 20.0f, (float) drawpoint.getY());
        });
    }

    private void drawCircleAt(Point2D p, double radius) {
        p = this.transform.transformPoint(p);
        this.canvas.fill(new Ellipse2D.Double(p.getX() - radius, p.getY() - radius, radius * 2.0, radius * 2.0));
    }

    private void drawLines() {
        this.canvas.setStroke(new BasicStroke(3.0f));
        this.graph.getLines().forEach(line -> {
            this.canvas.setPaint(line.getColourAWT());
            line.getStops().stream().reduce(null, (x, y) -> {
                if (x != null) {
                    this.canvas.draw(new Line2D.Double(
                            this.transform.transformPoint(x.getPoint()),
                            this.transform.transformPoint(y.getPoint())
                    ));
                }
                return y;
            });
        });
    }

    private void drawLineEnds() {
/*
        double radius = 6.0;
        this.graph.getNodes().stream().filter(x -> !x.getEndLines().isEmpty()).forEach(node -> {
            java.util.List<Line> endlines = node.getEndLines();
            Point2D point = this.transform.transformPoint(node.getPoint());
            double arc = 360.0 / endlines.size();
            for (int i = 0; i < endlines.size(); i++) {
                this.canvas.setPaint(endlines.get(i).getColourAWT());
//                this.canvas.fill(new Arc2D.Double(point.getX() - radius, point.getY() - radius, radius * 2.0, radius * 2.0, 100, 270, Arc2D.PIE));
                this.canvas.fill(new Arc2D.Double(point.getX() - radius, point.getY() - radius,
                        radius * 2.0, radius * 2.0,
                        i * arc, (i + 1) * arc, Arc2D.PIE));
                System.out.println("FROM " + (i * arc) + " TO " + (i + 1) * arc + " WITH " + endlines.get(i).getColourAWT());
            }
        });
*/

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
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), "UTF-8")) {
            canvas.stream(w, true);
            System.out.println("Wrote SVG file " + path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

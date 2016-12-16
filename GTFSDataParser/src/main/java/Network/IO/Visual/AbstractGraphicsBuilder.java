package Network.IO.Visual;

import Network.Graph;
import Network.IO.CoordTransform;
import Network.Edge;
import Network.Line;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by micha on 08.12.2016.
 */
public abstract class AbstractGraphicsBuilder {
    protected Graph graph;
    protected String name;
    protected CoordTransform transform;
    protected Graphics2D canvas;
    protected Consumer<String> layerprehook;
    protected Consumer<String> layerposthook;

    public AbstractGraphicsBuilder(Graph graph) {
        this.graph = graph;
    }

    protected void drawEdges() {
        this.canvas.setPaint(Color.BLUE);
        this.canvas.setStroke(new BasicStroke(1.5f));
        this.graph.getEdges().forEach(edge ->
                this.canvas.draw(new Line2D.Double(
                        this.transform.transformPoint(edge.getAnode().getPoint()),
                        this.transform.transformPoint(edge.getBnode().getPoint())
                ))
        );
    }

    protected void drawNodes() {
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

    protected void drawNodeNames() {
        this.canvas.setPaint(Color.BLACK);
        this.graph.getNodes().forEach(node -> { //78.5 is the area of a circle with radius 5
            Point2D drawpoint = this.transform.transformPoint(node.getPoint());
            this.canvas.drawString(node.getName(), (float) drawpoint.getX() + 20.0f, (float) drawpoint.getY());
        });
    }

    protected void drawCircleAt(Point2D p, double radius) {
        p = this.transform.transformPoint(p);
        this.canvas.fill(new Ellipse2D.Double(p.getX() - radius, p.getY() - radius, radius * 2.0, radius * 2.0));
    }

    protected void drawLines() {
        this.canvas.setStroke(new BasicStroke(3.0f));
        Map<Edge, Set<Line>> edgemap = new HashMap<>();
        this.graph.getEdges().forEach(x -> edgemap.put(x, new HashSet<>()));
        this.graph.getLines().forEach(line -> {
            line.getStops().stream().reduce(null, (x, y) -> {
                if (x != null) {
                    edgemap.get(new Edge(x, y)).add(line);
                }
                return y;
            });
        });

        float width = 3.0f;

        edgemap.forEach((edge, lines) -> {
            java.util.List<Line> linelist = new ArrayList<>(lines);
            float[] pattern = new float[]{10.0f, 10.0f * (lines.size() - 1)};
            for (int i = 0; i < lines.size(); i++) {
                this.canvas.setStroke(new BasicStroke(width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, pattern, 10.0f * i));
                this.canvas.setPaint(linelist.get(i).getColourAWT());
                this.canvas.draw(new Line2D.Double(
                        this.transform.transformPoint(edge.getAnode().getPoint()),
                        this.transform.transformPoint(edge.getBnode().getPoint())
                ));
            }
        });

        /*
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
        */
    }

    protected void drawLineEnds() {

        double radius = 6.0;
        this.graph.getNodes().stream().filter(x -> !x.getEndLines().isEmpty()).forEach(node -> {
            java.util.List<Line> endlines = node.getEndLines();
            Point2D point = this.transform.transformPoint(node.getPoint());
            double arc = 360.0 / endlines.size();

            //this.canvas.fill(new Arc2D.Double(300, 300, radius * 2.0, radius * 2.0, 240, arc, Arc2D.PIE));
            this.canvas.setPaint(Color.BLACK);
            this.drawCircleAt(node.getPoint(), radius * 1.2);

            for (int i = 0; i < endlines.size(); i++) {
                this.canvas.setPaint(endlines.get(i).getColourAWT());
                this.canvas.fill(new Arc2D.Double(point.getX() - radius, point.getY() - radius,
                        radius * 2.0, radius * 2.0,
                        i * arc + 90.0, arc, Arc2D.PIE));
            }
        });

/*
        this.graph.getLines().forEach(line -> {
            this.canvas.setPaint(line.getColourAWT());
            line.getStartAndEnd().forEach(x -> this.drawCircleAt(x.getPoint(), 6.0));
        });
*/
    }

    public void drawStats() {
        IntSummaryStatistics edgestats = graph.getEdgeStats();
        List<String> lines = Arrays.asList(
                "Name: " + this.name,
                "Nodes: " + edgestats.getCount(),
                "Edges: " + edgestats.getSum() / 2,
                "LineLength: " + graph.getLineStats().toString().replace("IntSummaryStatistics", "").replace("average", "avg"),
                "LineComps: " + graph.getLineComponents().stream().mapToInt(Set::size).summaryStatistics().toString().replace("IntSummaryStatistics", "").replace("average", "avg"));
        this.canvas.setPaint(Color.BLACK);
        this.canvas.setFont(new Font("Verdana", Font.BOLD, 16));
        for (int i = 0; i < lines.size(); i++)
            this.canvas.drawString(lines.get(i), 10.0f, 20.0f + 20.0f * i);
    }

    public void drawAllLayers() {
        this.transform = new CoordTransform(this.graph);

        Map<String, Runnable> layers = new LinkedHashMap<>();
        layers.put("edges", this::drawEdges);
        layers.put("lines", this::drawLines);
        layers.put("lineends", this::drawLineEnds);
        layers.put("nodes", this::drawNodes);
        //layers.put("names", this::drawNodeNames);
        layers.put("stats", this::drawStats);

        layers.forEach((x, y) -> {
            this.layerprehook.accept(x);
            y.run();
            this.layerposthook.accept(x);
        });

    }

    public abstract void export();
}

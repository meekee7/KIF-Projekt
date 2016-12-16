package Network.IO;

import Network.Graph;
import Network.Node;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.DoubleSummaryStatistics;

/**
 * Created by micha on 30.11.2016.
 */
public class CoordTransform {
    private AffineTransform matrix;

    public CoordTransform(Graph graph) {
        DoubleSummaryStatistics westeast = graph.getNodes().stream().mapToDouble(Node::getLat).summaryStatistics();
        double west = westeast.getMin();
        double east = westeast.getMax();
        DoubleSummaryStatistics northsouth = graph.getNodes().stream().mapToDouble(Node::getLon).summaryStatistics();
        double north = northsouth.getMax();
        double south = northsouth.getMin();

        //System.out.println("North: " + north + " South: " + south + " East: " + east + " West: " + west);

        double width = 800.0;
        double height = 800.0;

        double xscale = width / (Math.abs(east - west));
        double yscale = height / (Math.abs(north - south));

        double xtrans = -west;
        double ytrans = -south;

        double angle = Math.PI * -0.5;

        AffineTransform subtractmat = new AffineTransform(1.0, 0.0, 0.0, 1.0, xtrans, ytrans);
        AffineTransform scalemat = new AffineTransform(xscale, 0.0, 0.0, yscale, 0.0, 0.0);
        AffineTransform bordertransmat = new AffineTransform(1.0, 0.0, 0.0, 1.0, 50.0, 120.0);
        AffineTransform pluscentermat = new AffineTransform(1.0, 0.0, 0.0, 1.0, width * 0.5, height * 0.5);
        AffineTransform minuscentermat = new AffineTransform(1.0, 0.0, 0.0, 1.0, width * -0.5, height * -0.5);
        AffineTransform rotmat = new AffineTransform(Math.cos(angle), Math.sin(angle), -Math.sin(angle), Math.cos(angle), 0.0, 0.0);


        this.matrix = new AffineTransform();
        this.matrix.setToIdentity();
        this.matrix.concatenate(bordertransmat);
        this.matrix.concatenate(pluscentermat);
        this.matrix.concatenate(rotmat);
        this.matrix.concatenate(minuscentermat);
        this.matrix.concatenate(scalemat);
        this.matrix.concatenate(subtractmat);

        //System.out.println("Affine Matrix: " + this.matrix);
    }

    public AffineTransform getMatrix() {
        return matrix;
    }

    public Point2D transformPoint(Point2D point) {
        return this.matrix.transform(point, new Point2D.Double());
    }
}

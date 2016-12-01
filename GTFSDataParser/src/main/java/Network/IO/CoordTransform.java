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

        System.out.println("North: " + north + " South: " + south + " East: " + east + " West: " + west);

        double xscale = 800.0 / (Math.abs(east - west));
        double yscale = 800.0 / (Math.abs(north - south));

        double xtrans = -west;
        double ytrans = -south;

        double angle = Math.PI * 0.5;

        AffineTransform subtractmat = new AffineTransform(1.0, 0.0, 0.0, 1.0, xtrans, ytrans);
        AffineTransform rotmat = new AffineTransform(Math.cos(angle), Math.sin(angle), -Math.sin(angle), Math.cos(angle), 0.0,0.0);
        AffineTransform flipmat = new AffineTransform(-1.0, 0.0, 0.0, 1.0, 0.0,0.0);
        AffineTransform scalemat = new AffineTransform(xscale, 0.0, 0.0, yscale, 0.0, 0.0);
        AffineTransform bordertransmat = new AffineTransform(1.0, 0.0, 0.0, 1.0, 50.0, 50.0);

        this.matrix = new AffineTransform();
        this.matrix.setToIdentity();
        this.matrix.concatenate(bordertransmat);
        //this.matrix.concatenate(flipmat);
        //this.matrix.concatenate(rotmat);
        this.matrix.concatenate(scalemat);
        this.matrix.concatenate(subtractmat);

        //System.out.println("Affine Matrix: " + this.matrix);
    }

    public Point2D transformPoint(Point2D point) {
        return this.matrix.transform(point, new Point2D.Double());
    }
}

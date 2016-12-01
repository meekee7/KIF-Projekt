package Network.LineMaking;

import Network.Line;
import Network.Node;

import java.util.Comparator;

/**
 * Created by micha on 01.12.2016.
 */
public class NeighbourNode {
    private Line minline;
    private Node node;

    public NeighbourNode(Node node, Node other) {
        this.node = node;
        this.minline = node.getEndLines().stream().
                filter(x -> x.canAdd(other))
                .min(Comparator.comparingInt(x -> x.getStops().size()))
                .orElse(null);
    }

    public Line getMinline() {
        return minline;
    }

    public Node getNode() {
        return node;
    }

    public boolean isValid(){
        return this.minline != null;
    }
}

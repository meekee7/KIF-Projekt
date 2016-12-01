package MaxRouteSwitches;

import Network.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by micha on 22.11.2016.
 */
public class MRSStop extends Node {
    private Set<MRSRoute> MRSRoutes = new HashSet<>();

    public MRSStop(Node node){
        this.name = node.getName();
        this.neighbours = node.getNeighbours();
        this.lat = node.getLat();
        this.lon = node.getLon();
    }

    @Override
    public void absorbNode(Node other) {
        super.absorbNode(other);
        this.MRSRoutes.addAll(((MRSStop) other).MRSRoutes);
    }

    public Set<MRSRoute> getMRSRoutes() {
        return MRSRoutes;
    }
}

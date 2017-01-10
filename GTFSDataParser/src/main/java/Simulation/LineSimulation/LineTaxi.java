package Simulation.LineSimulation;

import Network.Line;
import Network.Node;
import Simulation.Entity.Taxi;
import Simulation.Simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by micha on 01.01.2017.
 */
public class LineTaxi extends Taxi {
    protected Line line;

    public LineTaxi(Simulator sim, int id, int capacity, Line line, List<Node> futurepath) {
        super(sim, id, capacity, futurepath);
        this.line = line;
        this.initLocation();
    }

    public LineTaxi(LineTaxi lineTaxi) {
        super(lineTaxi);
        this.line = lineTaxi.line;
        //TODO copy
    }

    public Line getLine() {
        return line;
    }

    public Node getLineEndStop() {
        return this.futurepath.get(this.futurepath.size() - 1);
    }

    @Override
    public Node fetchNextNode() {
        Node result = super.fetchNextNode();
        if (this.futurepath.isEmpty()) {
            List<Node> path = new ArrayList<>(this.line.getStops());
            if (path.get(0) != result)
                Collections.reverse(path);
            path.remove(0);
            this.setFuturepath(path);
        }
        return result;
    }
}

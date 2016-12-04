package Network.MaxOrigRoute;

import Network.Line;
import Network.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Created by micha on 03.12.2016.
 */
public class OriginalLine extends Line {
    private String name;
    private String agency;
    public OriginalLine(int id, String name, String agency) {
        super(id, new LinkedList<>());
        this.name = name;
        this.agency = agency.replaceAll("-|_","");
    }

    public void addStop(Node stop){
        this.stops.add(stop);
    }

    public String getName() {
        return name;
    }

    public String getAgency() {
        return agency;
    }

    public void shrinkStops() {
        this.stops = this.stops.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
    }
}

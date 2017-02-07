package Opta;

import Network.Graph;
import Network.Node;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sabine on 07.02.2017.
 */
@XStreamAlias("OptaTaxi")
public class OptaTaxi {
    private int capacity;
    private long Id;
    private List<Node> corepath;
    private Graph graph;

    public OptaTaxi() {
    }

    public OptaTaxi(int capacity, long id, Graph graph, List<Node> corepath) {
        this.capacity = capacity;
        this.Id = id;
        this.graph = graph;
        this.corepath = corepath;
    }

    public int getCapacity(){
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public List<Node> getCorepath() {
        return corepath;
    }

    public void setCorepath(List<Node> corepath) {
        this.corepath = corepath;
    }

    public Graph getGraph() {
        return graph;
    }
}

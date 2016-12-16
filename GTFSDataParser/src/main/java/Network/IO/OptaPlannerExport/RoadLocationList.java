package Network.IO.OptaPlannerExport;

import Network.Graph;
import Network.IO.DistanceMatrix;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by micha on 14.12.2016.
 */
@XStreamAlias("RoadLocationList")
public class RoadLocationList {
    protected List<RoadLocation> locationList;

    public RoadLocationList() {
    }

    public List<RoadLocation> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<RoadLocation> locationList) {
        this.locationList = locationList;
    }

    public void parseGraph(Graph graph) {
        this.locationList = new ArrayList<>(graph.getNodes().size());
        DistanceMatrix distmat = new DistanceMatrix(graph);
        distmat.calc();

    }
}

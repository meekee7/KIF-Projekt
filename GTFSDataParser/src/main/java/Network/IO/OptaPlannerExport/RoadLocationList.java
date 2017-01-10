package Network.IO.OptaPlannerExport;

import Network.Graph;
import Network.Node;
import Network.Utils.CoordTransform;
import Network.Utils.DistanceMatrix;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.*;
import java.util.*;

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
        CoordTransform transform = new CoordTransform(graph);
        DistanceMatrix distmat = new DistanceMatrix(graph);
        distmat.calc();
        Map<Node, RoadLocation> map = new HashMap<>();
        graph.getNodes().forEach(x -> {
            RoadLocation rl = new RoadLocation();
            rl.setId(x.getId());
            rl.setLatitude(x.getLat());
            rl.setLongitude(x.getLon());
            rl.setTravelDistanceMap(new LinkedHashMap<>(graph.getNodes().size()));
            map.put(x, rl);
        });
        graph.getNodes().forEach(x ->
                graph.getNodes().stream().filter(y -> y != x).forEach(y ->
                        map.get(x).getTravelDistanceMap().put(map.get(y), distmat.getDistance(x, y))
                ));
        this.setLocationList(new ArrayList<>(map.values()));
    }

    public void exportToXML(String filename) {
        if (!filename.endsWith(".xml"))
            filename += ".xml";
        try {
            Path path = Paths.get(filename);
            Files.createDirectories(path.getParent());
            if (!Files.exists(path))
                Files.createFile(path);

            XStream xs = new XStream();
            xs.alias("RoadLocationList", RoadLocationList.class);
            String text = xs.toXML(this);
            Files.write(path, text.getBytes());

            System.out.println("Wrote file " + filename);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

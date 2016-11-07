import Network.Graph;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by micha on 01.11.2016.
 */
public class Main {
    public static void main(String[] args) {
        GtfsReader reader = new GtfsReader();
        GtfsDaoImpl data = new GtfsDaoImpl();
        reader.setEntityStore(data);

        try {
            reader.setInputLocation(new File("./VBB-Daten/630229.zip"));
            reader.run();
        } catch (IOException e) {
            MyLogger.l.error(e.toString());
        }

        MyLogger.l.info("Start " + ZonedDateTime.now());

        Graph graph = Graph.parseGTFS(data, Arrays.asList("100", "200")
                //data.getAllRoutes().stream().map(Route::getShortName).collect(Collectors.toList())
        );
        //MyLogger.l.info("Nodes: " + graph.getNodes().size());
        //MyLogger.l.info("Stops: " + graph.getNodes().stream().map(Network.Node::getName).collect(Collectors.toList()));
        MyLogger.l.info("End " + ZonedDateTime.now());
        MyLogger.l.info("Edge stats " + graph.getEdgeStats());
    }
}

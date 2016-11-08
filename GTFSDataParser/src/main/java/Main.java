import Network.Graph;
import Network.GraphIO;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.serialization.GtfsReader;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

        Graph graph = Graph.parseGTFS(data, //Arrays.asList("100", "200")
                data.getAllRoutes().stream().map(Route::getShortName).collect(Collectors.toList())
        );
        //MyLogger.l.info("Nodes: " + graph.getNodes().size());
        //MyLogger.l.info("Stops: " + graph.getNodes().stream().map(Network.Node::getName).collect(Collectors.toList()));
        MyLogger.l.info("End " + ZonedDateTime.now());
        MyLogger.l.info("Edge stats " + graph.getEdgeStats());
        MyLogger.l.info("Largest nodes: ");
        graph.getNodes().stream().sorted((x, y) -> y.getNeighbours().size() - x.getNeighbours().size()).limit(20).forEach(System.err::println);


        /*
        try {
            Path path = Paths.get("TestOutput.xml");
            if (!Files.exists(path))
                Files.createFile(path);
            GraphIO.getMarshaller().marshal(graph, Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));
        } catch (JAXBException | IOException e) {
            MyLogger.l.error(e.toString());
        }
        */
    }
}

import Network.Graph;
import Network.GraphIO;
import Network.Visualize.SVGBuilder;
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
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by micha on 01.11.2016.
 */
public class Main {
    public static void main(String[] args) {
/*
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

        List<String> testnames =Arrays.asList("U4", "U2");
        //System.out.println("Start " + entry.getKey());
        Graph graph = Graph.parseGTFS(data, "GraphicsTest", x -> testnames.contains(x.getShortName())
        );
*/
        Graph graph = GraphIO.read("VBB-Daten/Potsdam.xml");
        //MyLogger.l.info("Nodes: " + graph.getNodes().size());
        //MyLogger.l.info("Stops: " + graph.getNodes().stream().map(Network.Node::getName).collect(Collectors.toList()));
        //MyLogger.l.info("End " + ZonedDateTime.now());
        MyLogger.l.info("Edge stats " + graph.getEdgeStats());
        //graph.collapseChains();
        //MyLogger.l.info("Largest nodes: ");
        //graph.getNodes().stream().sorted((x, y) -> y.getNeighbours().size() - x.getNeighbours().size()).limit(20).forEach(System.err::println);
        //GraphIO.write(graph, "GraphicsTest");
        //System.out.println("Finished " + "GraphicsTest");

        new SVGBuilder(graph,"PotsdamSVG").exportToSVG();
    }
}

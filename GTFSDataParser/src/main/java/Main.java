import Network.Graph;
import Network.IO.GraphIO;
import Network.IO.OptaPlannerExport.LocationList;
import Network.IO.SVGBuilder;
import Network.IO.StatJSON;
import Network.Node;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by micha on 01.11.2016.
 */
public class Main {
    public static void buildAllGraphs() {
        GtfsReader reader = new GtfsReader();
        GtfsDaoImpl data = new GtfsDaoImpl();
        reader.setEntityStore(data);

        try {
            reader.setInputLocation(new File("./VBB-Daten/630229.zip"));
            reader.run();
        } catch (IOException e) {
            MyLogger.l.error(e.toString());
        }

        Map<String, Predicate<Route>> cityfilters = new HashMap<>();
        //This could be done with reflection
        cityfilters.put("VBB", CityFilter::VBB);
        cityfilters.put("BerlinStreet", CityFilter::BerlinStreet);
        cityfilters.put("BerlinFull", CityFilter::BerlinFull);
        cityfilters.put("Brandenburg", CityFilter::Brandenburg);
        cityfilters.put("Cottbus", CityFilter::Cottbus);
        cityfilters.put("Frankfurt", CityFilter::Frankfurt);
        cityfilters.put("Potsdam", CityFilter::Potsdam);
        cityfilters.put("SmallTest", x -> Arrays.asList("U2", "U4").contains(x.getShortName()));

        List<Graph> graphs = new ArrayList<>(cityfilters.size());
        cityfilters.entrySet().forEach(entry -> {
            System.out.println("-- START " + entry.getKey() + " --");
            String name = entry.getKey();
            Graph graph = new Graph();
            graph.parseGTFS(data, name, entry.getValue());
            System.out.println("Edge stats " + graph.getEdgeStats());
            graph.buildLines();
            GraphIO.write(graph, "VBB-Daten/" + name);
            new SVGBuilder(graph, "GraphViewer/data/" + name + "SVG").exportToSVG();
            new LocationList(graph).exportToXML("VBB-Daten/OptaPlanner/Air/OptaAir" + graph.getName());
            graphs.add(graph);
            System.out.println("-- END " + entry.getKey() + " --");
        });
        try {
            Files.write(Paths.get("GraphViewer/data/Stats.js"), StatJSON.buildStatsJS(graphs.stream()
                    .sorted((x, y) -> y.getNodes().size() - x.getNodes().size()).collect(Collectors.toList())).getBytes());
            System.out.println("Wrote file GraphViewer/data/Stats.js");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
*/
/*
        Map<String, Integer> map = new HashMap<>(data.getAllStops().size());
        data.getAllStops().forEach(x -> {
            if (map.containsKey(x.getName()))
                map.put(x.getName(), map.get(x.getName()) + 1);
            else
                map.put(x.getName(), 1);
        });
        map.entrySet().stream().filter(x -> x.getValue() > 1).forEach(System.out::println);
*/
/*
        MyLogger.l.info("Start " + ZonedDateTime.now());

        List<String> testnames =Arrays.asList("U4", "U2");
        //System.out.println("Start " + entry.getKey());
        Graph graph = Graph.parseGTFS(data, "GraphicsTest", x -> testnames.contains(x.getShortName())
        );
*/

        //Main.buildAllGraphs();


        Graph graph = GraphIO.read("VBB-Daten/BerlinStreet.xml");
        Node start = graph.getNodes().stream().filter(x->x.getName().equals("Ahrensfelde/Stadtgrenze (Berlin)")).findFirst().get();
        Node end = graph.getNodes().stream().filter(x->x.getName().equals("Landsberger Allee/Blumberger Damm (Berlin)")).findFirst().get();
        System.out.println(graph.getShortestPathWeighted(start,end).stream().map(Node::getName).collect(Collectors.toList()));
/*        graph.buildLines();
        System.out.println(graph.getLines().stream().mapToInt(x -> x.getStops().size()).summaryStatistics());
        graph.getLines().forEach(x -> System.out.println(x.getStops()));
        new SVGBuilder(graph, "PotsdamWithLines").exportToSVG();
*/

        //MyLogger.l.info("Nodes: " + graph.getNodes().size());
        //MyLogger.l.info("Stops: " + graph.getNodes().stream().map(Network.Node::getName).collect(Collectors.toList()));
        //MyLogger.l.info("End " + ZonedDateTime.now());
        //int prev = graph.getNodes().size();
        //int curr = Integer.MAX_VALUE;
        //MyLogger.l.info("Edge stats " + graph.getEdgeStats());


        //Main.buildAllGraphs();


        //MyLogger.l.info("Largest nodes: ");
        //graph.getNodes().stream().sorted((x, y) -> y.getNeighbours().size() - x.getNeighbours().size()).limit(20).forEach(System.err::println);
        //GraphIO.write(graph, "GraphicsTest");
        //System.out.println("Finished " + "GraphicsTest");

        //new SVGBuilder(graph,"BerlinTestSVG").exportToSVG();
    }
}

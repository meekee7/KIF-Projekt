import Network.Graph;
import Network.IDFactory;
import Network.IO.DistanceMatrix;
import Network.IO.GraphIO;
import Network.IO.OptaPlannerExport.AirLocationList;
import Network.IO.Visual.SVGBuilder;
import Network.IO.StatJSON;
import Network.MaxOrigRoute.OrigGraph;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by micha on 01.11.2016.
 */
public class Main {
    public static void buildAllGraphs() {
        Instant start = Instant.now();
        GtfsReader reader = new GtfsReader();
        GtfsDaoImpl data = new GtfsDaoImpl();
        reader.setEntityStore(data);

        try {
            reader.setInputLocation(new File("./VBB-Daten/GTFS_VBB_Dez2016_Aug2017_mit_shapes-files.zip"));
            //reader.setInputLocation(new File("./VBB-Daten/630229.zip"));
            reader.run();
        } catch (IOException e) {
            MyLogger.l.error(e.toString());
        }

        Collection<Set<Stop>> transferclusters = Graph.buildTransferClusters(data);
        //System.out.println(data.getAllRoutes().stream().map(Route::getType).distinct().sorted().collect(Collectors.toList()));

        Map<String, Predicate<Route>> cityfilters = new LinkedHashMap<>();
        //This could be done with reflection

        cityfilters.put("VBB", CityFilter::VBB);
        cityfilters.put("BerlinStreet", CityFilter::BerlinStreet);
        cityfilters.put("BerlinFull", CityFilter::BerlinFull);
        cityfilters.put("Brandenburg", CityFilter::Brandenburg);
        cityfilters.put("Cottbus", CityFilter::Cottbus);
        cityfilters.put("Frankfurt", CityFilter::Frankfurt);
        cityfilters.put("Potsdam", CityFilter::Potsdam);
        cityfilters.put("SmallTest", x -> Arrays.asList("U2", "U4").contains(x.getShortName()));

        boolean longestshortestpath = false;

        IDFactory ids = new IDFactory();
        List<StatJSON> statJSONs = new ArrayList<>(cityfilters.size());
        cityfilters.forEach((name, predicate) -> {
            System.out.println("-- START " + name + " --");

            Graph graph = new Graph();
            graph.parseGTFS(data, name, predicate, transferclusters);
            System.out.println("Edge stats " + graph.getEdgeStats());
            graph.buildLines();
            GraphIO.write(graph, "VBB-Daten/" + name);
            new SVGBuilder(graph, "GraphViewer/data/" + name + "SVG").export();
            new AirLocationList(graph).exportToXML("VBB-Daten/OptaPlanner/Air/OptaAir" + graph.getName());


            StatJSON stats = new StatJSON();
            stats.add(graph);

            stats.add("link", "GraphViewer/data/" + name + "SVG.svg");
            stats.add("frame", ids.createID());
            if (longestshortestpath) {
                IntSummaryStatistics switchroutestats = graph.getSwitchRouteStats();
                System.out.println("Switch route stats: " + switchroutestats);
                stats.add("SwitchRouteStats", switchroutestats);

                Graph origlinegraph = new OrigGraph();
                origlinegraph.parseGTFS(data, name, predicate, transferclusters);
                IntSummaryStatistics origswitchroutestats = origlinegraph.getSwitchRouteStats();
                stats.add("OrigLines", origlinegraph.getLineStats());
                stats.add("OrigSwitchRouteStats", origswitchroutestats);
                System.out.println("OrigLineGraph SwitchRouteStats: " + origswitchroutestats);
            }

            statJSONs.add(stats);
            System.out.println("-- END " + name + " --");
        });
        try {
            Files.write(Paths.get("GraphViewer/data/Stats.js"), StatJSON.buildStatsJS(statJSONs).getBytes());
            System.out.println("Wrote file GraphViewer/data/Stats.js");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Instant end = Instant.now();
        System.out.println("PROCESSING TIME: " + Duration.between(start, end));
    }

    public static void readAllGraphs() {
        Map<String, Predicate<Route>> cityfilters = new LinkedHashMap<>();
        //This could be done with reflection

        cityfilters.put("VBB", CityFilter::VBB);
        cityfilters.put("BerlinStreet", CityFilter::BerlinStreet);
        cityfilters.put("BerlinFull", CityFilter::BerlinFull);
        cityfilters.put("Brandenburg", CityFilter::Brandenburg);
        cityfilters.put("Cottbus", CityFilter::Cottbus);
        cityfilters.put("Frankfurt", CityFilter::Frankfurt);
        cityfilters.put("Potsdam", CityFilter::Potsdam);
        cityfilters.put("SmallTest", x -> Arrays.asList("U2", "U4").contains(x.getShortName()));

        cityfilters.forEach((name, predicate) -> {
            System.out.println("Parsing " + name);
            Graph graph = GraphIO.read("VBB-Daten/" + name + ".xml");

            graph.buildLinesEdgeComplete();
            //System.out.println(graph.getSwitchRouteStats());
            //new DistanceMatrix(graph).calc();
            //graph.buildLines();
            new SVGBuilder(graph, "GraphViewer/data/" + name + "SVG").export();
        });
    }

    public static void main(String[] args) {
        //Main.buildAllGraphs();
        Main.readAllGraphs();

        //Graph graph = GraphIO.read("VBB-Daten/Cottbus.xml");
        //graph.buildLines();

//        graph.buildLines();
//        System.out.println(graph.getLines().stream().mapToInt(x -> x.getStops().size()).summaryStatistics());
//        graph.getLines().forEach(x -> System.out.println(x.getStops()));
//        new SVGBuilder(graph, "GraphViewer/data/BrandenburgSVG").exportToSVG();
    }
}

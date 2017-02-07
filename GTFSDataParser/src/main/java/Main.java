import Network.Graph;
import Network.IDFactory;
import Network.IO.GraphIO;
import Network.IO.OptaPlannerExport.AirLocationList;
import Network.IO.StatJSON;
import Network.IO.Visual.SVGBuilder;
import Network.MaxOrigRoute.OrigGraph;
import Network.Node;
import Simulation.LineSimulation.LineSimulator;
import Simulation.PlannedSimulation.PlannedSimulator;
import Simulation.SimulationConfig;
import Simulation.Simulator;
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

        boolean longestshortestpath = true;

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
        Instant start = Instant.now();

        Map<String, Predicate<Route>> cityfilters = new LinkedHashMap<>();
        //This could be done with reflection

//        cityfilters.put("VBB", CityFilter::VBB);
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

            //graph.buildLines();
            //graph.buildLinesEdgeComplete();
            //System.out.println("SwitchRouteStats: " +graph.getSwitchRouteStats());
            //System.out.println("SwitchRouteStats: " +graph.getSwitchRouteStatsAlt());
            //RoadLocationList rl = new RoadLocationList();
            //rl.parseGraph(graph);
            //rl.exportToXML("VBB-Daten/OptaPlanner/Road/OptaRoad" + name);
            List<Node> nodes = new ArrayList<>(graph.getNodes());
            Collections.shuffle(nodes);
            System.out.println(graph.getJourney(nodes.get(0), nodes.get(1), 0.0));
            //new DistanceMatrix(graph).calc();
            //graph.buildLines();
            //new SVGBuilder(graph, "GraphViewer/data/" + name + "SVG").export();
        });
        Instant end = Instant.now();
        System.out.println("PROCESSING TIME: " + Duration.between(start, end));
    }

    public static void main(String[] args) {
        //Main.buildAllGraphs();
        //System.exit(0);
        //Main.readAllGraphs();
        Instant start = Instant.now();
        Map<String, Predicate<Route>> cityfilters = new LinkedHashMap<>();
        //This could be done with reflection

        //cityfilters.put("VBB", CityFilter::VBB);
//        cityfilters.put("SmallTest", x -> Arrays.asList("U2", "U4").contains(x.getShortName()));
//        cityfilters.put("Potsdam", CityFilter::Potsdam);
//        cityfilters.put("Frankfurt", CityFilter::Frankfurt);
//        cityfilters.put("Cottbus", CityFilter::Cottbus);
        cityfilters.put("Brandenburg", CityFilter::Brandenburg);
        //cityfilters.put("BerlinStreet", CityFilter::BerlinStreet);
        //cityfilters.put("BerlinFull", CityFilter::BerlinFull);
        cityfilters.forEach((x, y) -> {
            Graph graph = GraphIO.read("VBB-Daten/" + x + ".xml");
            graph.buildPathCache();
            //PlannedSimulator sim = new PlannedSimulator(graph);
            SimulationConfig cfg = new SimulationConfig.Builder()
                    .capacity(8)
                    .spawnfrequency(1)
                    .spawnshare(0.1)
                    .speed(1000.0)

                    .turns(1000)

                    .linefrequency(graph.createEqualDistribution(4))

                    .taxirate(x.equals("SmallTest") ? 0.5 : 0.8)
                    .maxcalctime(10L)
                    .calcstep(1)
                    .clearing(200000)

                    .assemble();
            Simulator sim;
            if (false) {
                Map<Integer, Integer> freqdist = LineSimulator.findBestDistribution(graph, cfg);
                System.out.println(freqdist.values());
                cfg = new SimulationConfig.Builder(cfg).linefrequency(freqdist).assemble();
                sim = new LineSimulator(graph, cfg);
            } else
                sim = new PlannedSimulator(graph, cfg);

            sim.simulate();
            System.out.println("---STATS---");
            System.out.println(sim.getStats());
            sim.writeStatsToFile("./SimulationData/" + (sim instanceof LineSimulator ? "Line" : "Planned"));
            //sim.denialmap.forEach((a, b) -> System.out.println(a.getId() + " " + b));
            System.out.println("---------------------");
        });
        Instant end = Instant.now();

        System.out.println("PROCESSING TIME: " + Duration.between(start, end));
        //graph.buildLines();

//        graph.buildLines();
//        System.out.println(graph.getLines().stream().mapToInt(x -> x.getStops().size()).summaryStatistics());
//        graph.getLines().forEach(x -> System.out.println(x.getStops()));
//        new SVGBuilder(graph, "GraphViewer/data/BrandenburgSVG").exportToSVG();

        /*

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

        Map<Trip, Collection<StopTime>> tripmap = new HashMap<>();
        data.getAllTrips().forEach(x -> tripmap.put(x, new HashSet<>()));
        data.getAllStopTimes().forEach(x -> tripmap.get(x.getTrip()).add(x));

        System.out.println("Tripmap built");

        tripmap.forEach((trip, stopTimes) -> {
            Set<Stop> stopset = new HashSet<>();
            LinkedList<Stop> stoplist = stopTimes.stream().sorted(Comparator.comparingInt(StopTime::getStopSequence))
                    .map(StopTime::getStop)
                    .collect(Collectors.toCollection(LinkedList::new));
            if (stoplist.getFirst() != stoplist.getLast())
                stoplist.forEach(x -> {
                    if (stopset.contains(x))
                        System.out.println("Route " + trip.getRoute().getShortName() + " Trip " + trip.getId() + " Stop " + x.getName());
                    else
                        stopset.add(x);
                });
        });
        tripmap.forEach((x, y) -> {
            if (x.getId().toString().equals("716_56256346"))
                y.stream().sorted(Comparator.comparingInt(StopTime::getStopSequence))
                    .map(StopTime::getStop)
                    .map(Stop::getName)
                    .forEach(System.out::println);
        });

        */
    }
}

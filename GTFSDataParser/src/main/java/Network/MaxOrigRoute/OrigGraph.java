package Network.MaxOrigRoute;

import Network.Graph;
import Network.IDFactory;
import Network.Line;
import Network.LineMaking.UnitableLines;
import Network.Node;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by micha on 03.12.2016.
 */
public class OrigGraph extends Graph {
    private Collection<OriginalLine> originallines = new HashSet<>();

    @Override
    public IntSummaryStatistics getSwitchRouteStats() {
        Set<UnitableLines> pairs = new HashSet<>(this.originallines.size() * this.originallines.size() / 2);
        this.originallines.forEach(x -> this.originallines.forEach(y -> pairs.add(new UnitableLines(x, y, null))));
        AtomicInteger counter = new AtomicInteger(0);
        List<List<Line>> routes = pairs.parallelStream()
                .filter(x -> x.getA() != x.getB())
                .map(x -> {
                    if (counter.incrementAndGet() % 100000 == 0)
                        System.out.println(counter.intValue() + " out of " + (pairs.size() - this.originallines.size()));
                    return this.getSwitchRoute(x.getA(), x.getB());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        System.out.println("MAX ROUTE: ");
        List<Line> maxroute = routes.parallelStream().max(Comparator.comparingInt(List::size)).get();
        System.out.println(maxroute.stream().map(x -> (OriginalLine) x)
                .map(x -> x.getName() + " | " + x.getAgency())
                .collect(Collectors.toList()));

        return routes.parallelStream()
                .mapToInt(List::size)
                .summaryStatistics();
        //.max(Comparator.comparingInt(List::size))
        //.get();
    }

    @Override
    public void parseGTFS(GtfsDaoImpl data, String name, Predicate<Route> routepredicate) {
        this.name = name;

        System.out.println("Graph building start");

        Collection<Route> routes = data.getAllRoutes().stream()
                .filter(routepredicate)
                .collect(Collectors.toList());

        System.out.println("Routes included: " + routes.size());
        this.routesIncluded = routes.stream()
                .map(Route::getShortName)
                .collect(Collectors.toList());

        Collection<Trip> trips = data.getAllTrips().stream()
                .filter(x -> routes.contains(x.getRoute()))
                .collect(Collectors.toSet());

        System.out.println("Trips collected: " + trips.size());

        Collection<StopTime> stoptimes = data.getAllStopTimes().stream()
                .filter(x -> trips.contains(x.getTrip()))
                .collect(Collectors.toList());

        Collection<Stop> stops = stoptimes.stream()
                .map(StopTime::getStop)
                .distinct() //Removes duplicate elements
                .collect(Collectors.toList());

        System.out.println("Stops collected: " + stops.size());

        Map<Stop, Node> stopmap = new HashMap<>(stops.size());
        IDFactory nodeIDs = new IDFactory();
        stops.forEach(x -> stopmap.put(x,
                new Node(nodeIDs.createID(), x.getName(), x.getLat(), x.getLon())));

        System.out.println("Stopmap built");

        Map<Trip, Collection<StopTime>> tripmap = new HashMap<>();
        trips.forEach(x -> tripmap.put(x, new HashSet<>()));
        stoptimes.forEach(x -> tripmap.get(x.getTrip()).add(x));

        System.out.println("Tripmap built");

        this.getNodes().addAll(stopmap.values());

        Collection<Transfer> alltransfers = data.getAllTransfers().stream()
                .filter(x -> x.getFromStop() != x.getToStop()) //Skip self-transfers
                .collect(Collectors.toList());
        Collection<Set<Stop>> transferclusters = new ArrayList<>(alltransfers.size());
        alltransfers.forEach(transfer -> {
            Optional<Set<Stop>> cluster = transferclusters.stream()
                    .filter(x ->
                            x.contains(transfer.getFromStop()) || x.contains(transfer.getToStop()))
                    .findFirst();
            if (cluster.isPresent()) {
                cluster.get().add(transfer.getFromStop());
                cluster.get().add(transfer.getToStop());
            } else {
                Set<Stop> newcluster = new HashSet<>();
                newcluster.add(transfer.getFromStop());
                newcluster.add(transfer.getToStop());
                transferclusters.add(newcluster);
            }
        });

        System.out.println("Transfer clusters built: " + transferclusters.size());

        /*
        transferclusters.stream().map(x ->
                x.stream()
                        .min((y, z) -> y.getName().length() - z.getName().length())
        ).map(x->x.get().getName()).forEach(System.out::println);
        */

        Map<Node, Node> nodepointer = new HashMap<>(this.nodes.size());
        this.nodes.forEach(x -> nodepointer.put(x, x));

        transferclusters.stream()
                .map(x -> x.stream()
                        .filter(stops::contains)
                        .map(stopmap::get)
                        .collect(Collectors.toList())
                )
                .filter(x -> !x.isEmpty()).forEach(cluster -> {
            Node min = cluster.stream()
                    .min(Comparator.comparingInt(x -> x.getName().length())).get();
            cluster.remove(min);
            cluster.forEach(x -> nodepointer.replace(x, min));
        });


        IDFactory origIDs = new IDFactory();
        Map<Route, OriginalLine> origlines = new HashMap<>(routes.size());
        routes.forEach(x -> origlines.put(x, new OriginalLine(origIDs.createID(), x.getShortName(), x.getAgency().getId())));
        tripmap.forEach((x, y) -> origlines.get(x.getRoute()).getStops().addAll(y.stream()
                .map(z -> stopmap.get(z.getStop())).map(nodepointer::get).collect(Collectors.toList())));

        this.originallines = new ArrayList<>(origlines.values());
        this.originallines.forEach(OriginalLine::shrinkStops);
        this.originallines.forEach(x -> x.getStops().forEach(y -> y.getLines().add(x)));
        this.originallines.forEach(Line::calcNeighbourLines);

    }
}

package MaxRouteSwitches;

import Network.Graph;
import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.model.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by micha on 22.11.2016.
 */
public class MRSGraph {
    private Collection<MRSRoute> MRSRoutes;
    private Collection<MRSStop> MRSStops;
    private Map<String, MRSStop> mrsstopmap;

    public void build(Graph graph, GtfsDaoImpl data, Predicate<Route> routepredicate) {
        graph.getNodes().forEach(x -> {
            //MRSStop stop = new MRSStop(x);
            //mrsstopmap.put(stop.getName(), stop);
            //stop.getInnerNodes().forEach(y -> mrsstopmap.put(y, stop));
        });

        Collection<Route> routes = data.getAllRoutes().stream()
                .filter(routepredicate)
                .collect(Collectors.toList());

        System.out.println("Routes included: " + routes.size());

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

        Map<Stop, MRSStop> stopmap = new HashMap<>(stops.size());
        stops.forEach(x -> stopmap.put(x,
                mrsstopmap.get(x.getName())));
                //new Node(++graph.idcounter, x.getName(), x.getLat(), x.getLon())));

        System.out.println("Stopmap built");
    }

    public List<MRSRoute> findLongestPath() {
        return this.MRSRoutes.stream().map(x -> x.walkThisRoute(new HashSet<>(), new LinkedList<>()))
                .max(Comparator.comparingInt(List::size))
                .orElse(new ArrayList<>());
    }
}

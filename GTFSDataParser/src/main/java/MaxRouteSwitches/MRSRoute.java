package MaxRouteSwitches;

import java.util.*;

/**
 * Created by micha on 22.11.2016.
 */
public class MRSRoute {
    private Set<MRSRoute> neighbours = new HashSet<>();
    private String name;
    private String agency;
    private String id;

    public void gatherNeighbours(Collection<MRSStop> MRSStops) {
        MRSStops.forEach(x -> this.neighbours.addAll(x.getMRSRoutes()));
        this.neighbours.remove(this);
    }

    public List<MRSRoute> walkThisRoute(Set<MRSRoute> walked, List<MRSRoute> prevpath) {
        if (walked.contains(this))
            return prevpath;
        List<MRSRoute> newpath = new LinkedList<>(prevpath);
        newpath.add(this);
        Set<MRSRoute> newwalked = new HashSet<>(walked);
        newwalked.add(this);

        return this.neighbours.stream()
                .filter(x -> !newwalked.contains(x))
                .map(x -> x.walkThisRoute(newwalked, newpath))
                .max(Comparator.comparingInt(List::size))
                .orElse(newpath);
    }
}

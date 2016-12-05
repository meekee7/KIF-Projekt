package Network.IO;

import Network.Graph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by micha on 01.12.2016.
 */
public class StatJSON {
    private Map<String, String> data = new LinkedHashMap<>();

    public StatJSON add(String name, int value) {
        this.data.put(name, Integer.toString(value));
        return this;
    }

    public StatJSON add(String name, String value) {
        this.data.put(name, value);
        return this;
    }

    public StatJSON add(String name, Object value) {
        this.data.put(name, value.toString());
        return this;
    }

    public StatJSON add(Graph graph) {
        IntSummaryStatistics edgestats = graph.getEdgeStats();
        this.add("Name", graph.getName())
                .add("Nodes", edgestats.getCount())
                .add("Edges", edgestats.getSum() / 2)
                .add("MinNeighbs", edgestats.getMin())
                .add("AvgNeighbs", edgestats.getMin())
                .add("MaxNeighbs", edgestats.getMax())
                .add("LineLength", graph.getLineStats())
                ;
                 //put("Name", graph.getName());
        return this;
    }

    public String toJSONString() {
        return "{" +
                String.join(", ", this.data.entrySet().stream()
                        .map(x -> "\"" + x.getKey() + "\":\"" + x.getValue() + "\"")
                        .collect(Collectors.toList()))
                + "}";

        //IntSummaryStatistics edgestats = graph.getEdgeStats();
        //return String.format("{ \"Name\":\"%s\", \"Nodes\":%d, \"Edges\":%d, \"Lines\":%d, \"LineLength\":\"%s\"}",
        //        graph.getName(), edgestats.getCount(), edgestats.getSum() / 2, graph.getLineStats());
    }

    public static String buildStatsJS(List<StatJSON> stats) {
        return "var stats = [" + String.join(", ", stats.stream()
                .map(StatJSON::toJSONString)
                .collect(Collectors.toList()))
                + "];";
    }
}

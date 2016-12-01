package Network.IO;

import Network.Graph;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by micha on 01.12.2016.
 */
public class StatJSON {
    private StatJSON() {
    }

    public static String toJSONString(Graph graph) {
        IntSummaryStatistics edgestats = graph.getEdgeStats();
        return String.format("{ \"Name\":\"%s\", \"Nodes\":%d, \"Edges\":%d, \"Lines\":%d}",
                graph.getName(), edgestats.getCount(), edgestats.getSum() / 2, graph.getLines().size());
    }

    public static String buildStatsJS(List<Graph> graphs) {
        return "var stats = [" + String.join(", ", graphs.stream().map(StatJSON::toJSONString).collect(Collectors.toList())) + "];";
    }
}

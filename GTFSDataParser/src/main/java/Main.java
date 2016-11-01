import org.onebusaway.gtfs.impl.GtfsDaoImpl;
import org.onebusaway.gtfs.serialization.GtfsReader;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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

        Set<String> transits = new HashSet<>();
        data.getAllTransfers().stream()
                .filter(x -> x.getFromStop() != x.getToStop())
                .forEach(x -> {
                    transits.add(x.getFromStop().getName());
                    transits.add(x.getToStop().getName());
                });
        MyLogger.l.info("Transit affected: " + transits.size());
    }
}

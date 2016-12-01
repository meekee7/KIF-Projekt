package Network.IO;

import Network.Graph;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by micha on 06.11.2016.
 */
public class GraphIO {
    private static Marshaller mar = null;
    private static Unmarshaller unmar = null;

    private static void create() {
        try {
            JAXBContext context = JAXBContext.newInstance(Graph.class);
            mar = context.createMarshaller();
            mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            unmar = context.createUnmarshaller();
        } catch (JAXBException e) {
            System.err.println("Could not complete JAXB initialisation.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Graph read(String filename) {
        if (!filename.endsWith(".xml"))
            filename += ".xml";
        try {
            Graph graph = (Graph) GraphIO.getUnmarshaller().unmarshal(new File(filename));
            graph.postIOIntegration();
            System.out.println("Read file " + filename);
            return graph;
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    public static void write(Graph graph, String filename) {
        if (!filename.endsWith(".xml"))
            filename += ".xml";
        try {
            Path path = Paths.get(filename);
            if (!Files.exists(path))
                Files.createFile(path);
            GraphIO.getMarshaller().marshal(graph, Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));
            System.out.println("Wrote file " + filename);
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Marshaller getMarshaller() {
        if (mar == null)
            create();
        return mar;
    }

    private static Unmarshaller getUnmarshaller() {
        if (unmar == null)
            create();
        return unmar;
    }
}

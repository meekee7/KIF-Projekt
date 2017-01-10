package Network.IO.OptaPlannerExport;

import Network.Graph;
import Network.Utils.CoordTransform;
import Network.Node;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by micha on 30.11.2016.
 */
@XmlRootElement(name = "locationList")
public class AirLocationList {
    private List<AirLocation> locations;
    private int globalid = 2;
    private int locid = 0;
    private static Marshaller mar = null;

    public AirLocationList(){
        this.locations = new ArrayList<>();
    }

    public AirLocationList(Graph graph){
        //TODO this is for airdistance, we need roaddistance
        CoordTransform transform = new CoordTransform(graph);
        this.locations = graph.getNodes().stream()
                .map(Node::getPoint)
                .map(transform::transformPoint)
                .map(x-> new AirLocation(x, this.locid++))
                .collect(Collectors.toList());
    }

    @XmlElement(name = "VrpAirLocation")
    public List<AirLocation> getLocations() {
        return locations;
    }

    @XmlAttribute(name = "id")
    public int getGlobalid() {
        return globalid;
    }

    public void exportToXML(String filename) {
        try {
            JAXBContext context = JAXBContext.newInstance(AirLocationList.class);
            mar = context.createMarshaller();
            mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        } catch (JAXBException e) {
            System.err.println("Could not complete JAXB initialisation.");
            e.printStackTrace();
            System.exit(1);
        }
        if (!filename.endsWith(".xml"))
            filename += ".xml";
        try {
            Path path = Paths.get(filename);
            Files.createDirectories(path.getParent());
            if (!Files.exists(path))
                Files.createFile(path);
            mar.marshal(this, Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
            System.out.println("Wrote file " + filename);
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

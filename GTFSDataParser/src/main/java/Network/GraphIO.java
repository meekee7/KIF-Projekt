package Network;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Created by micha on 06.11.2016.
 */
public class GraphIO {
    private static Marshaller mar = null;
    private static Unmarshaller unmar = null;

    private static void create(){
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

    public static Marshaller getMarshaller() {
        if (mar == null)
            create();
        return mar;
    }

    public static Unmarshaller getUnmarshaller() {
        if (unmar == null)
            create();
        return unmar;
    }
}

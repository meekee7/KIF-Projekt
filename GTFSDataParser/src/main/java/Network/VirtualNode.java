package Network;

import javax.xml.bind.annotation.XmlElement;
import java.util.Set;

/**
 * Created by micha on 04.11.2016.
 */
public class VirtualNode extends Node {
    private Set<Node> hiddenNodes;

    public VirtualNode(String name, double lat, double lon) {
        super(name, lat, lon);
    }

    @XmlElement
    public Set<Node> getHiddenNodes() {
        return hiddenNodes;
    }
}

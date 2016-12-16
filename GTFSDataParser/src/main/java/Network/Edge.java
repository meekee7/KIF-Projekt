package Network;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by micha on 08.11.2016.
 */
public class Edge {
    private Node anode;
    private Node bnode;

    public Edge(Node a, Node b) {
        if (a.getId() < b.getId()) {
            this.anode = a;
            this.bnode = b;
        } else {
            this.bnode = a;
            this.anode = b;
        }
    }

    public Node getAnode() {
        return anode;
    }

    public Node getBnode() {
        return bnode;
    }

    /**
     * Automatically generated with IntelliJ IDEA
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Edge edge = (Edge) o;

        if (anode.getId() != edge.anode.getId()) return false;
        return bnode.getId() == edge.bnode.getId();

    }

    /**
     * Automatically generated with IntelliJ IDEA
     *
     * @return
     */
    @Override
    public int hashCode() {
        int result = anode.getId();
        result = 31 * result + bnode.getId();
        return result;
    }
}

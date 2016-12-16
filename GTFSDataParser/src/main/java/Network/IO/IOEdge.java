package Network.IO;

import Network.Edge;
import Network.Node;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by micha on 16.12.2016.
 */
public class IOEdge {
    private int aid;
    private int bid;

    private Node anode;
    private Node bnode;

    public IOEdge() {
        this(-1,-1);
    }

    public IOEdge(int a, int b) {
        this.aid = Math.min(a, b);
        this.bid = Math.max(a, b);
        this.anode = null;
        this.bnode = null;
    }

    @XmlAttribute(name = "a")
    public int getA() {
        return aid;
    }

    @XmlAttribute(name = "b")
    public int getB() {
        return bid;
    }

    public void setA(int a) {
        this.aid = a;
    }

    public void setB(int b) {
        this.bid = b;
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

        IOEdge edge = (IOEdge) o;

        if (aid != edge.aid) return false;
        return bid == edge.bid;

    }

    /**
     * Automatically generated with IntelliJ IDEA
     *
     * @return
     */
    @Override
    public int hashCode() {
        int result = aid;
        result = 31 * result + bid;
        return result;
    }
}

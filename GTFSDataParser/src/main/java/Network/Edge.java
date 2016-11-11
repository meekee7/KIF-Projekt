package Network;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by micha on 08.11.2016.
 */
public class Edge {
    //This edge type exists only for XML-IO
    private int a;
    private int b;

    public Edge(int a, int b) {
        if (a < b) {
            this.a = a;
            this.b = b;
        }
        else {
            this.b = a;
            this.a = b;
        }
    }

    @XmlAttribute
    public int getA() {
        return a;
    }

    @XmlAttribute
    public int getB() {
        return b;
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

        if (a != edge.a) return false;
        return b == edge.b;

    }

    /**
     * Automatically generated with IntelliJ IDEA
     *
     * @return
     */
    @Override
    public int hashCode() {
        int result = a;
        result = 31 * result + b;
        return result;
    }
}

package Network.IO;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by micha on 08.11.2016.
 */
public class Edge {
    //This edge type exists only for XML-IO
    private int a;
    private int b;

    public Edge() {
        this.a = -1;
        this.b = -1;
    }

    public Edge(int a, int b) {
        this.a = Math.min(a, b);
        this.b = Math.max(a, b);
    }

    @XmlAttribute
    public int getA() {
        return a;
    }

    @XmlAttribute
    public int getB() {
        return b;
    }

    public void setA(int a) {
        this.a = a;
    }

    public void setB(int b) {
        this.b = b;
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

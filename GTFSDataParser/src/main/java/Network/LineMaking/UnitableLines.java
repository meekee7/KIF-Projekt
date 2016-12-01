package Network.LineMaking;

import Network.Line;
import Network.Node;

/**
 * Created by micha on 30.11.2016.
 */
public class UnitableLines {
    private Line a;
    private Line b;
    private Node mergepoint;

    public Line getA() {
        return a;
    }

    public Line getB() {
        return b;
    }

    public Node getMergepoint() {
        return mergepoint;
    }

    public UnitableLines(Line a, Line b, Node mergepoint) {
        if (a.getId() < b.getId()) {
            this.a = a;
            this.b = b;
        } else {
            this.a = b;
            this.b = a;
        }
        this.mergepoint = mergepoint;
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

        UnitableLines that = (UnitableLines) o;

        if (a != null ? !a.equals(that.a) : that.a != null) return false;
        return b != null ? b.equals(that.b) : that.b == null;
    }

    /**
     * Automatically generated with IntelliJ IDEA
     *
     * @return
     */
    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        return result;
    }
}

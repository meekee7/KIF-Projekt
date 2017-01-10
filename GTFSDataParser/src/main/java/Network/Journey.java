package Network;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by micha on 06.01.2017.
 */
public class Journey {
    public static class Step{
        private Node start;
        private Node end;
        private Node lineEnd;
        private Line line;

        public Step(Node start, Node end, Line line, Node lineEnd) {
            this.start = start;
            this.end = end;
            this.line = line;
            this.lineEnd = lineEnd;
        }

        public Node getStart() {
            return start;
        }

        public Node getEnd() {
            return end;
        }

        public Line getLine() {
            return line;
        }

        public Node getLineEnd() {
            return lineEnd;
        }

        @Override
        public String toString() {
            return "Step{" +
                    "start=" + start +
                    ", end=" + end +
                    ", lineEnd=" + lineEnd +
                    ", line=" + line +
                    "}\n";
        }
    }

    protected Node start;
    protected Node end;
    protected Queue<Step> steps;

    public Journey(Node start, Node end, List<Step> steps) {
        this.start = start;
        this.end = end;
        this.steps = new LinkedList<>(steps);
    }

    public Node getStart() {
        return start;
    }

    public Node getEnd() {
        return end;
    }

    public Queue<Step> getSteps() {
        return steps;
    }

    @Override
    public String toString() {
        return "Journey{" +
                "start=" + start +
                ", end=" + end +
                ", steps=\n" + steps +
                '}';
    }
}

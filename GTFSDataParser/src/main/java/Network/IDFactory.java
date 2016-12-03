package Network;

/**
 * Created by micha on 03.12.2016.
 */
public class IDFactory {
    private int counter;

    public int createID() {
        return ++this.counter;
    }
}

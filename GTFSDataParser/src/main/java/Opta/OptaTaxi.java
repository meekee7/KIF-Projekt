package Opta;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Created by Sabine on 07.02.2017.
 */
@XStreamAlias("OptaTaxi")
public class OptaTaxi {
    private int capacity;
    private long Id;

    public int getCapacity(){
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

}

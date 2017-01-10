package Simulation.Entity;

import Network.ColourFactory;
import Simulation.Simulator;

import java.awt.*;

/**
 * Created by micha on 03.01.2017.
 */
public abstract class AbstractEntity {
    protected Simulator simulator;
    protected int id;
    protected Color colour;

    public AbstractEntity(Simulator simulator, int id) {
        this.simulator = simulator;
        this.id = id;
        this.colour = ColourFactory.createColour();
    }

    public int getId() {
        return id;
    }

    public Color getColour() {
        return colour;
    }
}

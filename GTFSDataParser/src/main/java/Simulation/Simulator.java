package Simulation;

import Network.Graph;
import Network.Line;
import Network.Node;
import Simulation.Entity.NodeLocation;
import Simulation.Entity.Passenger;
import Simulation.Entity.Taxi;
import Simulation.Factory.FaultFactory;
import Simulation.Factory.PassengerFactory;
import Simulation.LineSimulation.LineSimulator;
import Simulation.LineSimulation.LineTaxi;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by micha on 11.12.2016.
 */
public abstract class Simulator {
    protected PassengerFactory passengerFactory;
    protected FaultFactory faultFactory;
    protected Graph graph;
    protected Collection<Passenger> passengers;
    protected Collection<Taxi> taxis;
    protected int turn;
    protected Map<Node, NodeLocation> locmap;
    protected double movementspeed;
    protected List<Frame> recordedFrames = new LinkedList<>();
    protected SimulationConfig config;
    protected LongSummaryStatistics waitingstats = new LongSummaryStatistics();

    public Simulator(SimulationConfig config) {
        this.config = config;
    }

    protected abstract void advanceOneTurn();

    protected void createLocmap() {
        this.locmap = new HashMap<>(this.graph.getNodes().size());
        this.graph.getNodes().forEach(x -> this.locmap.put(x, new NodeLocation(x, x.getLat(), x.getLon())));
        //this.movementspeed = 0.0124689438025 / 3.0 * 2.0;
        //First number is 1.11km
        //With an average speed of 20km/h that takes 3.33 minutes
        //So 1min/turn or 0.37km/turn
        this.movementspeed = 1000; //In metres
    }

    public Frame getNextFrame() {
        this.turn++;
        this.passengers.addAll(this.passengerFactory.createNewPassengers(this.turn));
        this.faultFactory.createRepairedNodes().forEach(x -> {/* TODO SET NOT FAULTY */});
        this.faultFactory.createFaultyNodes().forEach(x -> {/* TODO SET FAULTY */});
        this.faultFactory.createRepairedTaxis().forEach(x -> {/* TODO SET NOT FAULTY */});
        this.faultFactory.createFaultyTaxis().forEach(x -> {/* TODO SET FAULTY */});
        this.faultFactory.createCommFaultTaxis().forEach(x -> {/* TODO SET COMM FAULT */});
        this.advanceOneTurn();
        this.taxis.forEach(Taxi::incrementLoadStats);
        this.passengers.stream().filter(Passenger::needsPickup).forEach(Passenger::incwaiting);
        this.waitingstats.accept(this.passengers.stream().filter(Passenger::needsPickup).count());

        if (this.turn % 1000 == 0)
            System.out.println("Calculated turn " + this.turn);
        //Frame frame = new Frame(this.turn, this.graph, this.taxis, this.passengers);
        //this.recordedFrames.add(frame);
        return null;
    }

    public String getStats() {
        StringBuilder sb = new StringBuilder();
        String nl = System.lineSeparator();
        sb.append("Graph " + this.graph.getName() + nl);
        sb.append("Type " + this.getClass().getSimpleName() + nl);
        sb.append("Passengers" + nl);
        List<Passenger> delivered = this.passengers.stream().filter(Passenger::isDelivered).collect(Collectors.toList());
        sb.append("Undelivered: " + (this.passengers.size() - delivered.size()) + nl);
        sb.append("Superstarved: " + this.passengers.stream().filter(x -> x.getCreatedAt() < 100 && !x.isDelivered()).count() + nl);
        sb.append("Happiness: " + delivered.stream().mapToDouble(Passenger::getHappinessIndex).summaryStatistics() + nl);
        sb.append("Waiting: " + delivered.stream().mapToInt(Passenger::getWaitingTime).summaryStatistics() + nl);
        sb.append("Switches: " + delivered.stream().mapToInt(Passenger::getSwitches).summaryStatistics() + nl);
        sb.append("InitPickup: " + delivered.stream().mapToInt(Passenger::getInitPickupTime).summaryStatistics() + nl);
        sb.append("TripTime: " + delivered.stream().mapToInt(Passenger::getTripTime).summaryStatistics() + nl);
        sb.append("Denied: " + delivered.stream().mapToInt(Passenger::getDenied).summaryStatistics() + nl);
        sb.append("Waiting/Round: " + this.waitingstats);
        sb.append(nl);
        sb.append("Taxis:" + nl);
        sb.append("PassengersLoaded: " + this.taxis.stream().mapToInt(Taxi::getPassengersloaded).summaryStatistics() + nl);
        sb.append("TotalDistance: " + this.taxis.stream().mapToDouble(Taxi::getTotaldistance).summaryStatistics() + nl);
        sb.append("NodesHit: " + this.taxis.stream().mapToInt(Taxi::getNodeshit).summaryStatistics() + nl);
        sb.append("Standstill: " + this.taxis.stream().mapToInt(Taxi::getStandstill).summaryStatistics() + nl);
        IntSummaryStatistics totalloadoutstats = new IntSummaryStatistics();
        this.taxis.forEach(x -> totalloadoutstats.combine(x.getLoadoutstats()));
        sb.append("LoadStats: " + totalloadoutstats + nl);
        if (this instanceof LineSimulator)
            System.out.println(this.taxis.stream().filter(x -> x.getLoadoutstats().getMax() == 0).map(x -> (LineTaxi) x).map(LineTaxi::getLine).map(Line::getId).distinct().collect(Collectors.toList()));
        return sb.toString();
    }

    public void writeStatsToFile(String directory) {
        List<String[]> passdata = new ArrayList<>(this.passengers.size());
        passdata.add(new String[]{"PassID", "Happiness", "Waiting", "Switches", "InitPickup", "TripTime", "Denied"});
        this.passengers.stream().filter(Passenger::isDelivered).forEach(x -> passdata.add(new String[]{
                Integer.toString(x.getId()),
                Double.toString(x.getHappinessIndex()).replace('.', ','),
                Integer.toString(x.getWaitingTime()),
                Integer.toString(x.getSwitches()),
                Integer.toString(x.getInitPickupTime()),
                Integer.toString(x.getTripTime()),
                Integer.toString(x.getDenied())
        }));

        List<String[]> taxidata = new ArrayList<>(this.taxis.size() + 1);
        taxidata.add(new String[]{"TaxiID", "Line", "PassengersLoaded", "TotalDistance", "NodesHit", "LoadAVG", "LoadMax", "Standstill"});
        this.taxis.forEach(x -> taxidata.add(new String[]{
                Integer.toString(x.getId()),
                x instanceof LineTaxi ? Integer.toString(((LineTaxi) x).getLine().getId()) : "-1",
                Integer.toString(x.getPassengersloaded()),
                Double.toString(x.getTotaldistance()).replace('.', ','),
                Integer.toString(x.getNodeshit()),
                Double.toString(x.getLoadoutstats().getAverage()).replace('.', ','),
                Integer.toString(x.getLoadoutstats().getMax()),
                Integer.toString(x.getStandstill())
        }));

        Path passpath = Paths.get(directory, this.graph.getName(), "passengers.csv");
        Path taxipath = Paths.get(directory, this.graph.getName(), "taxis.csv");
        Path summpath = Paths.get(directory, this.graph.getName(), "summary.txt");
        try {
            Files.createDirectories(passpath.getParent());
            if (!Files.exists(passpath))
                Files.createFile(passpath);

            try (FileWriter fw = new FileWriter(passpath.toString())) {
                for (String[] line : passdata)
                    fw.write(String.join(";", line) + ";" + System.lineSeparator());
            }
            System.out.println("Wrote file " + passpath);

            Files.createDirectories(taxipath.getParent());
            if (!Files.exists(taxipath))
                Files.createFile(taxipath);

            try (FileWriter fw = new FileWriter(taxipath.toString())) {
                for (String[] line : taxidata)
                    fw.write(String.join(";", line) + ";" + System.lineSeparator());
            }
            System.out.println("Wrote file" + taxipath);

            Files.createDirectories(summpath.getParent());
            if (!Files.exists(summpath))
                Files.createFile(summpath);


            try (FileWriter fw = new FileWriter(summpath.toString())) {
                fw.write(this.getStats());
            }
            System.out.println("Wrote file" + summpath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void simulate() {
        for (int i = 0; i < this.config.getTurns(); i++)
            this.getNextFrame();
    }

    public int getTurn() {
        return turn;
    }

    public NodeLocation getLoc(Node node) {
        return this.locmap.get(node);
    }

    public SimulationConfig getConfig() {
        return config;
    }
}

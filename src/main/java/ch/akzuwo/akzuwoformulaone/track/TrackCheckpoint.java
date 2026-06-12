package ch.akzuwo.akzuwoformulaone.track;

import org.bukkit.Location;

public final class TrackCheckpoint {

    private final String name;
    private final Location location;
    private final double radius;

    public TrackCheckpoint(String name, Location location, double radius) {
        this.name = name;
        this.location = location;
        this.radius = radius;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    public double getRadius() {
        return radius;
    }
}

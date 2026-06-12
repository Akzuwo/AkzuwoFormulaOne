package ch.akzuwo.akzuwoformulaone.track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public final class Track {

    private final String id;
    private String displayName;
    private String worldName;
    private boolean enabled;
    private int laps;
    private Location lobbyLocation;
    private Location spawnLocation;
    private Location startLine;
    private Location finishLine;
    private final List<TrackCheckpoint> checkpoints = new ArrayList<>();

    public Track(String id, String displayName, String worldName, boolean enabled, int laps) {
        this.id = id.toLowerCase();
        this.displayName = displayName;
        this.worldName = worldName;
        this.enabled = enabled;
        this.laps = laps;
    }

    public boolean isUsable() {
        return enabled
            && getWorld() != null
            && lobbyLocation != null
            && spawnLocation != null
            && finishLine != null
            && startLine != null
            && laps > 0;
    }

    public World getWorld() {
        return worldName == null ? null : Bukkit.getWorld(worldName);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getLaps() {
        return laps;
    }

    public void setLaps(int laps) {
        this.laps = laps;
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public void setLobbyLocation(Location lobbyLocation) {
        this.lobbyLocation = lobbyLocation;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public Location getStartLine() {
        return startLine;
    }

    public void setStartLine(Location startLine) {
        this.startLine = startLine;
    }

    public Location getFinishLine() {
        return finishLine;
    }

    public void setFinishLine(Location finishLine) {
        this.finishLine = finishLine;
    }

    public List<TrackCheckpoint> getCheckpoints() {
        return Collections.unmodifiableList(checkpoints);
    }

    public void addCheckpoint(TrackCheckpoint checkpoint) {
        checkpoints.add(checkpoint);
    }

    public boolean removeCheckpoint(String name) {
        return checkpoints.removeIf(cp -> cp.getName().equalsIgnoreCase(name));
    }

    public void clearCheckpoints() {
        checkpoints.clear();
    }
}

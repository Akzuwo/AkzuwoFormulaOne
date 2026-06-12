package ch.akzuwo.formulaboatracing.race;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class RacePlayer {

    private final UUID uuid;
    private final String name;
    private final LapTimer raceTimer = new LapTimer();
    private final LapTimer lapTimer = new LapTimer();
    private int currentLap = 1;
    private int nextCheckpointIndex;
    private boolean finished;
    private long finishTimeMillis;
    private long lastWarningMillis;
    private long lastFinishPassMillis;

    public RacePlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
    }

    public void startTiming() {
        raceTimer.start();
        lapTimer.start();
    }

    public void nextLap() {
        currentLap++;
        nextCheckpointIndex = 0;
        lapTimer.start();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getCurrentLap() {
        return currentLap;
    }

    public int getNextCheckpointIndex() {
        return nextCheckpointIndex;
    }

    public void advanceCheckpoint() {
        nextCheckpointIndex++;
    }

    public boolean isFinished() {
        return finished;
    }

    public void finish() {
        this.finished = true;
        this.finishTimeMillis = raceTimer.elapsedMillis();
    }

    public long getFinishTimeMillis() {
        return finishTimeMillis;
    }

    public long getElapsedMillis() {
        return raceTimer.elapsedMillis();
    }

    public long getLastWarningMillis() {
        return lastWarningMillis;
    }

    public void setLastWarningMillis(long lastWarningMillis) {
        this.lastWarningMillis = lastWarningMillis;
    }

    public long getLastFinishPassMillis() {
        return lastFinishPassMillis;
    }

    public void setLastFinishPassMillis(long lastFinishPassMillis) {
        this.lastFinishPassMillis = lastFinishPassMillis;
    }
}

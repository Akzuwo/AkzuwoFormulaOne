package ch.akzuwo.formulaboatracing.race;

import ch.akzuwo.formulaboatracing.track.Track;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Boat;

public final class Race {

    private final UUID id = UUID.randomUUID();
    private final Track track;
    private RaceType type;
    private RaceState state = RaceState.WAITING;
    private final Map<UUID, RacePlayer> players = new LinkedHashMap<>();
    private final Map<UUID, Boat> boats = new LinkedHashMap<>();

    public Race(Track track, RaceType type) {
        this.track = track;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public String getShortId() {
        return id.toString().substring(0, 8);
    }

    public Track getTrack() {
        return track;
    }

    public RaceType getType() {
        return type;
    }

    public void setType(RaceType type) {
        this.type = type;
    }

    public RaceState getState() {
        return state;
    }

    public void setState(RaceState state) {
        this.state = state;
    }

    public Collection<RacePlayer> getPlayers() {
        return players.values();
    }

    public Optional<RacePlayer> getPlayer(UUID uuid) {
        return Optional.ofNullable(players.get(uuid));
    }

    public void addPlayer(RacePlayer player) {
        players.put(player.getUuid(), player);
        if (players.size() > 1) {
            type = RaceType.MULTIPLAYER;
        }
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
    }

    public int getPlayerCount() {
        return players.size();
    }

    public void putBoat(UUID playerUuid, Boat boat) {
        boats.put(playerUuid, boat);
    }

    public Collection<Boat> getBoats() {
        return boats.values();
    }

    public Optional<Boat> getBoat(UUID playerUuid) {
        return Optional.ofNullable(boats.get(playerUuid));
    }

    public void removeBoat(UUID playerUuid) {
        boats.remove(playerUuid);
    }

    public boolean allFinished() {
        return !players.isEmpty() && players.values().stream().allMatch(RacePlayer::isFinished);
    }
}

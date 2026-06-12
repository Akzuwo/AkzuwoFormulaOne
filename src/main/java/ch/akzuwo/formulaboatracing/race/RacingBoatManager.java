package ch.akzuwo.formulaboatracing.race;

import java.util.Optional;
import java.util.UUID;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class RacingBoatManager {

    private final NamespacedKey racingBoatKey;
    private final NamespacedKey trackIdKey;
    private final NamespacedKey raceIdKey;
    private final NamespacedKey ownerKey;

    public RacingBoatManager(JavaPlugin plugin) {
        this.racingBoatKey = new NamespacedKey("formulaboat", "racing_boat");
        this.trackIdKey = new NamespacedKey("formulaboat", "track_id");
        this.raceIdKey = new NamespacedKey("formulaboat", "race_id");
        this.ownerKey = new NamespacedKey("formulaboat", "owner_uuid");
    }

    public void mark(Boat boat, String trackId, UUID raceId, UUID ownerUuid) {
        PersistentDataContainer data = boat.getPersistentDataContainer();
        data.set(racingBoatKey, PersistentDataType.BYTE, (byte) 1);
        data.set(trackIdKey, PersistentDataType.STRING, trackId);
        data.set(raceIdKey, PersistentDataType.STRING, raceId.toString());
        data.set(ownerKey, PersistentDataType.STRING, ownerUuid.toString());
    }

    public boolean isRacingBoat(Entity entity) {
        return entity instanceof Boat
            && entity.getPersistentDataContainer().has(racingBoatKey, PersistentDataType.BYTE);
    }

    public Optional<UUID> getRaceId(Entity entity) {
        String value = entity.getPersistentDataContainer().get(raceIdKey, PersistentDataType.STRING);
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public Optional<String> getTrackId(Entity entity) {
        return Optional.ofNullable(entity.getPersistentDataContainer().get(trackIdKey, PersistentDataType.STRING));
    }
}

package ch.akzuwo.akzuwoformulaone.storage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StorageManager {

    void load();

    void save();

    Optional<StoredTime> getBestTime(UUID playerUuid, String trackId);

    boolean updateBestTime(UUID playerUuid, String playerName, String trackId, long timeMillis);

    List<StoredTime> getLeaderboard(String trackId, int limit);
}

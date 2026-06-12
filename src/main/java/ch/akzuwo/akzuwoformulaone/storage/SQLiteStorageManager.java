package ch.akzuwo.akzuwoformulaone.storage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class SQLiteStorageManager implements StorageManager {

    @Override
    public void load() {
        throw new UnsupportedOperationException("SQLite storage is prepared as an extension point but not implemented yet.");
    }

    @Override
    public void save() {
    }

    @Override
    public Optional<StoredTime> getBestTime(UUID playerUuid, String trackId) {
        return Optional.empty();
    }

    @Override
    public boolean updateBestTime(UUID playerUuid, String playerName, String trackId, long timeMillis) {
        return false;
    }

    @Override
    public List<StoredTime> getLeaderboard(String trackId, int limit) {
        return List.of();
    }
}

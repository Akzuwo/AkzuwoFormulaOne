package ch.akzuwo.akzuwoformulaone.storage;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class YamlStorageManager implements StorageManager {

    private final JavaPlugin plugin;
    private final File file;
    private final Map<String, Map<UUID, StoredTime>> times = new HashMap<>();

    public YamlStorageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "times.yml");
    }

    @Override
    public void load() {
        times.clear();
        if (!file.exists()) {
            save();
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection tracks = config.getConfigurationSection("tracks");
        if (tracks == null) {
            return;
        }
        for (String trackId : tracks.getKeys(false)) {
            ConfigurationSection players = tracks.getConfigurationSection(trackId + ".players");
            if (players == null) {
                continue;
            }
            Map<UUID, StoredTime> byPlayer = new HashMap<>();
            for (String uuidText : players.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidText);
                    String base = uuidText + ".";
                    String name = players.getString(base + "name", "Unknown");
                    long best = players.getLong(base + "best-time-ms");
                    Instant date = Instant.parse(players.getString(base + "date", Instant.now().toString()));
                    byPlayer.put(uuid, new StoredTime(uuid, name, trackId, best, date));
                } catch (IllegalArgumentException ex) {
                    plugin.getLogger().warning("Skipping invalid time entry in times.yml: " + uuidText);
                }
            }
            times.put(trackId, byPlayer);
        }
    }

    @Override
    public void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, Map<UUID, StoredTime>> trackEntry : times.entrySet()) {
            for (StoredTime time : trackEntry.getValue().values()) {
                String base = "tracks." + trackEntry.getKey() + ".players." + time.playerUuid() + ".";
                config.set(base + "name", time.playerName());
                config.set(base + "best-time-ms", time.bestTimeMillis());
                config.set(base + "date", time.date().toString());
            }
        }
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                throw new IOException("Could not create plugin data folder");
            }
            config.save(file);
        } catch (IOException ex) {
            plugin.getLogger().severe("Could not save times.yml: " + ex.getMessage());
        }
    }

    @Override
    public Optional<StoredTime> getBestTime(UUID playerUuid, String trackId) {
        return Optional.ofNullable(times.getOrDefault(trackId, Map.of()).get(playerUuid));
    }

    @Override
    public boolean updateBestTime(UUID playerUuid, String playerName, String trackId, long timeMillis) {
        Map<UUID, StoredTime> byPlayer = times.computeIfAbsent(trackId, ignored -> new HashMap<>());
        StoredTime old = byPlayer.get(playerUuid);
        if (old != null && old.bestTimeMillis() <= timeMillis) {
            return false;
        }
        byPlayer.put(playerUuid, new StoredTime(playerUuid, playerName, trackId, timeMillis, Instant.now()));
        save();
        return true;
    }

    @Override
    public List<StoredTime> getLeaderboard(String trackId, int limit) {
        return times.getOrDefault(trackId, Map.of()).values().stream()
            .sorted(Comparator.comparingLong(StoredTime::bestTimeMillis))
            .limit(limit)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}

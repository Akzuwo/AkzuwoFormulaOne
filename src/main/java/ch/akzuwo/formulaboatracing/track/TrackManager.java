package ch.akzuwo.formulaboatracing.track;

import ch.akzuwo.formulaboatracing.utils.LocationUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class TrackManager {

    private final JavaPlugin plugin;
    private final File tracksFolder;
    private final Map<String, Track> tracks = new HashMap<>();

    public TrackManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.tracksFolder = new File(plugin.getDataFolder(), "tracks");
    }

    public void loadAll() {
        tracks.clear();
        if (!tracksFolder.exists() && !tracksFolder.mkdirs()) {
            plugin.getLogger().warning("Could not create tracks folder: " + tracksFolder.getAbsolutePath());
            return;
        }

        File[] files = tracksFolder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            load(file).ifPresent(track -> tracks.put(track.getId(), track));
        }
    }

    public Optional<Track> reload(String id) {
        File file = fileFor(id);
        if (!file.exists()) {
            return Optional.empty();
        }
        Optional<Track> loaded = load(file);
        loaded.ifPresent(track -> tracks.put(track.getId(), track));
        return loaded;
    }

    private Optional<Track> load(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String id = config.getString("id", file.getName().replace(".yml", "")).toLowerCase(Locale.ROOT);
        String displayName = config.getString("display-name", id);
        String worldName = config.getString("world");
        boolean enabled = config.getBoolean("enabled", false);
        int laps = Math.max(1, config.getInt("laps", plugin.getConfig().getInt("race.default-laps", 3)));

        Track track = new Track(id, displayName, worldName, enabled, laps);
        track.setLobbyLocation(LocationUtil.readLocation(config.getConfigurationSection("lobby")));
        track.setSpawnLocation(LocationUtil.readLocation(config.getConfigurationSection("spawn")));
        track.setStartLine(LocationUtil.readLocation(config.getConfigurationSection("start-line")));
        track.setFinishLine(LocationUtil.readLocation(config.getConfigurationSection("finish-line")));

        List<Map<?, ?>> checkpoints = config.getMapList("checkpoints");
        for (Map<?, ?> raw : checkpoints) {
            YamlConfiguration cpConfig = new YamlConfiguration();
            raw.forEach((key, value) -> cpConfig.set(String.valueOf(key), value));
            Location location = LocationUtil.readLocation(cpConfig);
            if (location == null) {
                plugin.getLogger().warning("Skipping checkpoint in " + file.getName() + " because its world is not loaded.");
                continue;
            }
            track.addCheckpoint(new TrackCheckpoint(
                cpConfig.getString("name", "checkpoint"),
                location,
                Math.max(0.5, cpConfig.getDouble("radius", 5.0))
            ));
        }

        if (worldName == null || Bukkit.getWorld(worldName) == null) {
            plugin.getLogger().warning("Track '" + id + "' references unloaded world '" + worldName + "'. It will not be usable until that world is loaded.");
        }
        return Optional.of(track);
    }

    public void save(Track track) throws IOException {
        if (!tracksFolder.exists() && !tracksFolder.mkdirs()) {
            throw new IOException("Could not create tracks folder");
        }
        YamlConfiguration config = new YamlConfiguration();
        config.set("id", track.getId());
        config.set("display-name", track.getDisplayName());
        config.set("world", track.getWorldName());
        config.set("enabled", track.isEnabled());
        config.set("laps", track.getLaps());
        writeLocationSection(config, "lobby", track.getLobbyLocation(), true);
        writeLocationSection(config, "spawn", track.getSpawnLocation(), true);
        writeLocationSection(config, "start-line", track.getStartLine(), false);
        writeLocationSection(config, "finish-line", track.getFinishLine(), false);

        List<Map<String, Object>> checkpointMaps = new ArrayList<>();
        for (TrackCheckpoint checkpoint : track.getCheckpoints()) {
            Location location = checkpoint.getLocation();
            Map<String, Object> map = new HashMap<>();
            map.put("name", checkpoint.getName());
            map.put("world", location.getWorld() == null ? track.getWorldName() : location.getWorld().getName());
            map.put("x", location.getX());
            map.put("y", location.getY());
            map.put("z", location.getZ());
            map.put("radius", checkpoint.getRadius());
            checkpointMaps.add(map);
        }
        config.set("checkpoints", checkpointMaps);
        config.save(fileFor(track.getId()));
        tracks.put(track.getId(), track);
    }

    private void writeLocationSection(YamlConfiguration config, String path, Location location, boolean includeRotation) {
        if (location == null) {
            return;
        }
        ConfigurationSection section = config.createSection(path);
        LocationUtil.writeLocation(section, location, includeRotation);
    }

    public Track create(String id, String displayName, String worldName) {
        Track track = new Track(id, displayName, worldName, false, plugin.getConfig().getInt("race.default-laps", 3));
        tracks.put(track.getId(), track);
        return track;
    }

    public Optional<Track> getTrack(String id) {
        return Optional.ofNullable(tracks.get(id.toLowerCase(Locale.ROOT)));
    }

    public Collection<Track> getTracks() {
        return tracks.values().stream()
            .sorted(Comparator.comparing(Track::getId))
            .toList();
    }

    public List<String> getTrackIds() {
        return getTracks().stream().map(Track::getId).toList();
    }

    private File fileFor(String id) {
        return new File(tracksFolder, id.toLowerCase(Locale.ROOT) + ".yml");
    }
}

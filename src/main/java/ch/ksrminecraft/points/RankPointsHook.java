package ch.ksrminecraft.points;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class RankPointsHook {

    private final JavaPlugin plugin;
    private RankPointsConfig config;
    private RankPointsServiceBridge rankPoints;
    private boolean available;
    private boolean pluginPresent;

    public RankPointsHook(JavaPlugin plugin, RankPointsConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void initialize() {
        this.available = false;
        this.rankPoints = null;
        this.pluginPresent = Bukkit.getPluginManager().getPlugin("RankPointsAPI") != null;

        if (!config.isEnabled()) {
            debug("RankPoints integration disabled in config.");
            return;
        }
        if (!pluginPresent) {
            plugin.getLogger().warning("RankPointsAPI plugin not found. Race points will not be awarded.");
            return;
        }

        try {
            this.rankPoints = RankPointsServiceBridge.find();
            this.available = rankPoints != null;
            if (available) {
                debug("RankPointsAPI service connected.");
            } else {
                plugin.getLogger().warning("RankPointsAPI service not available.");
            }
        } catch (LinkageError ex) {
            plugin.getLogger().warning("RankPointsAPI service class is not available. Install the service-enabled RankPointsAPI build.");
            if (config.isDebug()) {
                ex.printStackTrace();
            }
        }
    }

    public void reload(RankPointsConfig config) {
        this.config = config;
        initialize();
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isPluginPresent() {
        return pluginPresent;
    }

    public boolean addPoints(UUID uuid, int points) {
        if (!available || rankPoints == null || points <= 0) {
            return false;
        }
        try {
            rankPoints.addPoints(uuid, points);
            return true;
        } catch (RuntimeException ex) {
            plugin.getLogger().warning("RankPointsAPI addPoints failed: " + ex.getMessage());
            if (config.isDebug()) {
                ex.printStackTrace();
            }
            return false;
        }
    }

    private void debug(String message) {
        if (config.isDebug()) {
            plugin.getLogger().info("[RankPoints] " + message);
        }
    }
}

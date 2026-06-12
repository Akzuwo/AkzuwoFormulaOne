package ch.ksrminecraft.points;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public final class RankPointsConfig {

    private final boolean enabled;
    private final boolean debug;
    private final boolean rewardsEnabled;
    private final int finishRaceBasePoints;
    private final int personalBestBonusPoints;
    private final int trackRecordBonusPoints;
    private final boolean rewardCancelledRaces;
    private final boolean requireValidCheckpoints;
    private final boolean cooldownEnabled;
    private final int cooldownSeconds;
    private final double defaultTrackMultiplier;
    private final Map<String, Double> trackMultipliers;

    private RankPointsConfig(
        boolean enabled,
        boolean debug,
        boolean rewardsEnabled,
        int finishRaceBasePoints,
        int personalBestBonusPoints,
        int trackRecordBonusPoints,
        boolean rewardCancelledRaces,
        boolean requireValidCheckpoints,
        boolean cooldownEnabled,
        int cooldownSeconds,
        double defaultTrackMultiplier,
        Map<String, Double> trackMultipliers
    ) {
        this.enabled = enabled;
        this.debug = debug;
        this.rewardsEnabled = rewardsEnabled;
        this.finishRaceBasePoints = finishRaceBasePoints;
        this.personalBestBonusPoints = personalBestBonusPoints;
        this.trackRecordBonusPoints = trackRecordBonusPoints;
        this.rewardCancelledRaces = rewardCancelledRaces;
        this.requireValidCheckpoints = requireValidCheckpoints;
        this.cooldownEnabled = cooldownEnabled;
        this.cooldownSeconds = cooldownSeconds;
        this.defaultTrackMultiplier = defaultTrackMultiplier;
        this.trackMultipliers = Map.copyOf(trackMultipliers);
    }

    public static RankPointsConfig from(JavaPlugin plugin) {
        double defaultMultiplier = plugin.getConfig().getDouble("rank-points.track-multipliers.default", 1.0);
        if (defaultMultiplier < 0) {
            plugin.getLogger().warning("rank-points.track-multipliers.default is negative. Falling back to 1.0.");
            defaultMultiplier = 1.0;
        }

        Map<String, Double> multipliers = new HashMap<>();
        ConfigurationSection tracks = plugin.getConfig().getConfigurationSection("rank-points.track-multipliers.tracks");
        if (tracks != null) {
            for (String trackId : tracks.getKeys(false)) {
                double multiplier = tracks.getDouble(trackId, defaultMultiplier);
                if (multiplier < 0) {
                    plugin.getLogger().warning("Negative RankPoints multiplier for track '" + trackId + "' ignored.");
                    continue;
                }
                multipliers.put(trackId.toLowerCase(Locale.ROOT), multiplier);
            }
        }

        return new RankPointsConfig(
            plugin.getConfig().getBoolean("rank-points.enabled", true),
            plugin.getConfig().getBoolean("rank-points.debug", false),
            plugin.getConfig().getBoolean("rank-points.rewards.enabled", true),
            points(plugin, "rank-points.rewards.finish-race-base-points", 10),
            points(plugin, "rank-points.rewards.personal-best-bonus-points", 5),
            points(plugin, "rank-points.rewards.track-record-bonus-points", 15),
            plugin.getConfig().getBoolean("rank-points.rewards.reward-cancelled-races", false),
            plugin.getConfig().getBoolean("rank-points.rewards.require-valid-checkpoints", true),
            plugin.getConfig().getBoolean("rank-points.rewards.cooldown.enabled", true),
            Math.max(0, plugin.getConfig().getInt("rank-points.rewards.cooldown.seconds", 120)),
            defaultMultiplier,
            multipliers
        );
    }

    private static int points(JavaPlugin plugin, String path, int fallback) {
        int value = plugin.getConfig().getInt(path, fallback);
        if (value < 0) {
            plugin.getLogger().warning(path + " is negative. Falling back to 0.");
            return 0;
        }
        return value;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isRewardsEnabled() {
        return rewardsEnabled;
    }

    public int getFinishRaceBasePoints() {
        return finishRaceBasePoints;
    }

    public int getPersonalBestBonusPoints() {
        return personalBestBonusPoints;
    }

    public int getTrackRecordBonusPoints() {
        return trackRecordBonusPoints;
    }

    public boolean isRewardCancelledRaces() {
        return rewardCancelledRaces;
    }

    public boolean isRequireValidCheckpoints() {
        return requireValidCheckpoints;
    }

    public boolean isCooldownEnabled() {
        return cooldownEnabled;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public double getDefaultTrackMultiplier() {
        return defaultTrackMultiplier;
    }

    public double getTrackMultiplier(String trackId) {
        if (trackId == null) {
            return defaultTrackMultiplier;
        }
        return trackMultipliers.getOrDefault(trackId.toLowerCase(Locale.ROOT), defaultTrackMultiplier);
    }
}

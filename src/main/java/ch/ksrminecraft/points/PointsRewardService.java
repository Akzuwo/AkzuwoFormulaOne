package ch.ksrminecraft.points;

import ch.akzuwo.akzuwoformulaone.race.Race;
import ch.akzuwo.akzuwoformulaone.track.Track;
import ch.akzuwo.akzuwoformulaone.utils.MessageManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class PointsRewardService {

    private final JavaPlugin plugin;
    private final RankPointsHook hook;
    private final MessageManager messages;
    private final RacePointsCalculator calculator = new RacePointsCalculator();
    private final Map<String, Long> cooldowns = new HashMap<>();
    private RankPointsConfig config;

    public PointsRewardService(JavaPlugin plugin, RankPointsHook hook, RankPointsConfig config, MessageManager messages) {
        this.plugin = plugin;
        this.hook = hook;
        this.config = config;
        this.messages = messages;
    }

    public PointsRewardResult rewardRaceFinish(
        Player player,
        Race race,
        Track track,
        boolean personalBest,
        boolean trackRecord,
        boolean validCheckpoints
    ) {
        if (player == null || !player.isOnline()) {
            return PointsRewardResult.skipped("player-offline");
        }
        if (!config.isEnabled()) {
            return PointsRewardResult.skipped("disabled");
        }
        if (!config.isRewardsEnabled()) {
            return PointsRewardResult.skipped("rewards-disabled");
        }
        if (!hook.isAvailable()) {
            messages.send(player, "points.api-unavailable", "&cPunkte konnten nicht vergeben werden, weil das Punktesystem nicht erreichbar ist.");
            return PointsRewardResult.skipped("api-unavailable");
        }
        if (config.isRequireValidCheckpoints() && !validCheckpoints) {
            return PointsRewardResult.skipped("invalid-checkpoints");
        }

        int points = calculator.calculate(track.getId(), true, false, validCheckpoints, personalBest, trackRecord, config);
        if (points <= 0) {
            return PointsRewardResult.skipped("zero-points");
        }
        if (isCooldownActive(player.getUniqueId(), track.getId())) {
            messages.send(player, "points.skipped-cooldown", "&7Du hast für diese Strecke gerade erst Punkte erhalten.");
            return PointsRewardResult.skipped("cooldown");
        }

        try {
            boolean success = hook.addPoints(player.getUniqueId(), points);
            if (!success) {
                return PointsRewardResult.skipped("api-unavailable");
            }
            rememberCooldown(player.getUniqueId(), track.getId());
            messages.sendRaw(player, messages.get("points.rewarded", "&aDu hast &e%points% Punkte &afür dieses Rennen erhalten.").replace("%points%", String.valueOf(points)));
            return PointsRewardResult.rewarded(points);
        } catch (RuntimeException ex) {
            plugin.getLogger().warning("RankPoints reward failed: " + ex.getMessage());
            if (config.isDebug()) {
                ex.printStackTrace();
            }
            return PointsRewardResult.skipped("exception");
        }
    }

    public void reload(RankPointsConfig config) {
        this.config = config;
    }

    public RankPointsConfig getConfig() {
        return config;
    }

    private boolean isCooldownActive(UUID uuid, String trackId) {
        if (!config.isCooldownEnabled()) {
            return false;
        }
        Long lastReward = cooldowns.get(key(uuid, trackId));
        if (lastReward == null) {
            return false;
        }
        long cooldownMillis = config.getCooldownSeconds() * 1000L;
        return System.currentTimeMillis() - lastReward < cooldownMillis;
    }

    private void rememberCooldown(UUID uuid, String trackId) {
        if (config.isCooldownEnabled()) {
            cooldowns.put(key(uuid, trackId), System.currentTimeMillis());
        }
    }

    private String key(UUID uuid, String trackId) {
        return uuid + ":" + trackId.toLowerCase();
    }
}

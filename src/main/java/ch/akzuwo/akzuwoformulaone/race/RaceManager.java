package ch.akzuwo.akzuwoformulaone.race;

import ch.akzuwo.analytics.CrashReporter;
import ch.akzuwo.akzuwoformulaone.storage.StorageManager;
import ch.akzuwo.akzuwoformulaone.sounds.SoundManager;
import ch.akzuwo.akzuwoformulaone.track.Track;
import ch.akzuwo.akzuwoformulaone.track.TrackCheckpoint;
import ch.akzuwo.akzuwoformulaone.track.TrackManager;
import ch.akzuwo.akzuwoformulaone.utils.LocationUtil;
import ch.akzuwo.akzuwoformulaone.utils.MessageManager;
import ch.akzuwo.akzuwoformulaone.utils.TimeUtil;
import ch.akzuwo.akzuwoformulaone.visuals.VehicleVisualManager;
import ch.ksrminecraft.points.PointsRewardService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public final class RaceManager {

    private static final long CHECKPOINT_WARNING_COOLDOWN_MS = 2500L;
    private static final long FINISH_PASS_COOLDOWN_MS = 3000L;

    private final JavaPlugin plugin;
    private final TrackManager trackManager;
    private final StorageManager storageManager;
    private final RacingBoatManager racingBoatManager;
    private final VehicleVisualManager vehicleVisualManager;
    private final SoundManager soundManager;
    private final PointsRewardService pointsRewardService;
    private final MessageManager messages;
    private CrashReporter crashReporter;
    private final Map<UUID, Race> races = new HashMap<>();
    private final Map<UUID, UUID> playerRaces = new HashMap<>();
    private long startedRacesSinceStartup;
    private long finishedRacesSinceStartup;
    private long cancelledRacesSinceStartup;
    private BukkitTask tickerTask;

    public RaceManager(JavaPlugin plugin, TrackManager trackManager, StorageManager storageManager, RacingBoatManager racingBoatManager, VehicleVisualManager vehicleVisualManager, SoundManager soundManager, PointsRewardService pointsRewardService, MessageManager messages) {
        this.plugin = plugin;
        this.trackManager = trackManager;
        this.storageManager = storageManager;
        this.racingBoatManager = racingBoatManager;
        this.vehicleVisualManager = vehicleVisualManager;
        this.soundManager = soundManager;
        this.pointsRewardService = pointsRewardService;
        this.messages = messages;
    }

    public void setCrashReporter(CrashReporter crashReporter) {
        this.crashReporter = crashReporter;
    }

    public void startTicker() {
        if (tickerTask != null) {
            tickerTask.cancel();
        }
        tickerTask = new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    tickRunningRaces();
                } catch (RuntimeException ex) {
                    report("RaceManager", "tickRunningRaces", ex, Map.of("activeRaces", races.size()));
                }
            }
        }.runTaskTimer(plugin, 5L, 5L);
    }

    public void shutdown() {
        if (tickerTask != null) {
            tickerTask.cancel();
        }
        for (Race race : new ArrayList<>(races.values())) {
            cancelRace(race, "Plugin wird deaktiviert.");
        }
    }

    public Optional<Race> joinRace(Player player, Track track) {
        if (!track.isUsable()) {
            messages.sendRaw(player, "&cDiese Strecke ist noch nicht vollständig eingerichtet oder die Welt ist nicht geladen.");
            return Optional.empty();
        }
        if (getRace(player).isPresent()) {
            messages.sendRaw(player, "&cDu bist bereits in einem Rennen.");
            return Optional.empty();
        }

        Race race = findWaitingRace(track).orElseGet(() -> {
            Race created = new Race(track, RaceType.TIME_TRIAL);
            races.put(created.getId(), created);
            return created;
        });

        race.addPlayer(new RacePlayer(player));
        playerRaces.put(player.getUniqueId(), race.getId());
        player.teleport(track.getLobbyLocation());
        messages.sendRaw(player, "&aDu bist dem Rennen auf &f" + track.getDisplayName() + " &abeigetreten. Nutze &f/f1 start&a.");
        return Optional.of(race);
    }

    public boolean leaveRace(Player player) {
        Optional<Race> optionalRace = getRace(player);
        if (optionalRace.isEmpty()) {
            messages.sendRaw(player, "&cDu bist in keinem Rennen.");
            return false;
        }
        Race race = optionalRace.get();
        race.getBoat(player.getUniqueId()).ifPresent(boat -> {
            vehicleVisualManager.removeVisuals(boat);
            boat.eject();
            boat.remove();
        });
        race.removeBoat(player.getUniqueId());
        race.removePlayer(player.getUniqueId());
        playerRaces.remove(player.getUniqueId());
        messages.sendRaw(player, "&eDu hast das Rennen verlassen.");
        if (race.getPlayerCount() == 0) {
            races.remove(race.getId());
        }
        return true;
    }

    public boolean startRace(Player starter) {
        Optional<Race> optionalRace = getRace(starter);
        if (optionalRace.isEmpty()) {
            messages.sendRaw(starter, "&cDu bist in keinem Rennen.");
            return false;
        }
        Race race = optionalRace.get();
        if (race.getState() != RaceState.WAITING) {
            messages.sendRaw(starter, "&cDieses Rennen wurde bereits gestartet.");
            return false;
        }
        int countdown = Math.max(1, plugin.getConfig().getInt("race.countdown-seconds", 5));
        race.setState(RaceState.COUNTDOWN);

        new BukkitRunnable() {
            private int seconds = countdown;

            @Override
            public void run() {
                try {
                    if (race.getState() != RaceState.COUNTDOWN) {
                        cancel();
                        return;
                    }
                    if (seconds > 0) {
                        broadcastCountdown(race, seconds);
                        seconds--;
                        return;
                    }
                    beginRunning(race);
                    broadcastStart(race);
                    cancel();
                } catch (RuntimeException ex) {
                    report("RaceManager", "countdown", ex, Map.of("trackId", race.getTrack().getId(), "raceState", race.getState().name()));
                    cancelRace(race, "Interner Fehler beim Countdown");
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
        return true;
    }

    private void beginRunning(Race race) {
        race.setState(RaceState.RUNNING);
        startedRacesSinceStartup++;
        int index = 0;
        for (RacePlayer racePlayer : race.getPlayers()) {
            Player player = racePlayer.getPlayer();
            if (player == null) {
                continue;
            }
            Location spawn = race.getTrack().getSpawnLocation().clone().add(index * 2.0, 0.0, 0.0);
            player.teleport(spawn);
            Boat boat = spawnBoat(spawn, race, player.getUniqueId());
            race.putBoat(player.getUniqueId(), boat);
            vehicleVisualManager.applyVisuals(boat, player, race.getTrack());
            boat.addPassenger(player);
            racePlayer.startTiming();
            index++;
        }
    }

    private Boat spawnBoat(Location location, Race race, UUID ownerUuid) {
        World world = location.getWorld();
        if (world == null) {
            throw new IllegalStateException("Cannot spawn race boat without a loaded world.");
        }
        Boat boat = world.spawn(location, Boat.class);
        boat.setInvulnerable(true);
        boat.setPersistent(false);
        boat.setVelocity(new Vector(0, 0, 0));
        racingBoatManager.mark(boat, race.getTrack().getId(), race.getId(), ownerUuid);
        return boat;
    }

    private void tickRunningRaces() {
        for (Race race : new ArrayList<>(races.values())) {
            if (race.getState() != RaceState.RUNNING) {
                continue;
            }
            for (RacePlayer racePlayer : race.getPlayers()) {
                if (!racePlayer.isFinished()) {
                    tickRacePlayer(race, racePlayer);
                }
            }
            if (race.allFinished()) {
                finishRace(race);
            }
        }
    }

    private void tickRacePlayer(Race race, RacePlayer racePlayer) {
        Player player = racePlayer.getPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }
        Location location = player.getLocation();
        Track track = race.getTrack();
        List<TrackCheckpoint> checkpoints = track.getCheckpoints();

        if (racePlayer.getNextCheckpointIndex() < checkpoints.size()) {
            TrackCheckpoint next = checkpoints.get(racePlayer.getNextCheckpointIndex());
            if (isInside(location, next.getLocation(), next.getRadius())) {
                racePlayer.advanceCheckpoint();
                messages.sendRaw(player, "&aCheckpoint &f" + next.getName() + " &aerreicht.");
                soundManager.playCheckpoint(player);
            } else if (isInsideAnyLaterCheckpoint(location, checkpoints, racePlayer.getNextCheckpointIndex())) {
                warnCheckpoint(player, racePlayer, "&cDu hast einen Checkpoint übersprungen.");
            }
        } else if (isInside(location, track.getFinishLine(), plugin.getConfig().getDouble("race.finish-radius", 5.0))) {
            completeLapOrRace(race, racePlayer, player);
        }

        String nextName = racePlayer.getNextCheckpointIndex() < checkpoints.size()
            ? checkpoints.get(racePlayer.getNextCheckpointIndex()).getName()
            : "Finish";
        player.sendActionBar(Component.text("Runde " + racePlayer.getCurrentLap() + "/" + track.getLaps()
            + " | " + TimeUtil.formatMillis(racePlayer.getElapsedMillis())
            + " | Nächstes Ziel: " + nextName));
    }

    private void completeLapOrRace(Race race, RacePlayer racePlayer, Player player) {
        long now = System.currentTimeMillis();
        if (now - racePlayer.getLastFinishPassMillis() < FINISH_PASS_COOLDOWN_MS) {
            return;
        }
        racePlayer.setLastFinishPassMillis(now);

        if (racePlayer.getCurrentLap() >= race.getTrack().getLaps()) {
            racePlayer.finish();
            boolean trackRecord = isTrackRecord(race.getTrack().getId(), racePlayer.getFinishTimeMillis());
            boolean personalBest = storageManager.updateBestTime(
                racePlayer.getUuid(),
                racePlayer.getName(),
                race.getTrack().getId(),
                racePlayer.getFinishTimeMillis()
            );
            messages.sendRaw(player, "&aZiel! Zeit: &f" + TimeUtil.formatMillis(racePlayer.getFinishTimeMillis())
                + (personalBest ? " &6Neue Bestzeit!" : ""));
            soundManager.playFinish(player);
            try {
                pointsRewardService.rewardRaceFinish(player, race, race.getTrack(), personalBest, trackRecord, true);
            } catch (RuntimeException ex) {
                report("PointsRewardService", "rewardRaceFinish", ex, Map.of("trackId", race.getTrack().getId(), "raceState", race.getState().name()));
                plugin.getLogger().warning("RankPoints reward processing failed: " + ex.getMessage());
            }
        } else {
            racePlayer.nextLap();
            messages.sendRaw(player, "&aRunde &f" + racePlayer.getCurrentLap() + "/" + race.getTrack().getLaps() + " &agestartet.");
        }
    }

    private boolean isInsideAnyLaterCheckpoint(Location location, List<TrackCheckpoint> checkpoints, int nextIndex) {
        for (int i = nextIndex + 1; i < checkpoints.size(); i++) {
            TrackCheckpoint checkpoint = checkpoints.get(i);
            if (isInside(location, checkpoint.getLocation(), checkpoint.getRadius())) {
                return true;
            }
        }
        return false;
    }

    private boolean isInside(Location current, Location target, double radius) {
        return LocationUtil.sameWorld(current, target) && current.distanceSquared(target) <= radius * radius;
    }

    private void warnCheckpoint(Player player, RacePlayer racePlayer, String message) {
        long now = System.currentTimeMillis();
        if (now - racePlayer.getLastWarningMillis() < CHECKPOINT_WARNING_COOLDOWN_MS) {
            return;
        }
        racePlayer.setLastWarningMillis(now);
        messages.sendRaw(player, message);
    }

    private boolean isTrackRecord(String trackId, long finishTimeMillis) {
        return storageManager.getLeaderboard(trackId, 1).stream()
            .findFirst()
            .map(best -> finishTimeMillis < best.bestTimeMillis())
            .orElse(true);
    }

    private void finishRace(Race race) {
        race.setState(RaceState.FINISHED);
        finishedRacesSinceStartup++;
        List<RacePlayer> ranking = race.getPlayers().stream()
            .sorted(Comparator.comparingLong(RacePlayer::getFinishTimeMillis))
            .toList();
        for (int i = 0; i < ranking.size(); i++) {
            RacePlayer racePlayer = ranking.get(i);
            Player player = racePlayer.getPlayer();
            if (player != null) {
                messages.sendRaw(player, "&eRennen beendet. Platz " + (i + 1) + ": &f" + TimeUtil.formatMillis(racePlayer.getFinishTimeMillis()));
            }
        }
        cleanupRace(race);
        races.remove(race.getId());
    }

    public boolean cancelRace(Race race, String reason) {
        if (race.getState() != RaceState.CANCELLED && race.getState() != RaceState.FINISHED) {
            cancelledRacesSinceStartup++;
        }
        race.setState(RaceState.CANCELLED);
        for (RacePlayer racePlayer : race.getPlayers()) {
            Player player = racePlayer.getPlayer();
            if (player != null) {
                messages.sendRaw(player, "&cRennen abgebrochen: &f" + reason);
            }
        }
        cleanupRace(race);
        races.remove(race.getId());
        return true;
    }

    private void cleanupRace(Race race) {
        for (RacePlayer racePlayer : race.getPlayers()) {
            playerRaces.remove(racePlayer.getUuid());
            Player player = racePlayer.getPlayer();
            if (player != null && race.getTrack().getLobbyLocation() != null && plugin.getConfig().getBoolean("race.teleport-to-lobby-after-race", true)) {
                player.leaveVehicle();
                player.teleport(race.getTrack().getLobbyLocation());
            } else if (player != null) {
                player.leaveVehicle();
            }
        }
        if (plugin.getConfig().getBoolean("race.remove-boat-after-race", true)) {
            for (Boat boat : race.getBoats()) {
                if (!boat.isDead()) {
                    vehicleVisualManager.removeVisuals(boat);
                    boat.eject();
                    boat.remove();
                }
            }
        }
    }

    public Optional<Race> getRace(Player player) {
        UUID raceId = playerRaces.get(player.getUniqueId());
        return raceId == null ? Optional.empty() : Optional.ofNullable(races.get(raceId));
    }

    public Optional<Race> getRace(UUID raceId) {
        return Optional.ofNullable(races.get(raceId));
    }

    public Optional<Race> getRaceByShortIdOrTrack(String input) {
        for (Race race : races.values()) {
            if (race.getShortId().equalsIgnoreCase(input) || race.getId().toString().equalsIgnoreCase(input)) {
                return Optional.of(race);
            }
            if (race.getTrack().getId().equalsIgnoreCase(input)) {
                return Optional.of(race);
            }
        }
        return Optional.empty();
    }

    public Collection<Race> getRaces() {
        return races.values();
    }

    public boolean isActiveRacingBoat(org.bukkit.entity.Entity entity) {
        if (!racingBoatManager.isRacingBoat(entity)) {
            return false;
        }
        return racingBoatManager.getRaceId(entity)
            .map(races::get)
            .filter(race -> race.getState() == RaceState.RUNNING)
            .isPresent();
    }

    public void handleQuit(Player player) {
        getRace(player).ifPresent(race -> leaveRace(player));
    }

    private Optional<Race> findWaitingRace(Track track) {
        return races.values().stream()
            .filter(race -> race.getState() == RaceState.WAITING)
            .filter(race -> race.getTrack().getId().equals(track.getId()))
            .findFirst();
    }

    private void broadcastCountdown(Race race, int seconds) {
        for (RacePlayer racePlayer : race.getPlayers()) {
            Player player = racePlayer.getPlayer();
            if (player != null) {
                player.sendTitle(String.valueOf(seconds), "", 0, 20, 5);
                soundManager.playCountdown(player, seconds);
            }
        }
    }

    private void broadcastStart(Race race) {
        for (RacePlayer racePlayer : race.getPlayers()) {
            Player player = racePlayer.getPlayer();
            if (player != null) {
                player.sendTitle("GO", "", 0, 20, 5);
                soundManager.playStart(player);
            }
        }
    }

    private void report(String component, String action, RuntimeException ex, Map<String, Object> context) {
        if (crashReporter != null) {
            crashReporter.reportException(component, action, ex, context);
        } else {
            plugin.getLogger().warning(component + "#" + action + " failed: " + ex.getMessage());
        }
    }

    public long getStartedRacesSinceStartup() {
        return startedRacesSinceStartup;
    }

    public long getFinishedRacesSinceStartup() {
        return finishedRacesSinceStartup;
    }

    public long getCancelledRacesSinceStartup() {
        return cancelledRacesSinceStartup;
    }
}

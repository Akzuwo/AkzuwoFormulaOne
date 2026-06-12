package ch.akzuwo.akzuwoformulaone.listeners;

import ch.akzuwo.analytics.CrashReporter;
import ch.akzuwo.akzuwoformulaone.race.RaceManager;
import ch.akzuwo.akzuwoformulaone.resourcepack.ResourcePackManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerConnectionListener implements Listener {

    private final ResourcePackManager resourcePackManager;
    private final RaceManager raceManager;
    private final CrashReporter crashReporter;

    public PlayerConnectionListener(ResourcePackManager resourcePackManager, RaceManager raceManager, CrashReporter crashReporter) {
        this.resourcePackManager = resourcePackManager;
        this.raceManager = raceManager;
        this.crashReporter = crashReporter;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        try {
            resourcePackManager.sendPack(event.getPlayer());
        } catch (RuntimeException ex) {
            crashReporter.reportException("PlayerConnectionListener", "PlayerJoinEvent", ex);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        try {
            raceManager.handleQuit(event.getPlayer());
        } catch (RuntimeException ex) {
            crashReporter.reportException("PlayerConnectionListener", "PlayerQuitEvent", ex);
        }
    }
}

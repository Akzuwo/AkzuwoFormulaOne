package ch.akzuwo.formulaboatracing.listeners;

import ch.akzuwo.formulaboatracing.race.RaceManager;
import ch.akzuwo.formulaboatracing.resourcepack.ResourcePackManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerConnectionListener implements Listener {

    private final ResourcePackManager resourcePackManager;
    private final RaceManager raceManager;

    public PlayerConnectionListener(ResourcePackManager resourcePackManager, RaceManager raceManager) {
        this.resourcePackManager = resourcePackManager;
        this.raceManager = raceManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        resourcePackManager.sendPack(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        raceManager.handleQuit(event.getPlayer());
    }
}

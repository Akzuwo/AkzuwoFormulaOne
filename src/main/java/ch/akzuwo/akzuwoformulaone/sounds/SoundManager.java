package ch.akzuwo.akzuwoformulaone.sounds;

import ch.akzuwo.akzuwoformulaone.features.FeatureManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class SoundManager {

    private final JavaPlugin plugin;
    private final FeatureManager featureManager;
    private boolean enabled;
    private boolean useVanillaFallbackSounds;
    private String countdownSound;
    private String startSound;
    private String checkpointSound;
    private String finishSound;
    private boolean warnedAboutCustomSounds;

    public SoundManager(JavaPlugin plugin, FeatureManager featureManager) {
        this.plugin = plugin;
        this.featureManager = featureManager;
        reload();
    }

    public void reload() {
        this.enabled = plugin.getConfig().getBoolean("sounds.enabled", featureManager.isSoundsEnabled());
        this.useVanillaFallbackSounds = plugin.getConfig().getBoolean("sounds.use-vanilla-fallback-sounds", true);
        this.countdownSound = plugin.getConfig().getString("sounds.countdown-sound", Sound.BLOCK_NOTE_BLOCK_PLING.name());
        this.startSound = plugin.getConfig().getString("sounds.start-sound", Sound.ENTITY_PLAYER_LEVELUP.name());
        this.checkpointSound = plugin.getConfig().getString("sounds.checkpoint-sound", Sound.BLOCK_NOTE_BLOCK_BELL.name());
        this.finishSound = plugin.getConfig().getString("sounds.finish-sound", Sound.UI_TOAST_CHALLENGE_COMPLETE.name());
        this.warnedAboutCustomSounds = false;
    }

    public void playCountdown(Player player, int number) {
        play(player, countdownSound, Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, Math.max(0.5f, 1.2f + (number * 0.05f)));
    }

    public void playStart(Player player) {
        play(player, startSound, Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.0f);
    }

    public void playCheckpoint(Player player) {
        play(player, checkpointSound, Sound.BLOCK_NOTE_BLOCK_BELL, 0.6f, 1.4f);
    }

    public void playFinish(Player player) {
        play(player, finishSound, Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.0f);
    }

    public void playError(Player player) {
        if (useVanillaFallbackSounds) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.7f);
        }
    }

    public String statusSummary() {
        return "Sounds=" + status(featureManager.isSoundsEnabled() && enabled)
            + ", vanilla-fallback-sounds=" + useVanillaFallbackSounds
            + ", countdown=" + countdownSound
            + ", start=" + startSound
            + ", checkpoint=" + checkpointSound
            + ", finish=" + finishSound;
    }

    private void play(Player player, String configuredSound, Sound vanillaSound, float volume, float pitch) {
        if (featureManager.isSoundsEnabled() && enabled) {
            playConfigured(player, configuredSound, vanillaSound, volume, pitch);
            return;
        }
        if (useVanillaFallbackSounds) {
            player.playSound(player.getLocation(), vanillaSound, volume, pitch);
        }
    }

    private void playConfigured(Player player, String configuredSound, Sound vanillaSound, float volume, float pitch) {
        if (configuredSound == null || configuredSound.isBlank()) {
            warn("A configured sound is empty.");
            if (useVanillaFallbackSounds) {
                player.playSound(player.getLocation(), vanillaSound, volume, pitch);
            }
            return;
        }
        try {
            Sound sound = Sound.valueOf(configuredSound.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ex) {
            try {
                player.playSound(player.getLocation(), configuredSound.toLowerCase(), volume, pitch);
            } catch (RuntimeException runtimeException) {
                warn("Could not play configured sound '" + configuredSound + "': " + runtimeException.getMessage());
                if (useVanillaFallbackSounds) {
                    player.playSound(player.getLocation(), vanillaSound, volume, pitch);
                }
            }
        }
    }

    private void warn(String message) {
        if (!warnedAboutCustomSounds) {
            plugin.getLogger().warning(message + " Vanilla fallback sounds will be used when enabled.");
            warnedAboutCustomSounds = true;
        }
    }

    private String status(boolean active) {
        return active ? "enabled" : "disabled";
    }
}

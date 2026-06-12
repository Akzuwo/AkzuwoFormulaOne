package ch.akzuwo.akzuwoformulaone.resourcepack;

import ch.akzuwo.akzuwoformulaone.features.FeatureManager;
import ch.akzuwo.akzuwoformulaone.utils.MessageManager;
import java.util.HexFormat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class ResourcePackManager implements Listener {

    private final JavaPlugin plugin;
    private final FeatureManager featureManager;
    private final MessageManager messages;
    private String url;
    private String sha1;
    private boolean kickOnDecline;
    private String kickMessage;

    public ResourcePackManager(JavaPlugin plugin, FeatureManager featureManager, MessageManager messages) {
        this.plugin = plugin;
        this.featureManager = featureManager;
        this.messages = messages;
    }

    public void reload() {
        this.url = plugin.getConfig().getString("resourcepack.url", "");
        this.sha1 = plugin.getConfig().getString("resourcepack.sha1", "");
        this.kickOnDecline = plugin.getConfig().getBoolean("resourcepack.kick-on-decline", featureManager.isForceResourcepack());
        this.kickMessage = MessageManager.color(plugin.getConfig().getString(
            "resourcepack.kick-message",
            "Du musst das Resourcepack akzeptieren, um Formel-1-Rennen zu fahren."
        ));
        validate();
    }

    public void sendPack(Player player) {
        if (!featureManager.isResourcepackEnabled()) {
            return;
        }
        if (featureManager.isTestMode()) {
            debug("Test mode is enabled; skipping resourcepack for " + player.getName() + ".");
            return;
        }
        if (url == null || url.isBlank()) {
            return;
        }
        byte[] hash = parseSha1();
        try {
            if (hash.length == 20) {
                player.setResourcePack(url, hash, "AkzuwoFormulaOne Resourcepack", isForceActive());
            } else {
                player.setResourcePack(url);
            }
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Could not send resourcepack to " + player.getName() + ": " + ex.getMessage());
        }
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        if (!featureManager.isResourcepackEnabled() || !isForceActive() || featureManager.isTestMode()) {
            return;
        }
        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        if (status == PlayerResourcePackStatusEvent.Status.DECLINED || status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            event.getPlayer().kickPlayer(kickMessage);
        }
    }

    public String statusSummary() {
        return "Resourcepack=" + status(featureManager.isResourcepackEnabled())
            + ", force=" + isForceActive()
            + ", url=" + (url == null || url.isBlank() ? "empty" : "configured")
            + ", sha1=" + (sha1 == null || sha1.isBlank() ? "empty" : "configured")
            + ", test-mode=" + featureManager.isTestMode();
    }

    private void validate() {
        if (!featureManager.isResourcepackEnabled()) {
            return;
        }
        if (url == null || url.isBlank()) {
            if (featureManager.isVanillaFallbackEnabled() || featureManager.isTestMode()) {
                plugin.getLogger().warning("Resourcepack is enabled but resourcepack.url is empty. Continuing without sending a pack.");
            } else {
                plugin.getLogger().severe("Resourcepack is enabled but resourcepack.url is empty and vanilla fallback is disabled.");
            }
        }
    }

    private boolean isForceActive() {
        return featureManager.isForceResourcepack() || kickOnDecline;
    }

    private byte[] parseSha1() {
        if (sha1 == null || sha1.isBlank() || sha1.equalsIgnoreCase("PLACEHOLDER_SHA1")) {
            return new byte[0];
        }
        try {
            return HexFormat.of().parseHex(sha1.replace(" ", ""));
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Invalid resourcepack SHA1 in config.yml. Expected 40 hex characters.");
            return new byte[0];
        }
    }

    private void debug(String message) {
        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info(message);
        }
    }

    private String status(boolean enabled) {
        return enabled ? "enabled" : "disabled";
    }
}

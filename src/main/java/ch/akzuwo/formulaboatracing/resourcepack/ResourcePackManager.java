package ch.akzuwo.formulaboatracing.resourcepack;

import ch.akzuwo.formulaboatracing.utils.MessageManager;
import java.util.HexFormat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class ResourcePackManager implements Listener {

    private final JavaPlugin plugin;
    private final MessageManager messages;
    private boolean enabled;
    private String url;
    private String sha1;
    private boolean force;
    private String kickMessage;

    public ResourcePackManager(JavaPlugin plugin, MessageManager messages) {
        this.plugin = plugin;
        this.messages = messages;
    }

    public void reload() {
        this.enabled = plugin.getConfig().getBoolean("resourcepack.enabled", true);
        this.url = plugin.getConfig().getString("resourcepack.url", "");
        this.sha1 = plugin.getConfig().getString("resourcepack.sha1", "");
        this.force = plugin.getConfig().getBoolean("resourcepack.force", true);
        this.kickMessage = MessageManager.color(plugin.getConfig().getString(
            "resourcepack.kick-message",
            "Du musst das Resourcepack akzeptieren, um auf diesem Server zu spielen."
        ));
    }

    public void sendPack(Player player) {
        if (!enabled || url == null || url.isBlank() || url.contains("example.com")) {
            return;
        }
        byte[] hash = parseSha1();
        try {
            if (hash.length == 20) {
                player.setResourcePack(url, hash, "FormulaBoatRacing Resourcepack", force);
            } else {
                player.setResourcePack(url);
            }
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().warning("Could not send resourcepack to " + player.getName() + ": " + ex.getMessage());
        }
    }

    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        if (!enabled || !force) {
            return;
        }
        PlayerResourcePackStatusEvent.Status status = event.getStatus();
        if (status == PlayerResourcePackStatusEvent.Status.DECLINED || status == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            event.getPlayer().kickPlayer(kickMessage);
        }
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
}

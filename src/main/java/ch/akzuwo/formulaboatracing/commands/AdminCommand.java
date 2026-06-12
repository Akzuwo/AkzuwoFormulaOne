package ch.akzuwo.formulaboatracing.commands;

import ch.akzuwo.formulaboatracing.FormulaBoatRacingPlugin;
import ch.akzuwo.formulaboatracing.permissions.Permissions;
import ch.akzuwo.formulaboatracing.physics.BoatPhysicsManager;
import ch.akzuwo.formulaboatracing.resourcepack.ResourcePackManager;
import ch.akzuwo.formulaboatracing.storage.StorageManager;
import ch.akzuwo.formulaboatracing.track.Track;
import ch.akzuwo.formulaboatracing.track.TrackManager;
import ch.akzuwo.formulaboatracing.utils.MessageManager;
import java.io.IOException;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public final class AdminCommand implements CommandExecutor, TabCompleter {

    private final FormulaBoatRacingPlugin plugin;
    private final TrackManager trackManager;
    private final StorageManager storageManager;
    private final ResourcePackManager resourcePackManager;
    private final BoatPhysicsManager boatPhysicsManager;
    private final MessageManager messages;

    public AdminCommand(FormulaBoatRacingPlugin plugin, TrackManager trackManager, StorageManager storageManager, ResourcePackManager resourcePackManager, BoatPhysicsManager boatPhysicsManager, MessageManager messages) {
        this.plugin = plugin;
        this.trackManager = trackManager;
        this.storageManager = storageManager;
        this.resourcePackManager = resourcePackManager;
        this.boatPhysicsManager = boatPhysicsManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtil.require(sender, Permissions.ADMIN, messages)) {
            return true;
        }
        if (args.length == 0) {
            help(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (CommandUtil.require(sender, Permissions.ADMIN_RELOAD, messages)) {
                    plugin.reloadPlugin();
                    messages.sendRaw(sender, "&aFormulaBoatRacing neu geladen.");
                }
            }
            case "save" -> save(sender);
            case "debug" -> debug(sender);
            case "resourcepack" -> {
                if (args.length > 1 && args[1].equalsIgnoreCase("reload") && CommandUtil.require(sender, Permissions.ADMIN_RESOURCEPACK, messages)) {
                    resourcePackManager.reload();
                    messages.sendRaw(sender, "&aResourcepack-Konfiguration neu geladen.");
                } else {
                    messages.sendRaw(sender, "&cNutze: /f1admin resourcepack reload");
                }
            }
            case "physics" -> {
                if (args.length > 1 && args[1].equalsIgnoreCase("reload") && CommandUtil.require(sender, Permissions.ADMIN_PHYSICS, messages)) {
                    boatPhysicsManager.reload();
                    messages.sendRaw(sender, "&aPhysik-Konfiguration neu geladen.");
                } else {
                    messages.sendRaw(sender, "&cNutze: /f1admin physics reload");
                }
            }
            default -> help(sender);
        }
        return true;
    }

    private void help(CommandSender sender) {
        messages.sendRaw(sender, "&f/f1admin reload");
        messages.sendRaw(sender, "&f/f1admin save");
        messages.sendRaw(sender, "&f/f1admin debug");
        messages.sendRaw(sender, "&f/f1admin resourcepack reload");
        messages.sendRaw(sender, "&f/f1admin physics reload");
    }

    private void save(CommandSender sender) {
        if (!CommandUtil.require(sender, Permissions.ADMIN_RELOAD, messages)) {
            return;
        }
        int saved = 0;
        for (Track track : trackManager.getTracks()) {
            try {
                trackManager.save(track);
                saved++;
            } catch (IOException ex) {
                messages.sendRaw(sender, "&cTrack " + track.getId() + " konnte nicht gespeichert werden: " + ex.getMessage());
            }
        }
        storageManager.save();
        messages.sendRaw(sender, "&aGespeichert: &f" + saved + " Tracks &aund Zeiten.");
    }

    private void debug(CommandSender sender) {
        if (!CommandUtil.require(sender, Permissions.ADMIN_DEBUG, messages)) {
            return;
        }
        boolean newValue = !plugin.getConfig().getBoolean("debug", false);
        plugin.getConfig().set("debug", newValue);
        plugin.saveConfig();
        messages.sendRaw(sender, "&aDebug ist jetzt: &f" + newValue);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return CommandUtil.filter(List.of("reload", "save", "debug", "resourcepack", "physics"), args[0]);
        }
        if (args.length == 2 && List.of("resourcepack", "physics").contains(args[0].toLowerCase())) {
            return CommandUtil.filter(List.of("reload"), args[1]);
        }
        return List.of();
    }
}

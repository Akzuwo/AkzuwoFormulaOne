package ch.akzuwo.formulaboatracing.commands;

import ch.akzuwo.formulaboatracing.permissions.Permissions;
import ch.akzuwo.formulaboatracing.track.Track;
import ch.akzuwo.formulaboatracing.track.TrackCheckpoint;
import ch.akzuwo.formulaboatracing.track.TrackManager;
import ch.akzuwo.formulaboatracing.utils.MessageManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class BuilderCommand implements CommandExecutor, TabCompleter {

    private final TrackManager trackManager;
    private final MessageManager messages;

    public BuilderCommand(TrackManager trackManager, MessageManager messages) {
        this.trackManager = trackManager;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!CommandUtil.require(sender, Permissions.BUILDER, messages)) {
            return true;
        }
        if (args.length == 0) {
            help(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> create(sender, args);
            case "setworld" -> setWorld(sender, args);
            case "setlobby" -> setLocation(sender, args, "lobby");
            case "setspawn" -> setLocation(sender, args, "spawn");
            case "setstart" -> setLocation(sender, args, "start");
            case "setfinish" -> setLocation(sender, args, "finish");
            case "addcheckpoint" -> addCheckpoint(sender, args);
            case "removecheckpoint" -> removeCheckpoint(sender, args);
            case "setlaps" -> setLaps(sender, args);
            case "enable" -> setEnabled(sender, args, true);
            case "disable" -> setEnabled(sender, args, false);
            case "save" -> save(sender, args);
            case "reload" -> reload(sender, args);
            case "list" -> list(sender);
            default -> help(sender);
        }
        return true;
    }

    private void help(CommandSender sender) {
        messages.sendRaw(sender, "&f/f1builder create <trackId> <displayName>");
        messages.sendRaw(sender, "&f/f1builder setworld|setlobby|setspawn|setstart|setfinish <trackId>");
        messages.sendRaw(sender, "&f/f1builder addcheckpoint <trackId> <name> <radius>");
        messages.sendRaw(sender, "&f/f1builder setlaps <trackId> <laps>");
        messages.sendRaw(sender, "&f/f1builder enable|disable|save|reload|list");
    }

    private void create(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.BUILDER_CREATE, messages)) {
            return;
        }
        Player player = CommandUtil.requirePlayer(sender, messages);
        if (player == null) {
            return;
        }
        if (args.length < 3) {
            messages.sendRaw(sender, "&cNutze: /f1builder create <trackId> <displayName>");
            return;
        }
        String displayName = String.join(" ", List.of(args).subList(2, args.length));
        Track track = trackManager.create(args[1], displayName, player.getWorld().getName());
        messages.sendRaw(sender, "&aStrecke &f" + track.getId() + " &aerstellt.");
    }

    private void setWorld(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.BUILDER_EDIT, messages)) {
            return;
        }
        Player player = CommandUtil.requirePlayer(sender, messages);
        if (player == null) {
            return;
        }
        withTrack(sender, args, track -> {
            track.setWorldName(player.getWorld().getName());
            messages.sendRaw(sender, "&aWelt gesetzt: &f" + player.getWorld().getName());
        });
    }

    private void setLocation(CommandSender sender, String[] args, String type) {
        if (!CommandUtil.require(sender, Permissions.BUILDER_EDIT, messages)) {
            return;
        }
        Player player = CommandUtil.requirePlayer(sender, messages);
        if (player == null) {
            return;
        }
        withTrack(sender, args, track -> {
            Location location = player.getLocation();
            switch (type) {
                case "lobby" -> track.setLobbyLocation(location);
                case "spawn" -> track.setSpawnLocation(location);
                case "start" -> track.setStartLine(location);
                case "finish" -> track.setFinishLine(location);
                default -> {
                    return;
                }
            }
            messages.sendRaw(sender, "&a" + type + " gesetzt.");
        });
    }

    private void addCheckpoint(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.BUILDER_CHECKPOINT, messages)) {
            return;
        }
        Player player = CommandUtil.requirePlayer(sender, messages);
        if (player == null) {
            return;
        }
        if (args.length < 4) {
            messages.sendRaw(sender, "&cNutze: /f1builder addcheckpoint <trackId> <name> <radius>");
            return;
        }
        withTrack(sender, args, track -> {
            try {
                double radius = Double.parseDouble(args[3]);
                track.addCheckpoint(new TrackCheckpoint(args[2], player.getLocation(), radius));
                messages.sendRaw(sender, "&aCheckpoint &f" + args[2] + " &ahinzugefügt.");
            } catch (NumberFormatException ex) {
                messages.sendRaw(sender, "&cRadius muss eine Zahl sein.");
            }
        });
    }

    private void removeCheckpoint(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.BUILDER_CHECKPOINT, messages)) {
            return;
        }
        if (args.length < 3) {
            messages.sendRaw(sender, "&cNutze: /f1builder removecheckpoint <trackId> <name>");
            return;
        }
        withTrack(sender, args, track -> messages.sendRaw(sender,
            track.removeCheckpoint(args[2]) ? "&aCheckpoint entfernt." : "&cCheckpoint nicht gefunden."));
    }

    private void setLaps(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.BUILDER_EDIT, messages)) {
            return;
        }
        if (args.length < 3) {
            messages.sendRaw(sender, "&cNutze: /f1builder setlaps <trackId> <laps>");
            return;
        }
        withTrack(sender, args, track -> {
            try {
                track.setLaps(Math.max(1, Integer.parseInt(args[2])));
                messages.sendRaw(sender, "&aRunden gesetzt.");
            } catch (NumberFormatException ex) {
                messages.sendRaw(sender, "&cRunden müssen eine Zahl sein.");
            }
        });
    }

    private void setEnabled(CommandSender sender, String[] args, boolean enabled) {
        if (!CommandUtil.require(sender, Permissions.BUILDER_ENABLE, messages)) {
            return;
        }
        withTrack(sender, args, track -> {
            track.setEnabled(enabled);
            messages.sendRaw(sender, enabled ? "&aStrecke aktiviert." : "&eStrecke deaktiviert.");
        });
    }

    private void save(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.BUILDER_SAVE, messages)) {
            return;
        }
        withTrack(sender, args, track -> {
            try {
                trackManager.save(track);
                messages.sendRaw(sender, "&aStrecke gespeichert.");
            } catch (IOException ex) {
                messages.sendRaw(sender, "&cSpeichern fehlgeschlagen: " + ex.getMessage());
            }
        });
    }

    private void reload(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.BUILDER_SAVE, messages)) {
            return;
        }
        if (args.length < 2) {
            messages.sendRaw(sender, "&cNutze: /f1builder reload <trackId>");
            return;
        }
        messages.sendRaw(sender, trackManager.reload(args[1]).isPresent() ? "&aStrecke neu geladen." : "&cStrecke nicht gefunden.");
    }

    private void list(CommandSender sender) {
        messages.sendRaw(sender, "&aTracks:");
        for (Track track : trackManager.getTracks()) {
            messages.sendRaw(sender, "&7- &f" + track.getId() + " &7enabled=" + track.isEnabled() + " usable=" + track.isUsable());
        }
    }

    private void withTrack(CommandSender sender, String[] args, java.util.function.Consumer<Track> consumer) {
        if (args.length < 2) {
            messages.sendRaw(sender, "&cTrack-ID fehlt.");
            return;
        }
        Optional<Track> track = trackManager.getTrack(args[1]);
        if (track.isEmpty()) {
            messages.sendRaw(sender, "&cStrecke nicht gefunden.");
            return;
        }
        consumer.accept(track.get());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return CommandUtil.filter(List.of("create", "setworld", "setlobby", "setspawn", "setstart", "setfinish", "addcheckpoint", "removecheckpoint", "setlaps", "enable", "disable", "save", "reload", "list"), args[0]);
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("create")) {
            return CommandUtil.filter(new ArrayList<>(trackManager.getTrackIds()), args[1]);
        }
        return List.of();
    }
}

package ch.akzuwo.akzuwoformulaone.commands;

import ch.akzuwo.analytics.CrashReporter;
import ch.akzuwo.akzuwoformulaone.permissions.Permissions;
import ch.akzuwo.akzuwoformulaone.race.Race;
import ch.akzuwo.akzuwoformulaone.race.RaceManager;
import ch.akzuwo.akzuwoformulaone.track.Track;
import ch.akzuwo.akzuwoformulaone.track.TrackManager;
import ch.akzuwo.akzuwoformulaone.utils.MessageManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class ModCommand implements CommandExecutor, TabCompleter {

    private final TrackManager trackManager;
    private final RaceManager raceManager;
    private final MessageManager messages;
    private final CrashReporter crashReporter;

    public ModCommand(TrackManager trackManager, RaceManager raceManager, MessageManager messages, CrashReporter crashReporter) {
        this.trackManager = trackManager;
        this.raceManager = raceManager;
        this.messages = messages;
        this.crashReporter = crashReporter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            return handleCommand(sender, args);
        } catch (RuntimeException ex) {
            crashReporter.reportException("ModCommand", args.length == 0 ? "help" : args[0], ex);
            messages.sendRaw(sender, "&cEin interner Fehler ist aufgetreten.");
            return true;
        }
    }

    private boolean handleCommand(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.MOD, messages)) {
            return true;
        }
        if (args.length == 0) {
            help(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "cancel" -> cancel(sender, args);
            case "kick" -> kick(sender, args);
            case "tp" -> tp(sender, args);
            case "info" -> info(sender, args);
            default -> help(sender);
        }
        return true;
    }

    private void help(CommandSender sender) {
        messages.sendRaw(sender, "&f/f1mod cancel <trackId|raceId>");
        messages.sendRaw(sender, "&f/f1mod kick <player>");
        messages.sendRaw(sender, "&f/f1mod tp <trackId>");
        messages.sendRaw(sender, "&f/f1mod info <trackId|raceId>");
    }

    private void cancel(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.MOD_CANCEL, messages)) {
            return;
        }
        if (args.length < 2) {
            messages.sendRaw(sender, "&cNutze: /f1mod cancel <trackId|raceId>");
            return;
        }
        Optional<Race> race = raceManager.getRaceByShortIdOrTrack(args[1]);
        if (race.isEmpty()) {
            messages.sendRaw(sender, "&cRennen nicht gefunden.");
            return;
        }
        raceManager.cancelRace(race.get(), "Abbruch durch Moderation");
        messages.sendRaw(sender, "&aRennen abgebrochen.");
    }

    private void kick(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.MOD_KICK, messages)) {
            return;
        }
        if (args.length < 2) {
            messages.sendRaw(sender, "&cNutze: /f1mod kick <player>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            messages.sendRaw(sender, "&cSpieler nicht gefunden.");
            return;
        }
        raceManager.leaveRace(target);
        messages.sendRaw(sender, "&aSpieler aus dem Rennen entfernt.");
    }

    private void tp(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.MOD_TP, messages)) {
            return;
        }
        Player player = CommandUtil.requirePlayer(sender, messages);
        if (player == null) {
            return;
        }
        if (args.length < 2) {
            messages.sendRaw(sender, "&cNutze: /f1mod tp <trackId>");
            return;
        }
        Optional<Track> track = trackManager.getTrack(args[1]);
        if (track.isEmpty() || track.get().getLobbyLocation() == null) {
            messages.sendRaw(sender, "&cStrecke oder Lobby nicht gefunden.");
            return;
        }
        player.teleport(track.get().getLobbyLocation());
        messages.sendRaw(sender, "&aTeleportiert.");
    }

    private void info(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.MOD_INFO, messages)) {
            return;
        }
        if (args.length < 2) {
            messages.sendRaw(sender, "&cNutze: /f1mod info <trackId|raceId>");
            return;
        }
        raceManager.getRaceByShortIdOrTrack(args[1]).ifPresentOrElse(race -> {
            messages.sendRaw(sender, "&aRace &f" + race.getShortId() + " &7Track=" + race.getTrack().getId()
                + " State=" + race.getState() + " Type=" + race.getType() + " Players=" + race.getPlayerCount());
        }, () -> trackManager.getTrack(args[1]).ifPresentOrElse(track -> {
            messages.sendRaw(sender, "&aTrack &f" + track.getId() + " &7World=" + track.getWorldName()
                + " Enabled=" + track.isEnabled() + " Usable=" + track.isUsable() + " Laps=" + track.getLaps());
        }, () -> messages.sendRaw(sender, "&cNichts gefunden.")));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return CommandUtil.filter(List.of("cancel", "kick", "tp", "info"), args[0]);
        }
        if (args.length == 2 && List.of("cancel", "tp", "info").contains(args[0].toLowerCase())) {
            List<String> values = new ArrayList<>(trackManager.getTrackIds());
            values.addAll(raceManager.getRaces().stream().map(Race::getShortId).toList());
            return CommandUtil.filter(values, args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("kick")) {
            return CommandUtil.filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[1]);
        }
        return List.of();
    }
}

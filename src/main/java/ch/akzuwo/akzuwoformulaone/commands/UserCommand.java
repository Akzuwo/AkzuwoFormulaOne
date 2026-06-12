package ch.akzuwo.akzuwoformulaone.commands;

import ch.akzuwo.analytics.CrashReporter;
import ch.akzuwo.akzuwoformulaone.permissions.Permissions;
import ch.akzuwo.akzuwoformulaone.race.RaceManager;
import ch.akzuwo.akzuwoformulaone.storage.StorageManager;
import ch.akzuwo.akzuwoformulaone.storage.StoredTime;
import ch.akzuwo.akzuwoformulaone.track.Track;
import ch.akzuwo.akzuwoformulaone.track.TrackManager;
import ch.akzuwo.akzuwoformulaone.utils.MessageManager;
import ch.akzuwo.akzuwoformulaone.utils.TimeUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class UserCommand implements CommandExecutor, TabCompleter {

    private final TrackManager trackManager;
    private final RaceManager raceManager;
    private final StorageManager storageManager;
    private final MessageManager messages;
    private final CrashReporter crashReporter;

    public UserCommand(TrackManager trackManager, RaceManager raceManager, StorageManager storageManager, MessageManager messages, CrashReporter crashReporter) {
        this.trackManager = trackManager;
        this.raceManager = raceManager;
        this.storageManager = storageManager;
        this.messages = messages;
        this.crashReporter = crashReporter;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            return handleCommand(sender, args);
        } catch (RuntimeException ex) {
            crashReporter.reportException("UserCommand", args.length == 0 ? "help" : args[0], ex);
            messages.sendRaw(sender, "&cEin interner Fehler ist aufgetreten.");
            return true;
        }
    }

    private boolean handleCommand(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.USER, messages)) {
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "tracks" -> tracks(sender);
            case "join" -> join(sender, args);
            case "leave" -> leave(sender);
            case "start" -> start(sender);
            case "best" -> best(sender, args);
            case "leaderboard" -> leaderboard(sender, args);
            case "spectate" -> messages.sendRaw(sender, "&eSpectator-System ist vorbereitet, aber noch nicht vollständig implementiert.");
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        messages.sendRaw(sender, "&f/f1 tracks &7- Strecken anzeigen");
        messages.sendRaw(sender, "&f/f1 join <trackId> &7- Rennen beitreten");
        messages.sendRaw(sender, "&f/f1 leave &7- Rennen verlassen");
        messages.sendRaw(sender, "&f/f1 start &7- Rennen starten");
        messages.sendRaw(sender, "&f/f1 best <trackId> &7- Eigene Bestzeit");
        messages.sendRaw(sender, "&f/f1 leaderboard <trackId> &7- Bestenliste");
    }

    private void tracks(CommandSender sender) {
        List<Track> usable = trackManager.getTracks().stream().filter(Track::isUsable).toList();
        if (usable.isEmpty()) {
            messages.sendRaw(sender, "&eKeine spielbaren Strecken gefunden.");
            return;
        }
        messages.sendRaw(sender, "&aVerfügbare Strecken:");
        for (Track track : usable) {
            messages.sendRaw(sender, "&7- &f" + track.getId() + " &7(" + track.getDisplayName() + ", " + track.getLaps() + " Runden)");
        }
    }

    private void join(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.USER_JOIN, messages)) {
            return;
        }
        Player player = CommandUtil.requirePlayer(sender, messages);
        if (player == null) {
            return;
        }
        if (args.length < 2) {
            messages.sendRaw(sender, "&cNutze: /f1 join <trackId>");
            return;
        }
        Optional<Track> track = trackManager.getTrack(args[1]);
        if (track.isEmpty()) {
            messages.sendRaw(sender, "&cUnbekannte Strecke.");
            return;
        }
        raceManager.joinRace(player, track.get());
    }

    private void leave(CommandSender sender) {
        if (!CommandUtil.require(sender, Permissions.USER_LEAVE, messages)) {
            return;
        }
        Player player = CommandUtil.requirePlayer(sender, messages);
        if (player != null) {
            raceManager.leaveRace(player);
        }
    }

    private void start(CommandSender sender) {
        if (!CommandUtil.require(sender, Permissions.USER_START, messages)) {
            return;
        }
        Player player = CommandUtil.requirePlayer(sender, messages);
        if (player != null) {
            raceManager.startRace(player);
        }
    }

    private void best(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.USER_LEADERBOARD, messages)) {
            return;
        }
        Player player = CommandUtil.requirePlayer(sender, messages);
        if (player == null) {
            return;
        }
        if (args.length < 2) {
            messages.sendRaw(sender, "&cNutze: /f1 best <trackId>");
            return;
        }
        Optional<StoredTime> best = storageManager.getBestTime(player.getUniqueId(), args[1].toLowerCase());
        messages.sendRaw(sender, best
            .map(time -> "&aDeine Bestzeit: &f" + TimeUtil.formatMillis(time.bestTimeMillis()))
            .orElse("&eFür diese Strecke hast du noch keine Bestzeit."));
    }

    private void leaderboard(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.USER_LEADERBOARD, messages)) {
            return;
        }
        if (args.length < 2) {
            messages.sendRaw(sender, "&cNutze: /f1 leaderboard <trackId>");
            return;
        }
        List<StoredTime> top = storageManager.getLeaderboard(args[1].toLowerCase(), 10);
        if (top.isEmpty()) {
            messages.sendRaw(sender, "&eNoch keine Zeiten gespeichert.");
            return;
        }
        messages.sendRaw(sender, "&aTop-Zeiten für &f" + args[1].toLowerCase() + "&a:");
        for (int i = 0; i < top.size(); i++) {
            StoredTime time = top.get(i);
            messages.sendRaw(sender, "&7" + (i + 1) + ". &f" + time.playerName() + " &7- &f" + TimeUtil.formatMillis(time.bestTimeMillis()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return CommandUtil.filter(List.of("tracks", "join", "leave", "start", "best", "leaderboard", "spectate"), args[0]);
        }
        if (args.length == 2 && List.of("join", "best", "leaderboard", "spectate").contains(args[0].toLowerCase())) {
            return CommandUtil.filter(new ArrayList<>(trackManager.getTrackIds()), args[1]);
        }
        return List.of();
    }
}

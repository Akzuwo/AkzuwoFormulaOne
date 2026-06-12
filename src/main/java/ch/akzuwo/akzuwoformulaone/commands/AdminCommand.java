package ch.akzuwo.akzuwoformulaone.commands;

import ch.akzuwo.analytics.AnalyticsConstants;
import ch.akzuwo.analytics.AnalyticsHandler;
import ch.akzuwo.analytics.CrashReporter;
import ch.akzuwo.akzuwoformulaone.AkzuwoFormulaOnePlugin;
import ch.akzuwo.akzuwoformulaone.features.FeatureManager;
import ch.akzuwo.akzuwoformulaone.permissions.Permissions;
import ch.akzuwo.akzuwoformulaone.physics.BoatPhysicsManager;
import ch.akzuwo.akzuwoformulaone.resourcepack.ResourcePackManager;
import ch.akzuwo.akzuwoformulaone.sounds.SoundManager;
import ch.akzuwo.akzuwoformulaone.storage.StorageManager;
import ch.akzuwo.akzuwoformulaone.track.Track;
import ch.akzuwo.akzuwoformulaone.track.TrackManager;
import ch.akzuwo.akzuwoformulaone.utils.MessageManager;
import ch.akzuwo.akzuwoformulaone.visuals.VehicleVisualManager;
import ch.ksrminecraft.points.PointsRewardService;
import ch.ksrminecraft.points.RankPointsHook;
import java.io.IOException;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public final class AdminCommand implements CommandExecutor, TabCompleter {

    private final AkzuwoFormulaOnePlugin plugin;
    private final TrackManager trackManager;
    private final StorageManager storageManager;
    private final FeatureManager featureManager;
    private final ResourcePackManager resourcePackManager;
    private final VehicleVisualManager vehicleVisualManager;
    private final SoundManager soundManager;
    private final BoatPhysicsManager boatPhysicsManager;
    private final AnalyticsHandler analyticsHandler;
    private final CrashReporter crashReporter;
    private final RankPointsHook rankPointsHook;
    private final PointsRewardService pointsRewardService;
    private final MessageManager messages;

    public AdminCommand(AkzuwoFormulaOnePlugin plugin, TrackManager trackManager, StorageManager storageManager, FeatureManager featureManager, ResourcePackManager resourcePackManager, VehicleVisualManager vehicleVisualManager, SoundManager soundManager, BoatPhysicsManager boatPhysicsManager, AnalyticsHandler analyticsHandler, CrashReporter crashReporter, RankPointsHook rankPointsHook, PointsRewardService pointsRewardService, MessageManager messages) {
        this.plugin = plugin;
        this.trackManager = trackManager;
        this.storageManager = storageManager;
        this.featureManager = featureManager;
        this.resourcePackManager = resourcePackManager;
        this.vehicleVisualManager = vehicleVisualManager;
        this.soundManager = soundManager;
        this.boatPhysicsManager = boatPhysicsManager;
        this.analyticsHandler = analyticsHandler;
        this.crashReporter = crashReporter;
        this.rankPointsHook = rankPointsHook;
        this.pointsRewardService = pointsRewardService;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            return handleCommand(sender, args);
        } catch (RuntimeException ex) {
            crashReporter.reportException("AdminCommand", args.length == 0 ? "help" : args[0], ex);
            messages.sendRaw(sender, "&cEin interner Fehler ist aufgetreten.");
            return true;
        }
    }

    private boolean handleCommand(CommandSender sender, String[] args) {
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
                    messages.sendRaw(sender, "&aAkzuwoFormulaOne neu geladen.");
                }
            }
            case "save" -> save(sender);
            case "debug" -> debug(sender);
            case "features" -> features(sender);
            case "resourcepack" -> {
                if (!CommandUtil.require(sender, Permissions.ADMIN_RESOURCEPACK, messages)) {
                    return true;
                }
                if (args.length > 1 && args[1].equalsIgnoreCase("reload")) {
                    resourcePackManager.reload();
                    messages.sendRaw(sender, "&aResourcepack-Konfiguration neu geladen.");
                } else {
                    messages.sendRaw(sender, "&a" + resourcePackManager.statusSummary());
                }
            }
            case "visuals" -> visuals(sender);
            case "sounds" -> sounds(sender);
            case "analytics" -> analytics(sender, args);
            case "points" -> points(sender, args);
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
        messages.sendRaw(sender, "&f/f1admin features");
        messages.sendRaw(sender, "&f/f1admin resourcepack");
        messages.sendRaw(sender, "&f/f1admin visuals");
        messages.sendRaw(sender, "&f/f1admin sounds");
        messages.sendRaw(sender, "&f/f1admin analytics");
        messages.sendRaw(sender, "&f/f1admin analytics test");
        messages.sendRaw(sender, "&f/f1admin analytics testcrash");
        messages.sendRaw(sender, "&f/f1admin points");
        messages.sendRaw(sender, "&f/f1admin points reload");
        messages.sendRaw(sender, "&f/f1admin points test <player> <amount>");
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

    private void features(CommandSender sender) {
        messages.sendRaw(sender, "&aFeature status:");
        messages.sendRaw(sender, "&7- Test mode: &f" + enabled(featureManager.isTestMode()));
        messages.sendRaw(sender, "&7- Resourcepack: &f" + enabled(featureManager.isResourcepackEnabled()));
        messages.sendRaw(sender, "&7- Force resourcepack: &f" + enabled(featureManager.isForceResourcepack()));
        messages.sendRaw(sender, "&7- Custom model: &f" + enabled(featureManager.isCustomModelEnabled()));
        messages.sendRaw(sender, "&7- Sounds: &f" + enabled(featureManager.isSoundsEnabled()));
        messages.sendRaw(sender, "&7- Vanilla fallback: &f" + enabled(featureManager.isVanillaFallbackEnabled()));
    }

    private void visuals(CommandSender sender) {
        messages.sendRaw(sender, "&a" + vehicleVisualManager.statusSummary());
    }

    private void sounds(CommandSender sender) {
        messages.sendRaw(sender, "&a" + soundManager.statusSummary());
    }

    private void analytics(CommandSender sender, String[] args) {
        if (args.length > 1 && args[1].equalsIgnoreCase("test")) {
            if (!CommandUtil.require(sender, Permissions.ADMIN_ANALYTICS, messages)) {
                return;
            }
            analyticsHandler.sendTestReport();
            messages.sendRaw(sender, "&aAnalytics-Testpayload wurde verarbeitet. Details stehen in der Konsole.");
            return;
        }
        if (args.length > 1 && args[1].equalsIgnoreCase("testcrash")) {
            if (!CommandUtil.require(sender, Permissions.ADMIN_ANALYTICS_TESTCRASH, messages)) {
                return;
            }
            crashReporter.reportException("AdminCommand", "analytics-testcrash", new RuntimeException("Manual analytics test exception"));
            messages.sendRaw(sender, "&aTest-Crashreport wurde verarbeitet. Der Server wurde nicht gecrasht.");
            return;
        }
        messages.sendRaw(sender, "&aAnalytics Status:");
        messages.sendRaw(sender, "&7- Analytics aktiviert: &f" + yesNo(analyticsHandler.getConfig().isSendAnalyticsData()));
        messages.sendRaw(sender, "&7- Crashreporting aktiv: &f" + yesNo(analyticsHandler.getConfig().isCrashReportingEnabled()));
        messages.sendRaw(sender, "&7- Recent Plugin Logs: &f" + enabled(analyticsHandler.getConfig().isCrashIncludeRecentPluginLogs()));
        messages.sendRaw(sender, "&7- Recent Log Limit: &f" + analyticsHandler.getConfig().getCrashRecentLogLimit());
        messages.sendRaw(sender, "&7- Rate Limit: &f" + analyticsHandler.getConfig().getCrashRateLimitSeconds() + "s");
        messages.sendRaw(sender, "&7- Offizieller Endpoint: &f" + AnalyticsConstants.OFFICIAL_ANALYTICS_ENDPOINT);
        messages.sendRaw(sender, "&7- Custom Copy Endpoint: &f" + enabled(analyticsHandler.getConfig().isCustomCopyEnabled()));
        messages.sendRaw(sender, "&7- Local Copy: &f" + enabled(analyticsHandler.getConfig().isLocalCopyEnabled()));
        messages.sendRaw(sender, "&7- Timeout: &f" + analyticsHandler.getConfig().getTimeoutMs() + "ms");
        messages.sendRaw(sender, "&eHinweis: Der offizielle Endpoint ist fest im Plugin hinterlegt und kann nicht über die Config geändert werden.");
    }

    private void points(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.ADMIN_POINTS, messages)) {
            return;
        }
        if (args.length > 1 && args[1].equalsIgnoreCase("reload")) {
            plugin.reloadRankPoints();
            messages.sendRaw(sender, "&aRankPoints-Konfiguration neu geladen und Service neu gesucht.");
            return;
        }
        if (args.length > 1 && args[1].equalsIgnoreCase("test")) {
            pointsTest(sender, args);
            return;
        }

        messages.sendRaw(sender, "&aRankPoints Status:");
        messages.sendRaw(sender, "&7- RankPoints-Integration aktiviert: &f" + yesNo(pointsRewardService.getConfig().isEnabled()));
        messages.sendRaw(sender, "&7- RankPointsAPI Plugin gefunden: &f" + yesNo(rankPointsHook.isPluginPresent()));
        messages.sendRaw(sender, "&7- RankPointsService verfügbar: &f" + yesNo(rankPointsHook.isAvailable()));
        messages.sendRaw(sender, "&7- Rewards aktiviert: &f" + yesNo(pointsRewardService.getConfig().isRewardsEnabled()));
        messages.sendRaw(sender, "&7- Base Points: &f" + pointsRewardService.getConfig().getFinishRaceBasePoints());
        messages.sendRaw(sender, "&7- Personal Best Bonus: &f" + pointsRewardService.getConfig().getPersonalBestBonusPoints());
        messages.sendRaw(sender, "&7- Track Record Bonus: &f" + pointsRewardService.getConfig().getTrackRecordBonusPoints());
        messages.sendRaw(sender, "&7- Default Track Multiplier: &f" + pointsRewardService.getConfig().getDefaultTrackMultiplier());
        messages.sendRaw(sender, "&7- Cooldown aktiv: &f" + yesNo(pointsRewardService.getConfig().isCooldownEnabled()));
        messages.sendRaw(sender, "&7- Cooldown Sekunden: &f" + pointsRewardService.getConfig().getCooldownSeconds());
    }

    private void pointsTest(CommandSender sender, String[] args) {
        if (!CommandUtil.require(sender, Permissions.ADMIN_POINTS_TEST, messages)) {
            return;
        }
        if (args.length < 4) {
            messages.sendRaw(sender, "&cNutze: /f1admin points test <player> <amount>");
            return;
        }
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            messages.sendRaw(sender, "&cSpieler nicht gefunden.");
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException ex) {
            messages.sendRaw(sender, "&cAmount muss eine positive Zahl sein.");
            return;
        }
        if (amount <= 0) {
            messages.sendRaw(sender, "&cAmount muss positiv sein.");
            return;
        }
        if (!rankPointsHook.isAvailable()) {
            messages.sendRaw(sender, "&cRankPointsService ist nicht verfügbar.");
            return;
        }
        if (rankPointsHook.addPoints(target.getUniqueId(), amount)) {
            messages.sendRaw(sender, "&aTestpunkte vergeben.");
        } else {
            messages.sendRaw(sender, "&cTestpunkte konnten nicht vergeben werden.");
        }
    }

    private String enabled(boolean value) {
        return value ? "enabled" : "disabled";
    }

    private String yesNo(boolean value) {
        return value ? "ja" : "nein";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return CommandUtil.filter(List.of("reload", "save", "debug", "features", "resourcepack", "visuals", "sounds", "analytics", "points", "physics"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("analytics")) {
            return CommandUtil.filter(List.of("test", "testcrash"), args[1]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("points")) {
            return CommandUtil.filter(List.of("reload", "test"), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("points") && args[1].equalsIgnoreCase("test")) {
            return CommandUtil.filter(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), args[2]);
        }
        if (args.length == 2 && List.of("resourcepack", "physics").contains(args[0].toLowerCase())) {
            return CommandUtil.filter(List.of("reload"), args[1]);
        }
        return List.of();
    }
}

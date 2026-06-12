package ch.akzuwo.akzuwoformulaone;

import ch.akzuwo.analytics.AnalyticsHandler;
import ch.akzuwo.analytics.CrashReporter;
import ch.akzuwo.analytics.PluginLogBuffer;
import ch.akzuwo.akzuwoformulaone.commands.AdminCommand;
import ch.akzuwo.akzuwoformulaone.commands.BuilderCommand;
import ch.akzuwo.akzuwoformulaone.commands.ModCommand;
import ch.akzuwo.akzuwoformulaone.commands.UserCommand;
import ch.akzuwo.akzuwoformulaone.features.FeatureManager;
import ch.akzuwo.akzuwoformulaone.listeners.PlayerConnectionListener;
import ch.akzuwo.akzuwoformulaone.physics.BoatPhysicsManager;
import ch.akzuwo.akzuwoformulaone.race.RaceManager;
import ch.akzuwo.akzuwoformulaone.race.RacingBoatManager;
import ch.akzuwo.akzuwoformulaone.resourcepack.ResourcePackManager;
import ch.akzuwo.akzuwoformulaone.sounds.SoundManager;
import ch.akzuwo.akzuwoformulaone.storage.StorageManager;
import ch.akzuwo.akzuwoformulaone.storage.YamlStorageManager;
import ch.akzuwo.akzuwoformulaone.track.TrackManager;
import ch.akzuwo.akzuwoformulaone.utils.MessageManager;
import ch.akzuwo.akzuwoformulaone.visuals.VehicleVisualManager;
import ch.ksrminecraft.points.PointsRewardService;
import ch.ksrminecraft.points.RankPointsConfig;
import ch.ksrminecraft.points.RankPointsHook;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

public final class AkzuwoFormulaOnePlugin extends JavaPlugin {

    private MessageManager messageManager;
    private FeatureManager featureManager;
    private ResourcePackManager resourcePackManager;
    private VehicleVisualManager vehicleVisualManager;
    private SoundManager soundManager;
    private RacingBoatManager racingBoatManager;
    private TrackManager trackManager;
    private StorageManager storageManager;
    private RaceManager raceManager;
    private BoatPhysicsManager boatPhysicsManager;
    private AnalyticsHandler analyticsHandler;
    private PluginLogBuffer pluginLogBuffer;
    private CrashReporter crashReporter;
    private RankPointsConfig rankPointsConfig;
    private RankPointsHook rankPointsHook;
    private PointsRewardService pointsRewardService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("messages.yml");

        this.messageManager = new MessageManager(this);
        this.messageManager.reload();

        this.featureManager = new FeatureManager(this);
        this.featureManager.reload();
        this.featureManager.validateFeatureSetup();
        this.featureManager.printFeatureStatusToConsole();

        this.resourcePackManager = new ResourcePackManager(this, featureManager, messageManager);
        this.vehicleVisualManager = new VehicleVisualManager(this, featureManager);
        this.soundManager = new SoundManager(this, featureManager);
        this.racingBoatManager = new RacingBoatManager(this);
        this.trackManager = new TrackManager(this);
        this.storageManager = new YamlStorageManager(this);
        this.rankPointsConfig = RankPointsConfig.from(this);
        this.rankPointsHook = new RankPointsHook(this, rankPointsConfig);
        this.rankPointsHook.initialize();
        this.pointsRewardService = new PointsRewardService(this, rankPointsHook, rankPointsConfig, messageManager);
        this.raceManager = new RaceManager(this, trackManager, storageManager, racingBoatManager, vehicleVisualManager, soundManager, pointsRewardService, messageManager);
        this.analyticsHandler = new AnalyticsHandler(this, featureManager, trackManager, raceManager);
        this.pluginLogBuffer = new PluginLogBuffer(analyticsHandler.getConfig().getCrashRecentLogLimit());
        this.crashReporter = new CrashReporter(this, featureManager, analyticsHandler, pluginLogBuffer);
        this.raceManager.setCrashReporter(crashReporter);
        this.boatPhysicsManager = new BoatPhysicsManager(this, racingBoatManager, raceManager, crashReporter);

        trackManager.loadAll();
        storageManager.load();
        analyticsHandler.reload();
        crashReporter.reload(analyticsHandler.getConfig());
        analyticsHandler.printStartupStatus();
        resourcePackManager.reload();
        vehicleVisualManager.reload();
        soundManager.reload();
        boatPhysicsManager.reload();
        raceManager.startTicker();

        registerCommands();
        registerListeners();

        getLogger().info("AkzuwoFormulaOne enabled with " + trackManager.getTracks().size() + " loaded track(s).");
    }

    @Override
    public void onDisable() {
        if (raceManager != null) {
            raceManager.shutdown();
        }
        if (storageManager != null) {
            storageManager.save();
        }
        if (analyticsHandler != null) {
            analyticsHandler.sendShutdownReport();
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        messageManager.reload();
        featureManager.reload();
        featureManager.validateFeatureSetup();
        featureManager.printFeatureStatusToConsole();
        resourcePackManager.reload();
        vehicleVisualManager.reload();
        soundManager.reload();
        boatPhysicsManager.reload();
        reloadRankPoints();
        analyticsHandler.reload();
        crashReporter.reload(analyticsHandler.getConfig());
        analyticsHandler.printStartupStatus();
        trackManager.loadAll();
        storageManager.load();
    }

    private void registerCommands() {
        UserCommand userCommand = new UserCommand(trackManager, raceManager, storageManager, messageManager, crashReporter);
        BuilderCommand builderCommand = new BuilderCommand(trackManager, messageManager, crashReporter);
        ModCommand modCommand = new ModCommand(trackManager, raceManager, messageManager, crashReporter);
        AdminCommand adminCommand = new AdminCommand(this, trackManager, storageManager, featureManager, resourcePackManager, vehicleVisualManager, soundManager, boatPhysicsManager, analyticsHandler, crashReporter, rankPointsHook, pointsRewardService, messageManager);

        Objects.requireNonNull(getCommand("f1")).setExecutor(userCommand);
        Objects.requireNonNull(getCommand("f1")).setTabCompleter(userCommand);
        Objects.requireNonNull(getCommand("f1builder")).setExecutor(builderCommand);
        Objects.requireNonNull(getCommand("f1builder")).setTabCompleter(builderCommand);
        Objects.requireNonNull(getCommand("f1mod")).setExecutor(modCommand);
        Objects.requireNonNull(getCommand("f1mod")).setTabCompleter(modCommand);
        Objects.requireNonNull(getCommand("f1admin")).setExecutor(adminCommand);
        Objects.requireNonNull(getCommand("f1admin")).setTabCompleter(adminCommand);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(resourcePackManager, raceManager, crashReporter), this);
        getServer().getPluginManager().registerEvents(resourcePackManager, this);
        getServer().getPluginManager().registerEvents(boatPhysicsManager, this);
    }

    private void saveResourceIfMissing(String name) {
        if (!getDataFolder().toPath().resolve(name).toFile().exists()) {
            saveResource(name, false);
        }
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public ResourcePackManager getResourcePackManager() {
        return resourcePackManager;
    }

    public BoatPhysicsManager getBoatPhysicsManager() {
        return boatPhysicsManager;
    }

    public FeatureManager getFeatureManager() {
        return featureManager;
    }

    public AnalyticsHandler getAnalyticsHandler() {
        return analyticsHandler;
    }

    public CrashReporter getCrashReporter() {
        return crashReporter;
    }

    public void reloadRankPoints() {
        this.rankPointsConfig = RankPointsConfig.from(this);
        this.rankPointsHook.reload(rankPointsConfig);
        this.pointsRewardService.reload(rankPointsConfig);
    }

    public RankPointsHook getRankPointsHook() {
        return rankPointsHook;
    }

    public PointsRewardService getPointsRewardService() {
        return pointsRewardService;
    }
}

package ch.akzuwo.formulaboatracing;

import ch.akzuwo.formulaboatracing.commands.AdminCommand;
import ch.akzuwo.formulaboatracing.commands.BuilderCommand;
import ch.akzuwo.formulaboatracing.commands.ModCommand;
import ch.akzuwo.formulaboatracing.commands.UserCommand;
import ch.akzuwo.formulaboatracing.listeners.PlayerConnectionListener;
import ch.akzuwo.formulaboatracing.physics.BoatPhysicsManager;
import ch.akzuwo.formulaboatracing.race.RaceManager;
import ch.akzuwo.formulaboatracing.race.RacingBoatManager;
import ch.akzuwo.formulaboatracing.resourcepack.ResourcePackManager;
import ch.akzuwo.formulaboatracing.storage.StorageManager;
import ch.akzuwo.formulaboatracing.storage.YamlStorageManager;
import ch.akzuwo.formulaboatracing.track.TrackManager;
import ch.akzuwo.formulaboatracing.utils.MessageManager;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

public final class FormulaBoatRacingPlugin extends JavaPlugin {

    private MessageManager messageManager;
    private ResourcePackManager resourcePackManager;
    private RacingBoatManager racingBoatManager;
    private TrackManager trackManager;
    private StorageManager storageManager;
    private RaceManager raceManager;
    private BoatPhysicsManager boatPhysicsManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("messages.yml");

        this.messageManager = new MessageManager(this);
        this.messageManager.reload();

        this.resourcePackManager = new ResourcePackManager(this, messageManager);
        this.racingBoatManager = new RacingBoatManager(this);
        this.trackManager = new TrackManager(this);
        this.storageManager = new YamlStorageManager(this);
        this.raceManager = new RaceManager(this, trackManager, storageManager, racingBoatManager, messageManager);
        this.boatPhysicsManager = new BoatPhysicsManager(this, racingBoatManager, raceManager);

        trackManager.loadAll();
        storageManager.load();
        resourcePackManager.reload();
        boatPhysicsManager.reload();
        raceManager.startTicker();

        registerCommands();
        registerListeners();

        getLogger().info("FormulaBoatRacing enabled with " + trackManager.getTracks().size() + " loaded track(s).");
    }

    @Override
    public void onDisable() {
        if (raceManager != null) {
            raceManager.shutdown();
        }
        if (storageManager != null) {
            storageManager.save();
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        messageManager.reload();
        resourcePackManager.reload();
        boatPhysicsManager.reload();
        trackManager.loadAll();
        storageManager.load();
    }

    private void registerCommands() {
        UserCommand userCommand = new UserCommand(trackManager, raceManager, storageManager, messageManager);
        BuilderCommand builderCommand = new BuilderCommand(trackManager, messageManager);
        ModCommand modCommand = new ModCommand(trackManager, raceManager, messageManager);
        AdminCommand adminCommand = new AdminCommand(this, trackManager, storageManager, resourcePackManager, boatPhysicsManager, messageManager);

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
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(resourcePackManager, raceManager), this);
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
}

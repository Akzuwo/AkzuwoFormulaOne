package ch.akzuwo.akzuwoformulaone.features;

import org.bukkit.plugin.java.JavaPlugin;

public final class FeatureManager {

    private final JavaPlugin plugin;
    private boolean testMode;
    private boolean resourcepackEnabled;
    private boolean forceResourcepack;
    private boolean customModelEnabled;
    private boolean soundsEnabled;
    private boolean vanillaFallback;

    public FeatureManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        this.testMode = plugin.getConfig().getBoolean("features.test-mode", true);
        this.resourcepackEnabled = plugin.getConfig().getBoolean("features.resourcepack-enabled", false);
        this.forceResourcepack = plugin.getConfig().getBoolean("features.force-resourcepack", false);
        this.customModelEnabled = plugin.getConfig().getBoolean("features.custom-model-enabled", false);
        this.soundsEnabled = plugin.getConfig().getBoolean("features.sounds-enabled", false);
        this.vanillaFallback = plugin.getConfig().getBoolean("features.vanilla-fallback", true);
    }

    public void validateFeatureSetup() {
        if (testMode && (resourcepackEnabled || forceResourcepack || customModelEnabled || soundsEnabled)) {
            plugin.getLogger().warning("Test mode is enabled while optional AkzuwoFormulaOne extras are enabled. The plugin will still prefer safe fallbacks.");
        }
        if (resourcepackEnabled && plugin.getConfig().getString("resourcepack.url", "").isBlank()) {
            if (vanillaFallback || testMode) {
                plugin.getLogger().warning("Resourcepack is enabled, but resourcepack.url is empty. Continuing with vanilla fallback.");
            } else {
                plugin.getLogger().severe("Resourcepack is enabled, resourcepack.url is empty, and vanilla fallback is disabled.");
            }
        }
        if (customModelEnabled && !vanillaFallback) {
            plugin.getLogger().warning("Custom model mode is enabled without vanilla fallback. ItemDisplay visuals are not implemented yet.");
        }
    }

    public void printFeatureStatusToConsole() {
        plugin.getLogger().info("Feature status:");
        plugin.getLogger().info("- Test mode: " + status(testMode));
        plugin.getLogger().info("- Resourcepack: " + status(resourcepackEnabled));
        plugin.getLogger().info("- Force resourcepack: " + status(forceResourcepack));
        plugin.getLogger().info("- Custom model: " + status(customModelEnabled));
        plugin.getLogger().info("- Sounds: " + status(soundsEnabled));
        plugin.getLogger().info("- Vanilla fallback: " + status(vanillaFallback));
    }

    public String statusSummary() {
        return "Test mode=" + status(testMode)
            + ", Resourcepack=" + status(resourcepackEnabled)
            + ", Force resourcepack=" + status(forceResourcepack)
            + ", Custom model=" + status(customModelEnabled)
            + ", Sounds=" + status(soundsEnabled)
            + ", Vanilla fallback=" + status(vanillaFallback);
    }

    private String status(boolean enabled) {
        return enabled ? "enabled" : "disabled";
    }

    public boolean isTestMode() {
        return testMode;
    }

    public boolean isResourcepackEnabled() {
        return resourcepackEnabled;
    }

    public boolean isForceResourcepack() {
        return forceResourcepack;
    }

    public boolean isCustomModelEnabled() {
        return customModelEnabled;
    }

    public boolean isSoundsEnabled() {
        return soundsEnabled;
    }

    public boolean isVanillaFallbackEnabled() {
        return vanillaFallback;
    }
}

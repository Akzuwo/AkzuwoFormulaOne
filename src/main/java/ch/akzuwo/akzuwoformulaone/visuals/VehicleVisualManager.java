package ch.akzuwo.akzuwoformulaone.visuals;

import ch.akzuwo.akzuwoformulaone.features.FeatureManager;
import ch.akzuwo.akzuwoformulaone.track.Track;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class VehicleVisualManager {

    private final JavaPlugin plugin;
    private final FeatureManager featureManager;
    private final Set<UUID> customVisualBoats = new HashSet<>();
    private boolean warnedAboutPlaceholder;
    private boolean customModelEnabled;
    private String displayMode;
    private Material itemMaterial;
    private int customModelData;
    private boolean hideVanillaBoat;

    public VehicleVisualManager(JavaPlugin plugin, FeatureManager featureManager) {
        this.plugin = plugin;
        this.featureManager = featureManager;
        reload();
    }

    public void reload() {
        this.customModelEnabled = plugin.getConfig().getBoolean("custom-model.enabled", featureManager.isCustomModelEnabled());
        this.displayMode = plugin.getConfig().getString("custom-model.display-mode", "NONE");
        this.itemMaterial = parseMaterial(plugin.getConfig().getString("custom-model.item-material", "CARROT_ON_A_STICK"));
        this.customModelData = plugin.getConfig().getInt("custom-model.custom-model-data", 1001);
        this.hideVanillaBoat = plugin.getConfig().getBoolean("custom-model.hide-vanilla-boat", false);
        this.warnedAboutPlaceholder = false;
    }

    public void applyVisuals(Boat boat, Player player, Track track) {
        if (!featureManager.isCustomModelEnabled() || !customModelEnabled || "NONE".equalsIgnoreCase(displayMode)) {
            debug("Custom model disabled. Using visible vanilla boat for " + player.getName() + " on " + track.getId() + ".");
            return;
        }

        if (!warnedAboutPlaceholder) {
            plugin.getLogger().warning("Custom model visuals are enabled, but ItemDisplay vehicle visuals are not implemented yet. Falling back to vanilla boats.");
            warnedAboutPlaceholder = true;
        }
        if (featureManager.isVanillaFallbackEnabled()) {
            return;
        }
        plugin.getLogger().warning("Vanilla fallback is disabled, but no custom visual implementation exists. Keeping the vanilla boat visible to avoid an invisible vehicle.");
    }

    public void updateVisuals(Boat boat) {
        if (!hasCustomVisuals(boat)) {
            return;
        }
        // TODO: Move linked ItemDisplay with the boat once custom visuals are implemented.
    }

    public void removeVisuals(Boat boat) {
        customVisualBoats.remove(boat.getUniqueId());
        // TODO: Remove linked ItemDisplay entities once custom visuals are implemented.
    }

    public boolean hasCustomVisuals(Boat boat) {
        return customVisualBoats.contains(boat.getUniqueId());
    }

    public String statusSummary() {
        return "Custom model=" + status(featureManager.isCustomModelEnabled() && customModelEnabled)
            + ", display-mode=" + displayMode
            + ", item-material=" + itemMaterial
            + ", custom-model-data=" + customModelData
            + ", hide-vanilla-boat=" + hideVanillaBoat;
    }

    private Material parseMaterial(String value) {
        Material material = Material.matchMaterial(value == null ? "" : value);
        if (material == null) {
            plugin.getLogger().warning("Invalid custom-model.item-material '" + value + "'. Falling back to CARROT_ON_A_STICK.");
            return Material.CARROT_ON_A_STICK;
        }
        return material;
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

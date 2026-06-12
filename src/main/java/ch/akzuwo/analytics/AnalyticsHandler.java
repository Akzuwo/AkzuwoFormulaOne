package ch.akzuwo.analytics;

import ch.akzuwo.akzuwoformulaone.features.FeatureManager;
import ch.akzuwo.akzuwoformulaone.race.RaceManager;
import ch.akzuwo.akzuwoformulaone.track.Track;
import ch.akzuwo.akzuwoformulaone.track.TrackManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AnalyticsHandler {

    private static final String OFFICIAL_ENDPOINT_INFO_PATH = "analytics.official-endpoint-info";

    private final JavaPlugin plugin;
    private final FeatureManager featureManager;
    private final TrackManager trackManager;
    private final RaceManager raceManager;
    private final AnalyticsLocalWriter localWriter;
    private final Gson gson = new GsonBuilder().create();
    private AnalyticsConfig config;

    public AnalyticsHandler(JavaPlugin plugin, FeatureManager featureManager, TrackManager trackManager, RaceManager raceManager) {
        this.plugin = plugin;
        this.featureManager = featureManager;
        this.trackManager = trackManager;
        this.raceManager = raceManager;
        this.localWriter = new AnalyticsLocalWriter(plugin);
        reload();
    }

    public void reload() {
        this.config = AnalyticsConfig.from(plugin);
        warnIfEndpointInfoChanged();
    }

    public void printStartupStatus() {
        if (config.isSendAnalyticsData()) {
            plugin.getLogger().info("Analytics ist aktiviert.");
            plugin.getLogger().info("Offizielle Analytics gehen an: " + AnalyticsConstants.OFFICIAL_ANALYTICS_ENDPOINT);
            plugin.getLogger().info("Dies kann mit analytics.send-analytics-data=false deaktiviert werden.");
        } else {
            plugin.getLogger().info("Analytics ist deaktiviert.");
            plugin.getLogger().info("Es werden keine offiziellen Analytics-Daten gesendet.");
        }
        plugin.getLogger().info("Zusätzliche lokale Kopie: " + status(config.isLocalCopyEnabled()) + ".");
        plugin.getLogger().info("Zusätzlicher Custom-Copy-Endpoint: " + status(config.isCustomCopyEnabled()) + ".");
    }

    public void sendShutdownReport() {
        Map<String, Object> payload = createBasePayload("shutdown_report");
        dispatchReport(payload, "", "shutdown-report");
    }

    public void sendTestReport() {
        Map<String, Object> payload = createBasePayload("analytics_test");
        payload.put("message", "Manual analytics test from admin command");
        dispatchReport(payload, "", "analytics-test");
    }

    public String statusSummary() {
        return "Analytics aktiviert: " + yesNo(config.isSendAnalyticsData())
            + ", Offizieller Endpoint: " + AnalyticsConstants.OFFICIAL_ANALYTICS_ENDPOINT
            + ", Custom Copy Endpoint: " + status(config.isCustomCopyEnabled())
            + ", Local Copy: " + status(config.isLocalCopyEnabled())
            + ", Timeout: " + config.getTimeoutMs() + "ms";
    }

    public AnalyticsConfig getConfig() {
        return config;
    }

    public void dispatchReport(Map<String, Object> payload, String localSubFolder, String localFilePrefix) {
        String json = gson.toJson(payload);

        if (config.isSendAnalyticsData()) {
            try {
                post(URI.create(AnalyticsConstants.OFFICIAL_ANALYTICS_ENDPOINT), json, config.getTimeoutMs(), "Offizielle Analytics");
            } catch (RuntimeException ex) {
                plugin.getLogger().warning("Offizielle Analytics konnten nicht gesendet werden: " + ex.getMessage());
            }
        }

        if (config.isCustomCopyEnabled() && config.getCustomCopyUri() != null) {
            try {
                post(config.getCustomCopyUri(), json, config.getCustomCopyTimeoutMs(), "Custom Analytics Copy");
            } catch (RuntimeException ex) {
                plugin.getLogger().warning("Custom Analytics Copy konnte nicht gesendet werden: " + ex.getMessage());
            }
        }

        if (config.isLocalCopyEnabled()) {
            try {
                localWriter.write(payload, config, localSubFolder, localFilePrefix);
            } catch (RuntimeException ex) {
                plugin.getLogger().warning("Lokale Analytics-Kopie konnte nicht gespeichert werden: " + ex.getMessage());
            }
        }
    }

    private void post(URI uri, String json, int timeoutMs, String label) {
        try {
            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .build();
            HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofMillis(timeoutMs))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("HTTP " + response.statusCode());
            }
            if (config.isLogSuccess()) {
                plugin.getLogger().info(label + " gesendet.");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Endpoint nicht erreichbar.");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Request wurde unterbrochen.");
        }
    }

    private Map<String, Object> createBasePayload(String type) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("schema_version", AnalyticsConstants.SCHEMA_VERSION);
        payload.put("type", type);
        payload.put("timestamp", Instant.now().toString());
        payload.put("plugin", pluginInfo());
        payload.put("runtime", runtimeInfo());
        payload.put("features", featureInfo());
        payload.put("tracks", trackInfo());
        payload.put("races", raceInfo());
        payload.put("physics", physicsInfo());
        return payload;
    }

    private Map<String, Object> pluginInfo() {
        Map<String, Object> pluginInfo = new LinkedHashMap<>();
        pluginInfo.put("name", plugin.getName());
        pluginInfo.put("version", plugin.getDescription().getVersion());
        return pluginInfo;
    }

    private Map<String, Object> runtimeInfo() {
        Map<String, Object> runtimeInfo = new LinkedHashMap<>();
        runtimeInfo.put("minecraft_version", Bukkit.getMinecraftVersion());
        runtimeInfo.put("server_version", Bukkit.getVersion());
        runtimeInfo.put("java_version", System.getProperty("java.version", "unknown"));
        return runtimeInfo;
    }

    private Map<String, Object> featureInfo() {
        Map<String, Object> features = new LinkedHashMap<>();
        features.put("test_mode", featureManager.isTestMode());
        features.put("resourcepack_enabled", featureManager.isResourcepackEnabled());
        features.put("force_resourcepack", featureManager.isForceResourcepack());
        features.put("custom_model_enabled", featureManager.isCustomModelEnabled());
        features.put("sounds_enabled", featureManager.isSoundsEnabled());
        features.put("vanilla_fallback", featureManager.isVanillaFallbackEnabled());
        return features;
    }

    private Map<String, Object> trackInfo() {
        Map<String, Object> tracks = new LinkedHashMap<>();
        tracks.put("loaded", trackManager.getTracks().size());
        tracks.put("enabled", trackManager.getTracks().stream().filter(Track::isEnabled).count());
        tracks.put("usable", trackManager.getTracks().stream().filter(Track::isUsable).count());
        return tracks;
    }

    private Map<String, Object> raceInfo() {
        Map<String, Object> races = new LinkedHashMap<>();
        races.put("active", raceManager.getRaces().size());
        races.put("started_since_start", raceManager.getStartedRacesSinceStartup());
        races.put("finished_since_start", raceManager.getFinishedRacesSinceStartup());
        races.put("cancelled_since_start", raceManager.getCancelledRacesSinceStartup());
        return races;
    }

    private Map<String, Object> physicsInfo() {
        Map<String, Object> physics = new LinkedHashMap<>();
        physics.put("enabled", plugin.getConfig().getBoolean("physics.enabled", true));
        physics.put("max_speed", plugin.getConfig().getDouble("physics.max-speed", 1.2));
        physics.put("acceleration_multiplier", plugin.getConfig().getDouble("physics.acceleration-multiplier", 1.05));
        physics.put("drift_reduction", plugin.getConfig().getDouble("physics.drift-reduction", 0.55));
        physics.put("steering_assist", plugin.getConfig().getDouble("physics.steering-assist", 0.30));
        physics.put("countdown_brake", plugin.getConfig().getDouble("physics.countdown-brake", 0.15));
        return physics;
    }

    private void warnIfEndpointInfoChanged() {
        String info = plugin.getConfig().getString(OFFICIAL_ENDPOINT_INFO_PATH, AnalyticsConstants.OFFICIAL_ANALYTICS_ENDPOINT);
        if (!AnalyticsConstants.OFFICIAL_ANALYTICS_ENDPOINT.equals(info)) {
            plugin.getLogger().warning("Hinweis: analytics.official-endpoint-info wurde verändert, der offizielle Versand nutzt trotzdem fest " + AnalyticsConstants.OFFICIAL_ANALYTICS_ENDPOINT + ".");
        }
    }

    private String status(boolean enabled) {
        return enabled ? "aktiviert" : "deaktiviert";
    }

    private String yesNo(boolean enabled) {
        return enabled ? "ja" : "nein";
    }
}

package ch.akzuwo.analytics;

import ch.akzuwo.akzuwoformulaone.features.FeatureManager;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.java.JavaPlugin;

public final class CrashReporter {

    private final JavaPlugin plugin;
    private final FeatureManager featureManager;
    private final AnalyticsHandler analyticsHandler;
    private final PluginLogBuffer logBuffer;
    private final Map<String, Long> lastReports = new HashMap<>();
    private AnalyticsConfig config;

    public CrashReporter(JavaPlugin plugin, FeatureManager featureManager, AnalyticsHandler analyticsHandler, PluginLogBuffer logBuffer) {
        this.plugin = plugin;
        this.featureManager = featureManager;
        this.analyticsHandler = analyticsHandler;
        this.logBuffer = logBuffer;
        this.config = analyticsHandler.getConfig();
    }

    public void reload(AnalyticsConfig config) {
        this.config = config;
        this.logBuffer.setLimit(config.getCrashRecentLogLimit());
    }

    public void reportException(String component, String action, Throwable throwable) {
        reportException(component, action, throwable, Map.of());
    }

    public void reportException(String component, String action, Throwable throwable, Map<String, Object> context) {
        try {
            if (config == null || !config.isCrashReportingEnabled()) {
                return;
            }
            String key = component + ":" + action + ":" + (throwable == null ? "unknown" : throwable.getClass().getName());
            long now = System.currentTimeMillis();
            long last = lastReports.getOrDefault(key, 0L);
            if (now - last < config.getCrashRateLimitSeconds() * 1000L) {
                return;
            }
            lastReports.put(key, now);

            logBuffer.add("ERROR", component, action + " failed: " + (throwable == null ? "unknown" : throwable.getMessage()));
            Map<String, Object> payload = CrashReportPayload.create(
                plugin,
                featureManager,
                config,
                component,
                action,
                throwable,
                context,
                logBuffer.getRecentEntries()
            );
            analyticsHandler.dispatchReport(payload, "crashes", "crash-report");
            plugin.getLogger().warning("AkzuwoFormulaOne exception report captured for " + component + "#" + action + ": "
                + (throwable == null ? "unknown" : throwable.getClass().getSimpleName()));
            if (config.isCrashDebug() && throwable != null) {
                throwable.printStackTrace();
            }
        } catch (RuntimeException ex) {
            plugin.getLogger().warning("CrashReporter konnte keinen Fehlerbericht verarbeiten: " + ex.getMessage());
        }
    }

    public PluginLogBuffer getLogBuffer() {
        return logBuffer;
    }
}

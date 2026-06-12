package ch.akzuwo.analytics;

import ch.akzuwo.akzuwoformulaone.features.FeatureManager;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class CrashReportPayload {

    private CrashReportPayload() {
    }

    public static Map<String, Object> create(
        JavaPlugin plugin,
        FeatureManager featureManager,
        AnalyticsConfig config,
        String component,
        String action,
        Throwable throwable,
        Map<String, Object> context,
        List<PluginLogBuffer.LogEntry> recentLogs
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("source", "akzuwo-formula-one-plugin");
        payload.put("type", "plugin_exception");
        payload.put("severity", "error");
        payload.put("title", "Plugin exception");
        payload.put("message", "Exception in AkzuwoFormulaOne");
        payload.put("plugin", plugin(plugin));
        payload.put("server", server());
        payload.put("exception", exception(throwable, config));
        payload.put("context", context(featureManager, component, action, context));
        payload.put("recentPluginLogs", config.isCrashIncludeRecentPluginLogs() ? logs(recentLogs) : List.of());
        payload.put("metadata", Map.of("analyticsSchemaVersion", AnalyticsConstants.SCHEMA_VERSION));
        return payload;
    }

    private static Map<String, Object> plugin(JavaPlugin plugin) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("name", plugin.getName());
        data.put("version", plugin.getDescription().getVersion());
        data.put("apiVersion", plugin.getDescription().getAPIVersion());
        return data;
    }

    private static Map<String, Object> server() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("minecraftVersion", Bukkit.getMinecraftVersion());
        data.put("bukkitVersion", Bukkit.getVersion());
        data.put("javaVersion", System.getProperty("java.version", "unknown"));
        data.put("osName", System.getProperty("os.name", "unknown"));
        data.put("onlinePlayers", Bukkit.getOnlinePlayers().size());
        return data;
    }

    private static Map<String, Object> exception(Throwable throwable, AnalyticsConfig config) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("class", throwable == null ? "unknown" : throwable.getClass().getName());
        data.put("message", SafeExceptionUtil.sanitize(throwable == null ? "" : throwable.getMessage()));
        data.put("stacktrace", config.isCrashIncludeStacktrace()
            ? SafeExceptionUtil.stackTraceToString(throwable, config.getCrashMaxStacktraceLength())
            : "");
        return data;
    }

    private static Map<String, Object> context(FeatureManager featureManager, String component, String action, Map<String, Object> context) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("component", SafeExceptionUtil.sanitize(component));
        data.put("action", SafeExceptionUtil.sanitize(action));
        if (context != null) {
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                data.put(SafeExceptionUtil.sanitize(entry.getKey()), safeValue(entry.getValue()));
            }
        }
        data.put("featureStatus", Map.of(
            "testMode", featureManager.isTestMode(),
            "resourcepackEnabled", featureManager.isResourcepackEnabled(),
            "customModelEnabled", featureManager.isCustomModelEnabled(),
            "soundsEnabled", featureManager.isSoundsEnabled(),
            "vanillaFallback", featureManager.isVanillaFallbackEnabled()
        ));
        return data;
    }

    private static Object safeValue(Object value) {
        if (value == null || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        return SafeExceptionUtil.sanitize(String.valueOf(value));
    }

    private static List<Map<String, Object>> logs(List<PluginLogBuffer.LogEntry> recentLogs) {
        return recentLogs.stream()
            .map(entry -> {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("timestamp", entry.timestamp());
                data.put("level", entry.level());
                data.put("component", entry.component());
                data.put("message", entry.message());
                return data;
            })
            .toList();
    }
}

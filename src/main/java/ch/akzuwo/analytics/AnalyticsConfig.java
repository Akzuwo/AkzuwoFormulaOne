package ch.akzuwo.analytics;

import java.net.URI;
import java.net.URISyntaxException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class AnalyticsConfig {

    private final boolean sendAnalyticsData;
    private final int timeoutMs;
    private final boolean logSuccess;
    private final boolean crashReportingEnabled;
    private final boolean crashIncludeStacktrace;
    private final boolean crashIncludeRecentPluginLogs;
    private final int crashRecentLogLimit;
    private final int crashRateLimitSeconds;
    private final int crashMaxStacktraceLength;
    private final boolean crashDebug;
    private final boolean customCopyEnabled;
    private final URI customCopyUri;
    private final int customCopyTimeoutMs;
    private final boolean localCopyEnabled;
    private final String localCopyFolder;
    private final boolean localCopyPrettyPrint;

    private AnalyticsConfig(
        boolean sendAnalyticsData,
        int timeoutMs,
        boolean logSuccess,
        boolean crashReportingEnabled,
        boolean crashIncludeStacktrace,
        boolean crashIncludeRecentPluginLogs,
        int crashRecentLogLimit,
        int crashRateLimitSeconds,
        int crashMaxStacktraceLength,
        boolean crashDebug,
        boolean customCopyEnabled,
        URI customCopyUri,
        int customCopyTimeoutMs,
        boolean localCopyEnabled,
        String localCopyFolder,
        boolean localCopyPrettyPrint
    ) {
        this.sendAnalyticsData = sendAnalyticsData;
        this.timeoutMs = timeoutMs;
        this.logSuccess = logSuccess;
        this.crashReportingEnabled = crashReportingEnabled;
        this.crashIncludeStacktrace = crashIncludeStacktrace;
        this.crashIncludeRecentPluginLogs = crashIncludeRecentPluginLogs;
        this.crashRecentLogLimit = crashRecentLogLimit;
        this.crashRateLimitSeconds = crashRateLimitSeconds;
        this.crashMaxStacktraceLength = crashMaxStacktraceLength;
        this.crashDebug = crashDebug;
        this.customCopyEnabled = customCopyEnabled;
        this.customCopyUri = customCopyUri;
        this.customCopyTimeoutMs = customCopyTimeoutMs;
        this.localCopyEnabled = localCopyEnabled;
        this.localCopyFolder = localCopyFolder;
        this.localCopyPrettyPrint = localCopyPrettyPrint;
    }

    public static AnalyticsConfig from(JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
        boolean customEnabled = config.getBoolean("analytics.custom-copy-endpoint.enabled", false);
        URI customUri = null;
        String customUrl = config.getString("analytics.custom-copy-endpoint.url", "");
        if (customEnabled) {
            customUri = parseCustomUri(plugin, customUrl);
            if (customUri == null) {
                customEnabled = false;
            }
        }

        String folder = config.getString("analytics.local-copy.folder", "analytics");
        if (folder == null || folder.isBlank()) {
            folder = "analytics";
        }

        return new AnalyticsConfig(
            config.getBoolean("analytics.send-analytics-data", true),
            Math.max(250, config.getInt("analytics.timeout-ms", 3000)),
            config.getBoolean("analytics.log-success", false),
            config.getBoolean("analytics.crash-reporting.enabled", true),
            config.getBoolean("analytics.crash-reporting.include-stacktrace", true),
            config.getBoolean("analytics.crash-reporting.include-recent-plugin-logs", true),
            Math.max(1, config.getInt("analytics.crash-reporting.recent-log-limit", 200)),
            Math.max(1, config.getInt("analytics.crash-reporting.rate-limit-seconds", 60)),
            Math.max(500, config.getInt("analytics.crash-reporting.max-stacktrace-length", 12000)),
            config.getBoolean("analytics.crash-reporting.debug", config.getBoolean("debug", false)),
            customEnabled,
            customUri,
            Math.max(250, config.getInt("analytics.custom-copy-endpoint.timeout-ms", 3000)),
            config.getBoolean("analytics.local-copy.enabled", false),
            folder,
            config.getBoolean("analytics.local-copy.pretty-print", true)
        );
    }

    private static URI parseCustomUri(JavaPlugin plugin, String customUrl) {
        if (customUrl == null || customUrl.isBlank()) {
            plugin.getLogger().warning("Custom Analytics Copy is enabled, but analytics.custom-copy-endpoint.url is empty. Custom copy disabled.");
            return null;
        }
        try {
            URI uri = new URI(customUrl);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                plugin.getLogger().warning("Custom Analytics Copy URL must use http or https. Custom copy disabled.");
                return null;
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                plugin.getLogger().warning("Custom Analytics Copy URL has no host. Custom copy disabled.");
                return null;
            }
            return uri;
        } catch (URISyntaxException ex) {
            plugin.getLogger().warning("Custom Analytics Copy URL is invalid: " + ex.getMessage() + ". Custom copy disabled.");
            return null;
        }
    }

    public boolean isSendAnalyticsData() {
        return sendAnalyticsData;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public boolean isLogSuccess() {
        return logSuccess;
    }

    public boolean isCrashReportingEnabled() {
        return crashReportingEnabled;
    }

    public boolean isCrashIncludeStacktrace() {
        return crashIncludeStacktrace;
    }

    public boolean isCrashIncludeRecentPluginLogs() {
        return crashIncludeRecentPluginLogs;
    }

    public int getCrashRecentLogLimit() {
        return crashRecentLogLimit;
    }

    public int getCrashRateLimitSeconds() {
        return crashRateLimitSeconds;
    }

    public int getCrashMaxStacktraceLength() {
        return crashMaxStacktraceLength;
    }

    public boolean isCrashDebug() {
        return crashDebug;
    }

    public boolean isCustomCopyEnabled() {
        return customCopyEnabled;
    }

    public URI getCustomCopyUri() {
        return customCopyUri;
    }

    public int getCustomCopyTimeoutMs() {
        return customCopyTimeoutMs;
    }

    public boolean isLocalCopyEnabled() {
        return localCopyEnabled;
    }

    public String getLocalCopyFolder() {
        return localCopyFolder;
    }

    public boolean isLocalCopyPrettyPrint() {
        return localCopyPrettyPrint;
    }
}

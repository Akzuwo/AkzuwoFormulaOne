package ch.akzuwo.analytics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.bukkit.plugin.java.JavaPlugin;

public final class AnalyticsLocalWriter {

    private static final DateTimeFormatter FILE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final JavaPlugin plugin;

    public AnalyticsLocalWriter(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void write(Map<String, Object> payload, AnalyticsConfig config) {
        write(payload, config, "", "shutdown-report");
    }

    public void write(Map<String, Object> payload, AnalyticsConfig config, String subFolder, String filePrefix) {
        try {
            Path baseFolder = plugin.getDataFolder().toPath().toAbsolutePath().normalize();
            Path folder = baseFolder.resolve(config.getLocalCopyFolder()).normalize();
            if (subFolder != null && !subFolder.isBlank()) {
                folder = folder.resolve(subFolder).normalize();
            }
            if (!folder.startsWith(baseFolder)) {
                plugin.getLogger().warning("analytics.local-copy.folder zeigt ausserhalb des Plugin-Ordners. Fallback auf analytics.");
                folder = baseFolder.resolve("analytics");
                if (subFolder != null && !subFolder.isBlank()) {
                    folder = folder.resolve(subFolder).normalize();
                }
            }
            Files.createDirectories(folder);
            String safePrefix = SafeExceptionUtil.sanitize(filePrefix == null || filePrefix.isBlank() ? "analytics-report" : filePrefix)
                .replaceAll("[^A-Za-z0-9_-]", "-");
            Path file = folder.resolve(safePrefix + "-" + LocalDateTime.now().format(FILE_TIME_FORMAT) + ".json");
            Gson gson = config.isLocalCopyPrettyPrint() ? new GsonBuilder().setPrettyPrinting().create() : new Gson();
            Files.writeString(file, gson.toJson(payload), StandardCharsets.UTF_8);
            plugin.getLogger().info("Lokale Analytics-Kopie gespeichert: " + file);
        } catch (IOException | RuntimeException ex) {
            plugin.getLogger().warning("Lokale Analytics-Kopie konnte nicht gespeichert werden: " + ex.getMessage());
        }
    }
}

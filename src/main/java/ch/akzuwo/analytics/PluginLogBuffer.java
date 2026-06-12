package ch.akzuwo.analytics;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class PluginLogBuffer {

    private final ArrayDeque<LogEntry> entries = new ArrayDeque<>();
    private int limit;

    public PluginLogBuffer(int limit) {
        this.limit = Math.max(1, limit);
    }

    public synchronized void setLimit(int limit) {
        this.limit = Math.max(1, limit);
        trim();
    }

    public synchronized void add(String level, String component, String message) {
        entries.addLast(new LogEntry(
            Instant.now().toString(),
            SafeExceptionUtil.sanitize(level),
            SafeExceptionUtil.sanitize(component),
            SafeExceptionUtil.sanitize(message)
        ));
        trim();
    }

    public synchronized List<LogEntry> getRecentEntries() {
        return new ArrayList<>(entries);
    }

    public synchronized void clear() {
        entries.clear();
    }

    private void trim() {
        while (entries.size() > limit) {
            entries.removeFirst();
        }
    }

    public record LogEntry(String timestamp, String level, String component, String message) {
    }
}

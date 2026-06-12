package ch.akzuwo.formulaboatracing.storage;

import java.time.Instant;
import java.util.UUID;

public record StoredTime(UUID playerUuid, String playerName, String trackId, long bestTimeMillis, Instant date) {
}

package ch.akzuwo.formulaboatracing.race;

import java.util.UUID;

public record RaceResult(UUID playerUuid, String playerName, String trackId, long totalTimeMillis, boolean personalBest) {
}

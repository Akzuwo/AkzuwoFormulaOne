package ch.akzuwo.akzuwoformulaone.utils;

public final class TimeUtil {

    private TimeUtil() {
    }

    public static String formatMillis(long millis) {
        long minutes = millis / 60000L;
        long seconds = (millis % 60000L) / 1000L;
        long ms = millis % 1000L;
        return String.format("%d:%02d.%03d", minutes, seconds, ms);
    }
}

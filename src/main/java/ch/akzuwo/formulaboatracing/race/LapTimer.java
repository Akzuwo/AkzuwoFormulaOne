package ch.akzuwo.formulaboatracing.race;

public final class LapTimer {

    private long startMillis;

    public void start() {
        this.startMillis = System.currentTimeMillis();
    }

    public long elapsedMillis() {
        return startMillis <= 0L ? 0L : System.currentTimeMillis() - startMillis;
    }

    public long getStartMillis() {
        return startMillis;
    }
}

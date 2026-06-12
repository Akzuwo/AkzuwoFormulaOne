package ch.ksrminecraft.points;

public record PointsRewardResult(boolean success, boolean skipped, int points, String reason) {

    public static PointsRewardResult rewarded(int points) {
        return new PointsRewardResult(true, false, points, "rewarded");
    }

    public static PointsRewardResult skipped(String reason) {
        return new PointsRewardResult(false, true, 0, reason);
    }
}

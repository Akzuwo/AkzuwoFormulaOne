package ch.ksrminecraft.points;

public final class RacePointsCalculator {

    public int calculate(
        String trackId,
        boolean finished,
        boolean cancelled,
        boolean validCheckpoints,
        boolean personalBest,
        boolean trackRecord,
        RankPointsConfig config
    ) {
        if (!config.isRewardsEnabled()) {
            return 0;
        }
        if (!finished) {
            return 0;
        }
        if (cancelled && !config.isRewardCancelledRaces()) {
            return 0;
        }
        if (config.isRequireValidCheckpoints() && !validCheckpoints) {
            return 0;
        }

        int points = config.getFinishRaceBasePoints();
        if (personalBest) {
            points += config.getPersonalBestBonusPoints();
        }
        if (trackRecord) {
            points += config.getTrackRecordBonusPoints();
        }

        long result = Math.round(points * config.getTrackMultiplier(trackId));
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, result));
    }
}

package ch.akzuwo.formulaboatracing.physics;

import org.bukkit.configuration.file.FileConfiguration;

public record PhysicsConfig(
    boolean enabled,
    double maxSpeed,
    double accelerationMultiplier,
    double driftReduction,
    double steeringAssist,
    double offTrackSlowdown
) {

    public static PhysicsConfig from(FileConfiguration config) {
        return new PhysicsConfig(
            config.getBoolean("physics.enabled", true),
            config.getDouble("physics.max-speed", 1.2),
            config.getDouble("physics.acceleration-multiplier", 1.08),
            config.getDouble("physics.drift-reduction", 0.55),
            config.getDouble("physics.steering-assist", 0.35),
            config.getDouble("physics.off-track-slowdown", 0.45)
        );
    }
}

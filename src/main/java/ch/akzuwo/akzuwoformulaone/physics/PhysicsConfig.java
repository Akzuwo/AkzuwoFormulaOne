package ch.akzuwo.akzuwoformulaone.physics;

import org.bukkit.configuration.file.FileConfiguration;

public record PhysicsConfig(
    boolean enabled,
    double maxSpeed,
    double accelerationMultiplier,
    double driftReduction,
    double steeringAssist,
    double countdownBrake
) {

    public static PhysicsConfig from(FileConfiguration config) {
        return new PhysicsConfig(
            config.getBoolean("physics.enabled", true),
            config.getDouble("physics.max-speed", 1.2),
            config.getDouble("physics.acceleration-multiplier", 1.05),
            config.getDouble("physics.drift-reduction", 0.55),
            config.getDouble("physics.steering-assist", 0.30),
            config.getDouble("physics.countdown-brake", 0.15)
        );
    }
}

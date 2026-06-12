package ch.akzuwo.formulaboatracing.physics;

import ch.akzuwo.formulaboatracing.race.RaceManager;
import ch.akzuwo.formulaboatracing.race.RacingBoatManager;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public final class BoatPhysicsManager implements Listener {

    private static final double MIN_CONTROL_SPEED = 0.02;

    private final JavaPlugin plugin;
    private final RacingBoatManager racingBoatManager;
    private final RaceManager raceManager;
    private PhysicsConfig config;

    public BoatPhysicsManager(JavaPlugin plugin, RacingBoatManager racingBoatManager, RaceManager raceManager) {
        this.plugin = plugin;
        this.racingBoatManager = racingBoatManager;
        this.raceManager = raceManager;
        reload();
    }

    public void reload() {
        this.config = PhysicsConfig.from(plugin.getConfig());
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!config.enabled() || !(event.getVehicle() instanceof Boat boat) || !raceManager.isActiveRacingBoat(boat)) {
            return;
        }
        Player player = boat.getPassengers().stream()
            .filter(Player.class::isInstance)
            .map(Player.class::cast)
            .findFirst()
            .orElse(null);
        if (player == null) {
            return;
        }

        Vector velocity = boat.getVelocity();
        Vector horizontal = new Vector(velocity.getX(), 0.0, velocity.getZ());
        double speed = horizontal.length();
        if (speed < MIN_CONTROL_SPEED) {
            return;
        }

        Vector forward = player.getLocation().getDirection().setY(0.0);
        if (forward.lengthSquared() == 0.0) {
            return;
        }
        forward.normalize();

        // Split the current velocity into forward and lateral parts. Reducing only the
        // lateral part keeps speed while making the boat feel less like vanilla ice drift.
        double forwardMagnitude = horizontal.dot(forward);
        Vector forwardComponent = forward.clone().multiply(forwardMagnitude * config.accelerationMultiplier());
        Vector lateralComponent = horizontal.clone().subtract(forward.clone().multiply(forwardMagnitude)).multiply(config.driftReduction());
        Vector assistedDirection = forward.clone().multiply(speed);
        Vector adjusted = forwardComponent.add(lateralComponent).multiply(1.0 - config.steeringAssist())
            .add(assistedDirection.multiply(config.steeringAssist()));

        double adjustedSpeed = adjusted.length();
        if (adjustedSpeed > config.maxSpeed()) {
            adjusted.normalize().multiply(config.maxSpeed());
        }

        boat.setVelocity(new Vector(adjusted.getX(), velocity.getY(), adjusted.getZ()));
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (racingBoatManager.isRacingBoat(event.getVehicle()) && !(event.getEntered() instanceof Player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (racingBoatManager.isRacingBoat(event.getVehicle()) && event.getExited() instanceof Player player) {
            player.sendMessage("Du hast dein Rennboot verlassen. Nutze /f1 leave, falls du aufgeben möchtest.");
        }
    }
}

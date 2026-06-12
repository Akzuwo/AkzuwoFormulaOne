package ch.akzuwo.akzuwoformulaone.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public final class LocationUtil {

    private LocationUtil() {
    }

    public static Location readLocation(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        String worldName = section.getString("world");
        World world = worldName == null ? null : Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(
            world,
            section.getDouble("x"),
            section.getDouble("y"),
            section.getDouble("z"),
            (float) section.getDouble("yaw", 0.0),
            (float) section.getDouble("pitch", 0.0)
        );
    }

    public static void writeLocation(ConfigurationSection section, Location location, boolean includeRotation) {
        section.set("world", location.getWorld() == null ? "" : location.getWorld().getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        if (includeRotation) {
            section.set("yaw", location.getYaw());
            section.set("pitch", location.getPitch());
        }
    }

    public static boolean sameWorld(Location first, Location second) {
        return first != null
            && second != null
            && first.getWorld() != null
            && second.getWorld() != null
            && first.getWorld().equals(second.getWorld());
    }
}

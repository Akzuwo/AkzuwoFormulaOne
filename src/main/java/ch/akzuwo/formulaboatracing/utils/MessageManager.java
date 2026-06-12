package ch.akzuwo.formulaboatracing.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class MessageManager {

    private final JavaPlugin plugin;
    private FileConfiguration messages;
    private String prefix;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        this.messages = YamlConfiguration.loadConfiguration(file);
        this.prefix = color(messages.getString("prefix", "&c[F1]&r "));
    }

    public String get(String path, String fallback) {
        return color(messages.getString(path, fallback));
    }

    public void send(CommandSender sender, String path, String fallback) {
        sender.sendMessage(prefix + get(path, fallback));
    }

    public void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(prefix + color(message));
    }

    public static String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input == null ? "" : input);
    }
}

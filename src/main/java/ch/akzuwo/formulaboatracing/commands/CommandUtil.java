package ch.akzuwo.formulaboatracing.commands;

import ch.akzuwo.formulaboatracing.permissions.Permissions;
import ch.akzuwo.formulaboatracing.utils.MessageManager;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

final class CommandUtil {

    private CommandUtil() {
    }

    static boolean require(CommandSender sender, String permission, MessageManager messages) {
        if (Permissions.has(sender, permission)) {
            return true;
        }
        messages.send(sender, "errors.no-permission", "&cDazu hast du keine Berechtigung.");
        return false;
    }

    static Player requirePlayer(CommandSender sender, MessageManager messages) {
        if (sender instanceof Player player) {
            return player;
        }
        messages.send(sender, "errors.player-only", "&cDieser Command kann nur als Spieler ausgeführt werden.");
        return null;
    }

    static List<String> filter(List<String> values, String prefix) {
        String lower = prefix.toLowerCase();
        return values.stream().filter(value -> value.toLowerCase().startsWith(lower)).toList();
    }
}

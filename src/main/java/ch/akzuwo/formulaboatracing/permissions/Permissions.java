package ch.akzuwo.formulaboatracing.permissions;

import org.bukkit.command.CommandSender;

public final class Permissions {

    public static final String USER = "formulaboat.user";
    public static final String USER_JOIN = "formulaboat.user.join";
    public static final String USER_LEAVE = "formulaboat.user.leave";
    public static final String USER_START = "formulaboat.user.start";
    public static final String USER_LEADERBOARD = "formulaboat.user.leaderboard";

    public static final String MOD = "formulaboat.mod";
    public static final String MOD_CANCEL = "formulaboat.mod.cancel";
    public static final String MOD_KICK = "formulaboat.mod.kick";
    public static final String MOD_TP = "formulaboat.mod.tp";
    public static final String MOD_INFO = "formulaboat.mod.info";

    public static final String ADMIN = "formulaboat.admin";
    public static final String ADMIN_RELOAD = "formulaboat.admin.reload";
    public static final String ADMIN_DEBUG = "formulaboat.admin.debug";
    public static final String ADMIN_RESOURCEPACK = "formulaboat.admin.resourcepack";
    public static final String ADMIN_PHYSICS = "formulaboat.admin.physics";

    public static final String BUILDER = "formulaboat.builder";
    public static final String BUILDER_CREATE = "formulaboat.builder.create";
    public static final String BUILDER_EDIT = "formulaboat.builder.edit";
    public static final String BUILDER_CHECKPOINT = "formulaboat.builder.checkpoint";
    public static final String BUILDER_SAVE = "formulaboat.builder.save";
    public static final String BUILDER_ENABLE = "formulaboat.builder.enable";

    private Permissions() {
    }

    public static boolean has(CommandSender sender, String permission) {
        return sender.hasPermission(permission);
    }
}

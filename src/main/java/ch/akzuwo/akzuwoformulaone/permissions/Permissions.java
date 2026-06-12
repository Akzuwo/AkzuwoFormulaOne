package ch.akzuwo.akzuwoformulaone.permissions;

import org.bukkit.command.CommandSender;

public final class Permissions {

    public static final String USER = "akzuwoformulaone.user";
    public static final String USER_JOIN = "akzuwoformulaone.user.join";
    public static final String USER_LEAVE = "akzuwoformulaone.user.leave";
    public static final String USER_START = "akzuwoformulaone.user.start";
    public static final String USER_LEADERBOARD = "akzuwoformulaone.user.leaderboard";

    public static final String MOD = "akzuwoformulaone.mod";
    public static final String MOD_CANCEL = "akzuwoformulaone.mod.cancel";
    public static final String MOD_KICK = "akzuwoformulaone.mod.kick";
    public static final String MOD_TP = "akzuwoformulaone.mod.tp";
    public static final String MOD_INFO = "akzuwoformulaone.mod.info";

    public static final String ADMIN = "akzuwoformulaone.admin";
    public static final String ADMIN_RELOAD = "akzuwoformulaone.admin.reload";
    public static final String ADMIN_DEBUG = "akzuwoformulaone.admin.debug";
    public static final String ADMIN_RESOURCEPACK = "akzuwoformulaone.admin.resourcepack";
    public static final String ADMIN_PHYSICS = "akzuwoformulaone.admin.physics";
    public static final String ADMIN_ANALYTICS = "formulaboat.admin.analytics";
    public static final String ADMIN_ANALYTICS_TESTCRASH = "formulaboat.admin.analytics.testcrash";
    public static final String ADMIN_POINTS = "formulaboat.admin.points";
    public static final String ADMIN_POINTS_TEST = "formulaboat.admin.points.test";

    public static final String BUILDER = "akzuwoformulaone.builder";
    public static final String BUILDER_CREATE = "akzuwoformulaone.builder.create";
    public static final String BUILDER_EDIT = "akzuwoformulaone.builder.edit";
    public static final String BUILDER_CHECKPOINT = "akzuwoformulaone.builder.checkpoint";
    public static final String BUILDER_SAVE = "akzuwoformulaone.builder.save";
    public static final String BUILDER_ENABLE = "akzuwoformulaone.builder.enable";

    private Permissions() {
    }

    public static boolean has(CommandSender sender, String permission) {
        return sender.hasPermission(permission);
    }
}

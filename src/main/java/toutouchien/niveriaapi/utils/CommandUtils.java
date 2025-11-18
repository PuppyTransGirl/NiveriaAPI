package toutouchien.niveriaapi.utils;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.permissions.Permissible;

public class CommandUtils {
    private CommandUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean defaultRequirements(CommandSourceStack css, String permission) {
        return css.getSender().hasPermission(permission)
                && css.getExecutor() instanceof Permissible perm
                && perm.hasPermission(permission);
    }
}

package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for command-related operations.
 */
public class CommandUtils {
    private CommandUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Checks if the command source stack meets the default requirements for executing a command.
     * This includes checking if both the sender and executor have the specified permission.
     *
     * @param css        The command source stack to check.
     * @param permission The permission node to check against.
     * @return {@code true} if both the sender and executor have the specified permission, {@code false} otherwise.
     * @throws NullPointerException if css or permission is null.
     */
    public static boolean defaultRequirements(@NotNull CommandSourceStack css, @NotNull String permission) {
        Preconditions.checkNotNull(css, "css cannot be null");
        Preconditions.checkNotNull(permission, "permission cannot be null");

        return css.getSender().hasPermission(permission)
                && css.getExecutor() instanceof Permissible perm && perm.hasPermission(permission);
    }
}

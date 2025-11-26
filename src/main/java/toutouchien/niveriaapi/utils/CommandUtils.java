package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

public class CommandUtils {
    private CommandUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean defaultRequirements(@NotNull CommandSourceStack css, @NotNull String permission) {
        Preconditions.checkNotNull(css, "css cannot be null");
        Preconditions.checkNotNull(permission, "permission cannot be null");

        return css.getSender().hasPermission(permission)
                && css.getExecutor() instanceof Permissible perm && perm.hasPermission(permission);
    }
}

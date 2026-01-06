package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for command-related operations.
 */
public class CommandUtils {
    private CommandUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Checks if the command source stack meets the default requirements.
     *
     * @param css        The command source stack.
     * @param permission The required permission.
     * @return True if the requirements are met, false otherwise.
     */
    public static boolean defaultRequirements(@NotNull CommandSourceStack css, @NotNull String permission) {
        return defaultRequirements(css, permission, false);
    }

    /**
     * Checks if the command source stack meets the default requirements.
     *
     * @param css            The command source stack.
     * @param permission     The required permission.
     * @param requiresPlayer Whether the executor must be a player.
     * @return True if the requirements are met, false otherwise.
     */
    public static boolean defaultRequirements(@NotNull CommandSourceStack css, @NotNull String permission, boolean requiresPlayer) {
        Preconditions.checkNotNull(css, "css cannot be null");
        Preconditions.checkNotNull(permission, "permission cannot be null");

        CommandSender sender = css.getSender();
        Entity executor = css.getExecutor();
        if (requiresPlayer) {
            return sender.hasPermission(permission)
                    && executor instanceof Player player && player.hasPermission(permission);
        }

        return sender.hasPermission(permission)
                && (executor == null || executor.hasPermission(permission));
    }

    /**
     * Retrieves the command sender from the command context.
     *
     * @param ctx The command context.
     * @return The command sender.
     */
    public static CommandSender sender(@NotNull CommandContext<CommandSourceStack> ctx) {
        Preconditions.checkNotNull(ctx, "ctx cannot be null");
        CommandSourceStack css = ctx.getSource();
        return css.getExecutor() != null ? css.getExecutor() : css.getSender();
    }
}

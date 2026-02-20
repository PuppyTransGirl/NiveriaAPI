package toutouchien.niveriaapi.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.lang.Lang;
import toutouchien.niveriaapi.utils.CommandUtils;
import toutouchien.niveriaapi.utils.MathUtils;
import toutouchien.niveriaapi.utils.StringUtils;
import toutouchien.niveriaapi.utils.Task;

import java.util.List;
import java.util.Map;

import static toutouchien.niveriaapi.NiveriaAPI.LANG;

public final class NiveriaAPICommand {
    private NiveriaAPICommand() {
        throw new IllegalStateException("Command class");
    }

    public static LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("niveriaapi")
                .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi"))
                .then(NiveriaAPIDebugCommand.get())
                .then(fixCommandsCommand())
                .then(pingCommand())
                .then(reloadCommand())
                .build();
    }

    private static LiteralArgumentBuilder<CommandSourceStack> fixCommandsCommand() {
        return Commands.literal("fixcommands")
                .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi.fixcommands"))
                .then(Commands.argument("targets", ArgumentTypes.players())
                        .executes(ctx -> {
                            PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("targets", PlayerSelectorArgumentResolver.class);
                            List<Player> targets = targetResolver.resolve(ctx.getSource());
                            CommandSender sender = CommandUtils.sender(ctx);

                            for (Player target : targets)
                                target.updateCommands();

                            int playersNumber = targets.size();
                            String messageKey = "command.fixcommands.";
                            String finalMessageKey = StringUtils.pluralize(messageKey + "single", messageKey + "multiple", playersNumber);
                            LANG.sendMessage(sender, finalMessageKey,
                                    Lang.numberPlaceholder("player_amount", playersNumber)
                            );

                            return Command.SINGLE_SUCCESS;
                        })
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> pingCommand() {
        return Commands.literal("ping")
                .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi.ping"))
                .executes(ctx -> {
                    CommandSender sender = CommandUtils.sender(ctx);
                    Map<String, Long> pings = NiveriaAPI.instance().mongoManager().ping();

                    LANG.sendMessage(sender, "command.ping.header");

                    for (Map.Entry<String, Long> pingEntry : pings.entrySet()) {
                        String databaseName = pingEntry.getKey();
                        double pingInMilliseconds = pingEntry.getValue() / 1_000_000D;
                        LANG.sendMessage(sender, "command.ping.line",
                                Lang.unparsedPlaceholder("database_name", databaseName),
                                Lang.numberPlaceholder("ping_ms", pingInMilliseconds)
                        );
                    }

                    return Command.SINGLE_SUCCESS;
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> reloadCommand() {
        return Commands.literal("reload")
                .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi.reload"))
                .executes(ctx -> {
                    CommandSender sender = CommandUtils.sender(ctx);

                    LANG.sendMessage(sender, "command.reload.start");
                    long startNanos = System.nanoTime();

                    Task.async(task -> {
                        try {
                            NiveriaAPI.instance().reload();
                            double timeTaken = (System.nanoTime() - startNanos) / 1_000_000D;

                            LANG.sendMessage(sender, "command.reload.done",
                                    Lang.numberPlaceholder("time_ms", MathUtils.decimalRound(timeTaken, 2))
                            );
                        } catch (Exception e) {
                            NiveriaAPI.instance().getSLF4JLogger().error("Failed to reload NiveriaAPI", e);
                            LANG.sendMessage(sender, "command.reload.error");
                        }
                    }, NiveriaAPI.instance());

                    return Command.SINGLE_SUCCESS;
                });
    }
}

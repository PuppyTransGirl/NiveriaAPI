package toutouchien.niveriaapi.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.lang.Lang;
import toutouchien.niveriaapi.utils.CommandUtils;
import toutouchien.niveriaapi.utils.StringUtils;

import java.util.List;
import java.util.Map;

public class NiveriaAPICommand {
    private NiveriaAPICommand() {
        throw new IllegalStateException("Command class");
    }

    public static LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("niveriaapi")
                .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi"))
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
                            Entity executor = ctx.getSource().getExecutor();

                            for (Player target : targets)
                                target.updateCommands();

                            int playersNumber = targets.size();
                            String messageKey = "niveriaapi.fixcommands.";
                            String finalMessageKey = StringUtils.pluralize(messageKey + "single", messageKey + "multiple", playersNumber);
                            Lang.sendMessage(executor, finalMessageKey, playersNumber);

                            return Command.SINGLE_SUCCESS;
                        })
                );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> pingCommand() {
        return Commands.literal("ping")
                .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi.ping"))
                .executes(ctx -> {
                    Entity executor = ctx.getSource().getExecutor();
                    Map<String, Long> pings = NiveriaAPI.instance().mongoManager().ping();

                    Lang.sendMessage(executor, "niveriaapi.ping.header");

                    for (Map.Entry<String, Long> pingEntry : pings.entrySet()) {
                        String databaseName = pingEntry.getKey();
                        double pingInMilliseconds = pingEntry.getValue() / 1_000_000D;
                        Lang.sendMessage(executor, "niveriaapi.ping.line", databaseName, pingInMilliseconds);
                    }

                    return Command.SINGLE_SUCCESS;
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> reloadCommand() {
        return Commands.literal("reload")
                .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi.reload"))
                .executes(ctx -> {
                    Entity executor = ctx.getSource().getExecutor();

                    long startMillis = System.currentTimeMillis();
                    NiveriaAPI.instance().reload();
                    long timeTaken = System.currentTimeMillis() - startMillis;
                    Lang.sendMessage(executor, "niveriaapi.reload.done", timeTaken);

                    return Command.SINGLE_SUCCESS;
                });
    }
}

package toutouchien.niveriaapi.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.utils.ColorUtils;
import toutouchien.niveriaapi.utils.CommandUtils;
import toutouchien.niveriaapi.utils.MessageUtils;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

public class NiveriaAPICommand {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    public static LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("niveriaapi")
                .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi"))
                .then(Commands.literal("fixcommands")
                        .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi.fixcommands"))
                        .then(Commands.argument("targets", ArgumentTypes.players())
                                .executes(ctx -> {
                                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("targets", PlayerSelectorArgumentResolver.class);
                                    List<Player> targets = targetResolver.resolve(ctx.getSource());
                                    Entity executor = ctx.getSource().getExecutor();

                                    for (Player target : targets)
                                        target.updateCommands();

                                    MessageUtils.sendSuccessMessage(executor, Component.text("Vous avez rechargé les commandes de %s".formatted(targets.size())));

                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("ping")
                        .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi.ping"))
                        .executes(ctx -> {
                            Entity executor = ctx.getSource().getExecutor();
                            Map<String, Long> pings = NiveriaAPI.instance().mongoManager().ping();

                            MessageUtils.sendInfoMessage(executor, Component.text("Ping des bases de données:"));

                            for (Map.Entry<String, Long> pingEntry : pings.entrySet()) {
                                double pingInMilliseconds = pingEntry.getValue() / 1_000_000D;
                                String formattedPing = DECIMAL_FORMAT.format(pingInMilliseconds);

                                executor.sendMessage(
                                        Component.text(pingEntry.getKey(), ColorUtils.primaryColor())
                                                .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                                                .append(Component.text(formattedPing + " ms"))
                                );
                            }

                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("reload")
                        .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi.reload"))
                        .executes(ctx -> {
                            Entity executor = ctx.getSource().getExecutor();

                            long startMillis = System.currentTimeMillis();
                            NiveriaAPI.instance().reload();
                            long timeTaken = System.currentTimeMillis() - startMillis;
                            MessageUtils.sendSuccessMessage(executor, Component.text("NiveriaAPI a été rechargé avec succès ! (%s ms)".formatted(timeTaken)));

                            return Command.SINGLE_SUCCESS;
                        })
                ).build();
    }
}

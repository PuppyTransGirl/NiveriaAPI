package toutouchien.homeplugin.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import toutouchien.homeplugin.HomePlugin;
import toutouchien.homeplugin.managers.HomeManager;
import toutouchien.homeplugin.models.Home;
import toutouchien.niveriaapi.lang.Lang;
import toutouchien.niveriaapi.utils.CommandUtils;

import java.util.UUID;

public class DeleteHomeCommand {
    private DeleteHomeCommand() {
        throw new IllegalStateException("Command class");
    }

    public static LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("deletehome")
                .requires(css -> CommandUtils.defaultRequirements(css, "homeplugin.command.deletehome", true))
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            HomeManager homeManager = HomePlugin.instance().homeManager();
                            Player player = (Player) ctx.getSource().getExecutor();
                            UUID uuid = player.getUniqueId();

                            for (Home home : homeManager.homes(uuid))
                                builder.suggest(home.name());

                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            HomeManager homeManager = HomePlugin.instance().homeManager();
                            Player player = (Player) ctx.getSource().getExecutor();
                            UUID uuid = player.getUniqueId();

                            String homeName = ctx.getArgument("name", String.class);
                            if (!homeManager.homeExists(uuid, homeName)) {
                                Lang.sendMessage(player, "homeplugin.deletehome.doesnt_exists");
                                return Command.SINGLE_SUCCESS;
                            }

                            homeManager.deleteHome(uuid, homeName);
                            Lang.sendMessage(player, "homeplugin.deletehome.deleted", homeName);
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }
}

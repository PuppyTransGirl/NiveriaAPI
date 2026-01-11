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

public class HomeCommand {
    private HomeCommand() {
        throw new IllegalStateException("Command class");
    }

    public static LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("home")
                .requires(css -> CommandUtils.defaultRequirements(css, "homeplugin.command.home", true))
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
                            Home home = homeManager.home(uuid, homeName);
                            if (home == null) {
                                Lang.sendMessage(player, "homeplugin.home.doesnt_exists");
                                return Command.SINGLE_SUCCESS;
                            }

                            homeManager.teleportHome(player, home);
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }
}

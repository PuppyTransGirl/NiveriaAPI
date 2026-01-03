package toutouchien.homeplugin.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import toutouchien.homeplugin.HomePlugin;
import toutouchien.homeplugin.managers.HomeManager;
import toutouchien.niveriaapi.lang.Lang;
import toutouchien.niveriaapi.utils.CommandUtils;

import java.util.UUID;

public class SetHomeCommand {
    private SetHomeCommand() {
        throw new IllegalStateException("Command class");
    }

    public static LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("sethome")
                .requires(css -> CommandUtils.defaultRequirements(css, "homeplugin.command.sethome"))
                .requires(CommandUtils::playerExecutorRequirement)
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> {
                            HomeManager homeManager = HomePlugin.instance().homeManager();
                            Player player = (Player) ctx.getSource().getExecutor();
                            UUID uuid = player.getUniqueId();

                            String homeName = ctx.getArgument("name", String.class);
                            if (homeName.contains(".") || homeName.contains("+")) {
                                Lang.sendMessage(player, "homeplugin.sethome.invalid_character");
                                return Command.SINGLE_SUCCESS;
                            }

                            if (homeName.length() > 20) {
                                Lang.sendMessage(player, "homeplugin.sethome.too_long");
                                return Command.SINGLE_SUCCESS;
                            }

                            if (homeManager.homeExists(uuid, homeName)) {
                                Lang.sendMessage(player, "homeplugin.sethome.already_exists");
                                return Command.SINGLE_SUCCESS;
                            }

                            homeManager.createHome(player, homeName);
                            Lang.sendMessage(player, "homeplugin.sethome.created", homeName);
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }
}

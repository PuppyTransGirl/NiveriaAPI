package toutouchien.homeplugin.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import toutouchien.homeplugin.HomePlugin;
import toutouchien.homeplugin.managers.HomeManager;
import toutouchien.homeplugin.menus.HomesMenu;
import toutouchien.niveriaapi.utils.CommandUtils;

import java.util.UUID;

import static toutouchien.homeplugin.HomePlugin.LANG;

public class HomesCommand {
    private HomesCommand() {
        throw new IllegalStateException("Command class");
    }

    public static LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("homes")
                .requires(css -> CommandUtils.defaultRequirements(css, "homeplugin.command.homes", true))
                .executes(ctx -> {
                    HomeManager homeManager = HomePlugin.instance().homeManager();
                    Player player = (Player) ctx.getSource().getExecutor();
                    UUID uuid = player.getUniqueId();

                    if (homeManager.homes(uuid).isEmpty()) {
                        player.sendMessage(LANG.get("homeplugin.homes.no_homes"));
                        return Command.SINGLE_SUCCESS;
                    }

                    new HomesMenu(player, homeManager).open();
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}

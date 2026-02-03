package toutouchien.homeplugin.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import toutouchien.homeplugin.HomePlugin;
import toutouchien.niveriaapi.lang.Lang;
import toutouchien.niveriaapi.utils.CommandUtils;

import static toutouchien.homeplugin.HomePlugin.LANG;

public class HomePluginCommand {
    private HomePluginCommand() {
        throw new IllegalStateException("Command class");
    }

    public static LiteralCommandNode<CommandSourceStack> get() {
        return Commands.literal("homeplugin")
                .requires(css -> CommandUtils.defaultRequirements(css, "homeplugin.command.homeplugin"))
                .then(reloadCommand())
                .build();
    }

    private static LiteralArgumentBuilder<CommandSourceStack> reloadCommand() {
        return Commands.literal("reload")
                .requires(css -> CommandUtils.defaultRequirements(css, "homeplugin.command.homeplugin.reload"))
                .executes(ctx -> {
                    CommandSender sender = CommandUtils.sender(ctx);

                    long startMillis = System.currentTimeMillis();
                    HomePlugin.instance().reload();
                    long timeTaken = System.currentTimeMillis() - startMillis;
                    LANG.sendMessage(sender, "homeplugin.reload.done",
                            Lang.numberPlaceholder("homeplugin_time_ms", timeTaken)
                    );

                    return Command.SINGLE_SUCCESS;
                });
    }
}

package toutouchien.homeplugin.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Entity;
import toutouchien.homeplugin.HomePlugin;
import toutouchien.niveriaapi.lang.Lang;
import toutouchien.niveriaapi.utils.CommandUtils;

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
                    Entity executor = ctx.getSource().getExecutor();

                    long startMillis = System.currentTimeMillis();
                    HomePlugin.instance().reload();
                    long timeTaken = System.currentTimeMillis() - startMillis;
                    Lang.sendMessage(executor, "homeplugin.reload.done", timeTaken);

                    return Command.SINGLE_SUCCESS;
                });
    }
}

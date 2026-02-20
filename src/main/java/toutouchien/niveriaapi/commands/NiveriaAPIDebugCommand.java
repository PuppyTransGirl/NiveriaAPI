package toutouchien.niveriaapi.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import toutouchien.niveriaapi.utils.CommandUtils;

import static toutouchien.niveriaapi.NiveriaAPI.LANG;

public final class NiveriaAPIDebugCommand {
    private NiveriaAPIDebugCommand() {
        throw new IllegalStateException("Command class");
    }

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("debug")
                .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi.debug"))
                .then(NiveriaAPIDebugMenuCommand.get())
                .then(testMessageCommand());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> testMessageCommand() {
        return Commands.literal("testmessage")
                .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi.debug.testmessage"))
                .then(Commands.argument("key", StringArgumentType.word())
                        .executes(ctx -> {
                            CommandSender sender = CommandUtils.sender(ctx);
                            String key = ctx.getArgument("key", String.class);

                            LANG.sendMessage(sender, key);

                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
}

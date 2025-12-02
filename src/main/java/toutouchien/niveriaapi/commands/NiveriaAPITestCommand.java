package toutouchien.niveriaapi.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import toutouchien.niveriaapi.menu.test.TestMenu;
import toutouchien.niveriaapi.utils.CommandUtils;

public class NiveriaAPITestCommand {
    private NiveriaAPITestCommand() {
        throw new IllegalStateException("Command class");
    }

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("test")
                .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi.test"))
                .requires(css -> css.getExecutor() instanceof Player)
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getExecutor();
                    TestMenu menu = new TestMenu(player);
                    menu.open();

                    return Command.SINGLE_SUCCESS;
                });
    }
}

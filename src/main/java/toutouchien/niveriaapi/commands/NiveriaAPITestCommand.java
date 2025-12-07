package toutouchien.niveriaapi.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import toutouchien.niveriaapi.menu.component.premade.ConfirmationMenu;
import toutouchien.niveriaapi.menu.test.TestMenu;
import toutouchien.niveriaapi.utils.CommandUtils;
import toutouchien.niveriaapi.utils.ItemBuilder;

public class NiveriaAPITestCommand {
    private NiveriaAPITestCommand() {
        throw new IllegalStateException("Command class");
    }

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        return Commands.literal("test")
                .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi.test"))
                .requires(css -> css.getExecutor() instanceof Player)
                .then(Commands.literal("basic")
                        .executes(ctx -> {
                            Player player = (Player) ctx.getSource().getExecutor();
                            TestMenu menu = new TestMenu(player);
                            menu.open();

                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("confirmation")
                        .executes(ctx -> {
                            Player player = (Player) ctx.getSource().getExecutor();
                            ConfirmationMenu menu = new ConfirmationMenu(
                                    player,
                                    Component.text("Are you sure ?"),
                                    Component.text("Yes", NamedTextColor.GREEN),
                                    Component.text("No", NamedTextColor.RED),
                                    ItemBuilder.of(Material.OAK_SIGN).name(Component.text("This action is irreversible")).build(),
                                    event -> event.player().sendRichMessage("You clicked <green>Yes"),
                                    event -> event.player().sendRichMessage("You clicked <red>No")
                            );
                            menu.open();

                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
}

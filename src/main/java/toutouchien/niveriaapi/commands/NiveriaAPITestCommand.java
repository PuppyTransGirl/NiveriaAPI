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
import toutouchien.niveriaapi.menu.test.PaginatedTestMenu;
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
                .then(basicCommand())
                .then(confirmationCommand())
                .then(paginatorCommand());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> basicCommand() {
        return Commands.literal("basic")
                .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi.test.basic"))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getExecutor();
                    TestMenu menu = new TestMenu(player);
                    menu.open();

                    return Command.SINGLE_SUCCESS;
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> confirmationCommand() {
        return Commands.literal("confirmation")
                .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi.test.confirmation"))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getExecutor();
                    ConfirmationMenu menu = new ConfirmationMenu(
                            player,
                            Component.text("Are you sure ?"),
                            ItemBuilder.of(Material.LIME_DYE).name(Component.text("Yes", NamedTextColor.GREEN)).build(),
                            ItemBuilder.of(Material.RED_DYE).name(Component.text("No", NamedTextColor.RED)).build(),
                            ItemBuilder.of(Material.OAK_SIGN).name(Component.text("This action is irreversible")).build(),
                            event -> event.player().sendRichMessage("You clicked <green>Yes"),
                            event -> event.player().sendRichMessage("You clicked <red>No")
                    );
                    menu.open();

                    return Command.SINGLE_SUCCESS;
                });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> paginatorCommand() {
        return Commands.literal("paginator")
                .requires(css -> CommandUtils.defaultRequirements(css, "niveriaapi.command.niveriaapi.test.paginator"))
                .executes(ctx -> {
                    Player player = (Player) ctx.getSource().getExecutor();
                    PaginatedTestMenu menu = new PaginatedTestMenu(player);
                    menu.open();

                    return Command.SINGLE_SUCCESS;
                });
    }
}

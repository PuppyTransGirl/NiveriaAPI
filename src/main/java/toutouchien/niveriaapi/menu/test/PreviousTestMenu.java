package toutouchien.niveriaapi.menu.test;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.Menu;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.MenuComponent;
import toutouchien.niveriaapi.menu.component.display.Icon;
import toutouchien.niveriaapi.menu.component.interactive.Button;
import toutouchien.niveriaapi.menu.component.layout.Grid;
import toutouchien.niveriaapi.utils.ColorUtils;
import toutouchien.niveriaapi.utils.ItemBuilder;

/**
 * Example menu using the previousMenus system.
 */
public class PreviousTestMenu extends Menu {

    public PreviousTestMenu(Player player) {
        super(player);
    }

    public PreviousTestMenu(Player player, MenuContext context) {
        super(player, context);
    }

    @Override
    protected @NotNull Component title() {
        return Component.text("Menu ID: " + System.identityHashCode(this), ColorUtils.primaryColor());
    }

    @Override
    protected @NotNull MenuComponent root(@NotNull MenuContext context) {
        return Grid.create()
                .size(9, 3)
                .add(context, 0, previousMenuButton())
                .add(context, 4, Icon.create()
                        .item(ItemBuilder.of(Material.BOOK)
                                .name(Component.text("Current Menu ID: " + System.identityHashCode(this)))
                                .build())
                        .build())
                .add(context, 8, nextMenuButton())
                .build();
    }

    private static Button previousMenuButton() {
        return Button.create()
                .item(ItemBuilder.of(Material.ARROW)
                        .name(Component.text("Go to Previous Menu"))
                        .build())
                .onClick(event -> {
                    Menu previous = event.context().previousMenu();
                    if (previous == null) {
                        event.player().sendMessage(Component.text("No previous menu found!", NamedTextColor.RED));
                        return;
                    }

                    previous.open();
                })
                .build();
    }

    private static Button nextMenuButton() {
        return Button.create()
                .item(ItemBuilder.of(Material.ARROW)
                        .name(Component.text("Go to Next Menu"))
                        .build())
                .onClick(event -> {
                    new PreviousTestMenu(event.player(), event.context()).open();
                })
                .build();
    }
}
package toutouchien.homeplugin.menus;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import toutouchien.homeplugin.managers.HomeManager;
import toutouchien.homeplugin.models.Home;
import toutouchien.niveriaapi.lang.Lang;
import toutouchien.niveriaapi.menu.Menu;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.MenuComponent;
import toutouchien.niveriaapi.menu.component.container.Paginator;
import toutouchien.niveriaapi.menu.component.interactive.DoubleDropButton;
import toutouchien.niveriaapi.menu.component.layout.Grid;
import toutouchien.niveriaapi.utils.ItemBuilder;

public class HomesMenu extends Menu {
    private final HomeManager homeManager;

    /**
     * Constructs a new Menu for the specified player.
     *
     * @param player the player who will interact with this menu
     * @throws NullPointerException if player is null
     */
    public HomesMenu(@NotNull Player player, @NotNull HomeManager homeManager) {
        super(player);

        this.homeManager = homeManager;
    }

    /**
     * Returns the title component for this menu's inventory.
     * <p>
     * This method must be implemented by subclasses to define the menu's title.
     *
     * @return the title component displayed at the top of the inventory
     */
    @Override
    protected @NotNull Component title() {
        return Lang.get("homeplugin.menu.title");
    }

    /**
     * Creates and returns the root component for this menu.
     * <p>
     * This method must be implemented by subclasses to define the menu's layout
     * and components.
     *
     * @param context the menu context for component interaction
     * @return the root component that defines the menu's structure
     */
    @Override
    protected @NotNull MenuComponent root(@NotNull MenuContext context) {
        Paginator paginator = Paginator.create()
                .size(5, 2)
                .nextItem(ItemBuilder.of(Material.ARROW).name(Lang.get("homeplugin.menu.next")).build())
                .backItem(ItemBuilder.of(Material.ARROW).name(Lang.get("homeplugin.menu.previous")).build())
                .build();

        for (Home home : this.homeManager.homes(context.player().getUniqueId())) {
            DoubleDropButton button = DoubleDropButton.create()
                    .item(ItemBuilder.of(home.icon())
                            .name(Lang.get("homeplugin.menu.home.name", home.name()))
                            .lore(
                                    Lang.get("homeplugin.menu.home.lore.0"),
                                    Lang.get("homeplugin.menu.home.lore.1"),
                                    Lang.get("homeplugin.menu.home.lore.2")
                            )
                            .build()
                    )
                    .dropItem(ItemBuilder.of(Material.BARRIER)
                            .name(Lang.get("homeplugin.menu.home_dropped.name", home.name()))
                            .lore(
                                    Lang.get("homeplugin.menu.home_dropped.lore.0"),
                                    Lang.get("homeplugin.menu.home_dropped.lore.1")
                            )
                            .build()
                    )
                    .onLeftClick(event -> {
                        this.homeManager.teleportHome(context.player(), home);
                        context.player().closeInventory();
                    })
                    .onRightClick(event -> {
                        new ItemSelectorMenu(this.player(), event.context(), selectedMaterial -> {
                            home.icon(selectedMaterial);
                            MenuContext ctx = event.context();
                            paginator.render(ctx);
                            open();
                        }).open();
                    })
                    .onDoubleDrop(event -> {
                        this.homeManager.deleteHome(context.player().getUniqueId(), home);

                        MenuContext ctx = event.context();
                        paginator.remove(ctx, event.slot());
                        paginator.render(ctx);
                    })
                    .build();

            paginator.add(context, button);
        }

        return Grid.create()
                .size(9, 5)
                .add(context, 11, paginator)
                .add(context, 36, paginator.backButton())
                .add(context, 44, paginator.nextButton())
                .build();
    }
}

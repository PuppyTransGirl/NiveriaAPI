package toutouchien.niveriaapi.menu.test;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import toutouchien.niveriaapi.menu.Menu;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.MenuComponent;
import toutouchien.niveriaapi.menu.component.container.Paginator;
import toutouchien.niveriaapi.menu.component.interactive.Button;
import toutouchien.niveriaapi.menu.component.layout.Grid;
import toutouchien.niveriaapi.utils.ColorUtils;

import java.util.Arrays;

/**
 * A test menu demonstrating paginated content functionality.
 * <p>
 * This menu displays a paginated grid of all available Material items,
 * filtered to exclude legacy items, non-items (like piston heads),
 * disabled experimental features, and air blocks. The pagination controls
 * are positioned at the bottom of the menu for easy navigation.
 */
@NullMarked
public class PaginatedTestMenu extends Menu {

    /**
     * Constructs a new PaginatedTestMenu for the specified player.
     *
     * @param player the player who will view this menu
     */
    public PaginatedTestMenu(Player player) {
        super(player);
    }

    /**
     * Returns the title component for this test menu.
     *
     * @return a colorized title component
     */
    @Override
    protected Component title() {
        return Component.text("Paginated Test Menu Hehe :3", ColorUtils.primaryColor());
    }

    /**
     * Creates and returns the root component for this paginated test menu.
     * <p>
     * The menu structure consists of:
     * - A 7x3 paginator area starting at position (10) containing filtered Material buttons
     * - Navigation controls at the bottom row:
     * - First page button at slot 45
     * - Back button at slot 46
     * - Next button at slot 52
     * - Last page button at slot 53
     *
     * @param context the menu context
     * @return the root grid component containing the paginator and navigation controls
     */
    @Override
    protected MenuComponent root(MenuContext context) {
        Paginator paginator = Paginator.create()
                .size(7, 3)
                .firstPageItem(ItemStack.of(Material.SPECTRAL_ARROW))
                .lastPageItem(ItemStack.of(Material.SPECTRAL_ARROW))
                .offBackItem(ItemStack.of(Material.RED_DYE))
                .offNextItem(ItemStack.of(Material.RED_DYE))
                .offFirstPageItem(ItemStack.of(Material.ORANGE_DYE))
                .offLastPageItem(ItemStack.of(Material.ORANGE_DYE))
                .build();

        World world = this.player().getWorld();
        ObjectList<MenuComponent> materials = Arrays.stream(Material.values())
                .filter(material -> !material.isLegacy())
                .filter(Material::isItem) // Remove things like Piston Head
                .filter(material -> world.isEnabled(material.asItemType())) // Remove disabled experimental features
                .filter(material -> !material.isAir())
                .map(ItemStack::of)
                .map(itemStack -> {
                    return Button.create()
                            .item(itemStack)
                            .onClick(event -> {
                                event.player().sendMessage(Component.translatable(event.getCurrentItem().translationKey()));

                                MenuContext ctx = event.context();
                                paginator.remove(ctx, event.slot());
                                paginator.render(ctx);
                            })
                            .build();
                })
                .collect(ObjectArrayList.toList());

        paginator.addAll(context, materials);

        return Grid.create()
                .size(9, 6)
                .add(45, paginator.firstPageButton())
                .add(46, paginator.backButton())
                .add(52, paginator.nextButton())
                .add(53, paginator.lastPageButton())
                .add(10, paginator)
                .build();
    }
}
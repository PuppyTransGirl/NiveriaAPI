package toutouchien.niveriaapi.menu.test;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.Menu;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.container.Paginator;
import toutouchien.niveriaapi.menu.component.interactive.Button;
import toutouchien.niveriaapi.menu.component.layout.Grid;
import toutouchien.niveriaapi.utils.ColorUtils;

import java.util.Arrays;

public class PaginatedTestMenu extends Menu {
    public PaginatedTestMenu(Player player) {
        super(player);
    }

    @NotNull
    @Override
    protected Component title() {
        return Component.text("Paginated Test Menu Hehe :3", ColorUtils.primaryColor());
    }

    @NotNull
    @Override
    protected toutouchien.niveriaapi.menu.component.Component root(@NotNull MenuContext context) {
        Paginator.Builder builder = Paginator.create()
                .firstPageItem(ItemStack.of(Material.SPECTRAL_ARROW))
                .lastPageItem(ItemStack.of(Material.SPECTRAL_ARROW))
                .offBackItem(ItemStack.of(Material.RED_DYE))
                .offNextItem(ItemStack.of(Material.RED_DYE))
                .offFirstPageItem(ItemStack.of(Material.ORANGE_DYE))
                .offLastPageItem(ItemStack.of(Material.ORANGE_DYE))
                .size(7, 3);

        World world = this.player().getWorld();
        ObjectList<toutouchien.niveriaapi.menu.component.Component> materials = Arrays.stream(Material.values())
                .filter(material -> !material.isLegacy())
                .filter(Material::isItem) // Remove things like Piston Head
                .filter(material -> world.isEnabled(material.asItemType())) // Remove disabled experimental features
                .filter(material -> !material.isAir())
                .map(ItemStack::of)
                .map(itemStack -> Button.create().item(itemStack).build())
                .collect(ObjectArrayList.toList());

        builder.addAll(materials);

        Paginator paginator = builder.build();

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

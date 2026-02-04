package toutouchien.homeplugin.menus;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import toutouchien.niveriaapi.menu.Menu;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.MenuComponent;
import toutouchien.niveriaapi.menu.component.container.Paginator;
import toutouchien.niveriaapi.menu.component.interactive.Button;
import toutouchien.niveriaapi.menu.component.layout.Grid;
import toutouchien.niveriaapi.utils.ItemBuilder;

import java.util.Arrays;
import java.util.function.Consumer;

import static toutouchien.homeplugin.HomePlugin.LANG;

public class ItemSelectorMenu extends Menu {
    private final Consumer<Material> materialConsumer;

    public ItemSelectorMenu(Player player, Consumer<Material> materialConsumer) {
        super(player);

        this.materialConsumer = materialConsumer;
    }

    public ItemSelectorMenu(@NonNullPlayer player, @NonNull MenuContext context, Consumer<Material> materialConsumer) {
        super(player, context);

        this.materialConsumer = materialConsumer;
    }

    @NonNull
    @Override
    protected Component title() {
        return LANG.get("homeplugin.menu_item_selector.title");
    }

    @NonNull
    @Override
    protected MenuComponent root(@NotNull MenuContext context) {
        World world = this.player().getWorld();
        Paginator paginator = Paginator.create()
                .size(7, 3)
                .nextItem(ItemBuilder.of(Material.ARROW).name(LANG.get("homeplugin.menu_item_selector.next")).build())
                .backItem(ItemBuilder.of(Material.ARROW).name(LANG.get("homeplugin.menu_item_selector.previous")).build())
                .firstPageItem(ItemBuilder.of(Material.SPECTRAL_ARROW).name(LANG.get("homeplugin.menu_item_selector.first_page")).build())
                .lastPageItem(ItemBuilder.of(Material.SPECTRAL_ARROW).name(LANG.get("homeplugin.menu_item_selector.last_page")).build())
                .addAll(context,
                        Arrays.stream(Material.values())
                                .filter(material -> !material.isLegacy())
                                .filter(Material::isItem) // Remove things like Piston Head
                                .filter(material -> world.isEnabled(material.asItemType())) // Remove disabled experimental features
                                .filter(material -> !material.isAir())
                                .map(ItemStack::of)
                                .map(itemStack -> {
                                    return Button.create()
                                            .item(itemStack)
                                            .onClick(event -> {
                                                this.materialConsumer.accept(event.getCurrentItem().getType());
                                            })
                                            .build();
                                })
                                .collect(ObjectArrayList.toList())
                )
                .build();

        return Grid.create()
                .size(9, 6)
                .add(context, 10, paginator)
                .add(context, 45, paginator.firstPageButton())
                .add(context, 46, paginator.backButton())
                .add(context, 52, paginator.nextButton())
                .add(context, 53, paginator.lastPageButton())
                .build();
    }
}
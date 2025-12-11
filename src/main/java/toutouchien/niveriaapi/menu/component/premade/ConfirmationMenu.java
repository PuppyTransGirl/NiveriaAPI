package toutouchien.niveriaapi.menu.component.premade;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.menu.Menu;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.display.Icon;
import toutouchien.niveriaapi.menu.component.interactive.Button;
import toutouchien.niveriaapi.menu.component.layout.Grid;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;
import toutouchien.niveriaapi.utils.ItemBuilder;

import java.util.function.Consumer;

public class ConfirmationMenu extends Menu {
    private final Component title, yesTitle, noTitle;
    private final ItemStack explanationItem;
    private final Consumer<NiveriaInventoryClickEvent> yesConsumer, noConsumer;

    public ConfirmationMenu(
            @NotNull Player player,
            @NotNull Component title, @NotNull Component yesTitle, @NotNull Component noTitle,
            @Nullable ItemStack explanationItem,
            @NotNull Consumer<NiveriaInventoryClickEvent> yesConsumer, @NotNull Consumer<NiveriaInventoryClickEvent> noConsumer
    ) {
        super(player);

        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(title, "title cannot be null");
        Preconditions.checkNotNull(yesTitle, "yesTitle cannot be null");
        Preconditions.checkNotNull(noTitle, "noTitle cannot be null");
        Preconditions.checkNotNull(yesConsumer, "yesConsumer cannot be null");
        Preconditions.checkNotNull(noConsumer, "noConsumer cannot be null");

        this.title = title;
        this.yesTitle = yesTitle;
        this.noTitle = noTitle;

        this.explanationItem = explanationItem;

        this.yesConsumer = yesConsumer;
        this.noConsumer = noConsumer;
    }

    @NotNull
    @Override
    protected Component title() {
        return this.title;
    }

    @NotNull
    @Override
    protected toutouchien.niveriaapi.menu.component.Component root(@NotNull MenuContext context) {
        Grid.Builder builder = Grid.create()
                .size(9, 3)
                .add(11, noButton())
                .add(15, yesButton());

        if (this.explanationItem != null)
            builder.add(13, explanationIcon());

        return builder.build();
    }

    @NotNull
    private Button yesButton() {
        ItemStack yesItem = ItemBuilder.of(Material.LIME_DYE)
                .name(this.yesTitle)
                .build();

        return Button.create()
                .item(yesItem)
                .onClick(this.yesConsumer)
                .build();
    }

    @NotNull
    private Button noButton() {
        ItemStack noItem = ItemBuilder.of(Material.RED_DYE)
                .name(this.noTitle)
                .build();

        return Button.create()
                .item(noItem)
                .onClick(this.noConsumer)
                .build();
    }

    @NotNull
    private Icon explanationIcon() {
        return Icon.create()
                .item(this.explanationItem)
                .build();
    }
}

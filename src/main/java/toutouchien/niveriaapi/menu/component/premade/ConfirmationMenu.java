package toutouchien.niveriaapi.menu.component.premade;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.menu.Menu;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.MenuComponent;
import toutouchien.niveriaapi.menu.component.display.Icon;
import toutouchien.niveriaapi.menu.component.interactive.Button;
import toutouchien.niveriaapi.menu.component.layout.Grid;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

import java.util.function.Consumer;

/**
 * A pre-made menu for displaying confirmation dialogs with Yes/No options.
 * <p>
 * This menu presents a 3-row interface with customizable yes and no buttons,
 * an optional explanation item, and configurable click handlers for each option.
 * The layout positions the no button on the left (slot 11), yes button on the right (slot 15),
 * and the optional explanation item in the center (slot 13).
 */
public class ConfirmationMenu extends Menu {
    private final Component title;
    private final ItemStack yesItem, noItem;
    private final ItemStack explanationItem;
    private final Consumer<NiveriaInventoryClickEvent> yesConsumer, noConsumer;

    /**
     * Constructs a new ConfirmationMenu with the specified parameters.
     *
     * @param player          the player who will view this confirmation menu
     * @param yesItem         the ItemStack to display for the "yes" button
     * @param noItem          the ItemStack to display for the "no" button
     * @param title           the title component displayed at the top of the menu
     * @param explanationItem optional ItemStack to display as an explanation (may be null)
     * @param yesConsumer     the action to perform when the yes button is clicked
     * @param noConsumer      the action to perform when the no button is clicked
     * @throws NullPointerException if any required parameter is null
     */
    public ConfirmationMenu(
            @NotNull Player player,
            @NotNull Component title,
            @NotNull ItemStack yesItem, @NotNull ItemStack noItem,
            @Nullable ItemStack explanationItem,
            @NotNull Consumer<NiveriaInventoryClickEvent> yesConsumer, @NotNull Consumer<NiveriaInventoryClickEvent> noConsumer
    ) {
        super(player);

        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(title, "title cannot be null");
        Preconditions.checkNotNull(yesItem, "yesMaterial cannot be null");
        Preconditions.checkNotNull(noItem, "noMaterial cannot be null");
        Preconditions.checkNotNull(yesConsumer, "yesConsumer cannot be null");
        Preconditions.checkNotNull(noConsumer, "noConsumer cannot be null");

        this.title = title;

        this.yesItem = yesItem;
        this.noItem = noItem;
        this.explanationItem = explanationItem;

        this.yesConsumer = yesConsumer;
        this.noConsumer = noConsumer;
    }

    /**
     * Returns the title component for this confirmation menu.
     *
     * @return the title component
     */
    @NotNull
    @Override
    protected Component title() {
        return this.title;
    }

    /**
     * Creates and returns the root component for this confirmation menu.
     * <p>
     * The root component is a 9x3 grid containing:
     * - No button at position 11 (left side)
     * - Yes button at position 15 (right side)
     * - Optional explanation icon at position 13 (center)
     *
     * @param context the menu context
     * @return the root grid component containing all menu elements
     */
    @NotNull
    @Override
    protected MenuComponent root(@NotNull MenuContext context) {
        Grid.Builder builder = Grid.create()
                .size(9, 3)
                .add(11, noButton())
                .add(15, yesButton());

        if (this.explanationItem != null)
            builder.add(13, explanationIcon());

        return builder.build();
    }

    /**
     * Creates the yes button component.
     *
     * @return a button component configured with the yes item and click handler
     */
    @NotNull
    private Button yesButton() {
        return Button.create()
                .item(this.yesItem)
                .onClick(this.yesConsumer)
                .build();
    }

    /**
     * Creates the no button component.
     *
     * @return a button component configured with the no item and click handler
     */
    @NotNull
    private Button noButton() {
        return Button.create()
                .item(this.noItem)
                .onClick(this.noConsumer)
                .build();
    }

    /**
     * Creates the explanation icon component.
     *
     * @return an icon component displaying the explanation item
     */
    @NotNull
    private Icon explanationIcon() {
        return Icon.create()
                .item(this.explanationItem)
                .build();
    }
}
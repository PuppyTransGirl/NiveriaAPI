package toutouchien.niveriaapi.menu.event;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.utils.ItemBuilder;

import java.util.function.Consumer;

/**
 * A custom InventoryClickEvent that includes additional context for menu interactions.
 */
public class NiveriaInventoryClickEvent extends InventoryClickEvent {
    private final MenuContext context;

    /**
     * Constructs a new NiveriaInventoryClickEvent.
     *
     * @param event   The original InventoryClickEvent.
     * @param context The MenuContext associated with this event.
     */
    @SuppressWarnings("UnstableApiUsage")
    public NiveriaInventoryClickEvent(@NotNull InventoryClickEvent event, @NotNull MenuContext context) {
        super(event.getView(), event.getSlotType(), event.getSlot(), event.getClick(), event.getAction());

        Preconditions.checkNotNull(context, "context cannot be null");

        this.context = context;
    }

    /**
     * Sets the current item in the clicked slot to the specified new item.
     *
     * @param newItem The new ItemStack to set in the clicked slot, or null to clear it.
     */
    public void changeItem(@Nullable ItemStack newItem) {
        this.setCurrentItem(newItem);
    }

    /**
     * Modifies the current item in the clicked slot using the provided modifier.
     *
     * @param modifier A Consumer that modifies an ItemBuilder for the current item.
     */
    public void changeItem(@NotNull Consumer<ItemBuilder> modifier) {
        Preconditions.checkNotNull(modifier, "modifier cannot be null");

        ItemStack item = this.getCurrentItem();
        if (item == null)
            return;

        ItemBuilder builder = ItemBuilder.of(item);
        modifier.accept(builder);
        this.setCurrentItem(builder.build());
    }

    /**
     * Returns the player who clicked in the inventory.
     *
     * @return The player who clicked.
     */
    @NotNull
    public Player player() {
        return (Player) getWhoClicked();
    }

    /**
     * Returns the MenuContext associated with this click event.
     *
     * @return The MenuContext.
     */
    @NotNull
    public MenuContext context() {
        return context;
    }
}

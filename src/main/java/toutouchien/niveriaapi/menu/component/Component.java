package toutouchien.niveriaapi.menu.component;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

public abstract class Component {
    private boolean visible = true;
    private boolean enabled = true;

    private int x = 0;
    private int y = 0;

    public void render(@NotNull MenuContext context) {
        Preconditions.checkNotNull(context, "context cannot be null");

        Int2ObjectMap<ItemStack> items = this.items(context);
        IntSet slots = this.slots();

        for (int slot : slots) {
            ItemStack item = items.get(slot);
            context.menu().getInventory().setItem(slot, item);
        }
    }

    public void onAdd(@NotNull MenuContext context) {

    }

    public void onRemove(@NotNull MenuContext context) {

    }

    public abstract void onClick(@NotNull NiveriaInventoryClickEvent event, @NotNull MenuContext context);

    @NotNull
    public abstract Int2ObjectMap<ItemStack> items(@NotNull MenuContext context);

    @NotNull
    public abstract IntSet slots();

    public void position(@NonNegative int x, @NonNegative int y) {
        Preconditions.checkArgument(x >= 0, "x cannot be negative: %d", x);
        Preconditions.checkArgument(y >= 0, "y cannot be negative: %d", y);

        this.x = x;
        this.y = y;
    }

    public void visible(boolean visible) {
        this.visible = visible;
    }

    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    @NonNegative
    public int x() {
        return x;
    }

    @NonNegative
    public int y() {
        return y;
    }

    @Positive
    public abstract int width();

    @Positive
    public abstract int height();

    @NonNegative
    public int slot() {
        return y * 9 + x;
    }

    public boolean visible() {
        return visible;
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean interactable() {
        return this.visible && this.enabled;
    }

    @NonNegative
    public static int toX(int slot) {
        return slot % 9;
    }

    @NonNegative
    public static int toY(int slot) {
        return slot / 9;
    }

    @NonNegative
    public static int toSlot(int x, int y) {
        return y * 9 + x;
    }
}

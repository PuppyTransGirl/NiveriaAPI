package toutouchien.niveriaapi.menu.component;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

public abstract class Component {
    private Component parent;

    private boolean visible = true;
    private boolean enabled = true;

    private int x = 0;
    private int y = 0;
    private int width = 1;
    private int height = 1;

    private int updateInterval = -1;

    public void render(@NotNull MenuContext context) {
        Int2ObjectMap<ItemStack> items = this.items(context);
        IntSet slots = this.slots();

        for (int slot : slots) {
            ItemStack item = items.get(slot);
            context.menu().getInventory().setItem(slot, item);
        }
    }

    public abstract void onAdd(@NotNull MenuContext context);

    public abstract void onRemove(@NotNull MenuContext context);

    public abstract void onClick(@NotNull NiveriaInventoryClickEvent event, @NotNull MenuContext context);

    public abstract Int2ObjectMap<ItemStack> items(@NotNull MenuContext context);

    public abstract IntSet slots();

    public void position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

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

    public static int toX(int slot) {
        return slot % 9;
    }

    public static int toY(int slot) {
        return slot / 9;
    }

    public static int toSlot(int x, int y) {
        return y * 9 + x;
    }
}

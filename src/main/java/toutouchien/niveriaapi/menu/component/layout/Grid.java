package toutouchien.niveriaapi.menu.component.layout;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

public class Grid extends Component {
    private final int width, height;

    private final ObjectList<Component> slotComponents;

    private final ItemStack border;
    private final ItemStack fill;

    public Grid(
            int width, int height,
            ObjectList<Component> slotComponents,
            ItemStack border, ItemStack fill
    ) {
        this.width = width;
        this.height = height;
        this.slotComponents = slotComponents;
        this.border = border;
        this.fill = fill;
    }

    @Override
    public void onAdd(@NotNull MenuContext context) {
        if (!this.visible())
            return;

        this.slotComponents.forEach(component -> component.onAdd(context));
    }

    @Override
    public void onRemove(@NotNull MenuContext context) {
        if (!this.visible())
            return;

        this.slotComponents.forEach(component -> component.onRemove(context));
    }

    @Override
    public void onClick(@NotNull NiveriaInventoryClickEvent event, @NotNull MenuContext context) {
        if (!this.interactable())
            return;

        for (Component component : this.slotComponents) {
            if (component.slots().contains(event.getSlot())) {
                component.onClick(event, context);
                break;
            }
        }
    }

    // slotComponents -> border -> fill
    @Override
    public Int2ObjectMap<ItemStack> items(@NotNull MenuContext context) {
        Int2ObjectMap<ItemStack> items = new Int2ObjectOpenHashMap<>();

        for (Component slotComponent : this.slotComponents)
            items.putAll(slotComponent.items(context));

        if (this.border == null && this.fill == null)
            return items;

        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int slot = toSlot(x + this.x(), y + this.y());
                if (items.containsKey(slot))
                    continue;

                if (this.border != null && this.border(x + this.x(), y + this.y()))
                    items.put(slot, this.border);
                else if (this.fill != null)
                    items.put(slot, this.fill);
            }
        }

        return items;
    }

    // slotComponents -> border -> fill
    @Override
    public IntSet slots() {
        IntSet slots = new IntOpenHashSet();

        for (Component slotComponent : this.slotComponents)
            slots.addAll(slotComponent.slots());

        if (this.border == null && this.fill == null)
            return slots;

        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int slot = toSlot(x + this.x(), y + this.y());
                if (slots.contains(slot))
                    continue;

                if ((this.border != null && border(x + this.x(), y + this.y())) || this.fill != null)
                    slots.add(slot);
            }
        }

        return slots;
    }

    private boolean border(int x, int y) {
        return x == this.x()
                || x == this.x() + this.width - 1
                || y == this.y()
                || y == this.y() + this.height - 1;
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private int width, height;

        private final ObjectList<Component> slotComponents = new ObjectArrayList<>();

        private ItemStack border;
        private ItemStack fill;

        public Builder add(int slot, Component component) {
            slotComponents.add(component);
            component.position(toX(slot), toY(slot));
            return this;
        }

        public Builder add(int x, int y, Component component) {
            return add(y * 9 + x, component);
        }

        public Builder border(ItemStack borderItem) {
            border = borderItem;
            return this;
        }

        public Builder fill(ItemStack fillItem) {
            this.fill = fillItem;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Grid build() {
            return new Grid(
                    this.width, this.height,
                    this.slotComponents,
                    this.border, this.fill
            );
        }
    }
}

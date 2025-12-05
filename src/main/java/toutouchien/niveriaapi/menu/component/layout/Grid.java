package toutouchien.niveriaapi.menu.component.layout;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Contract;
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
    @NotNull
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
    @NotNull
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

    @Positive
    @Override
    public int width() {
        return this.width;
    }

    @Positive
    @Override
    public int height() {
        return this.height;
    }

    private boolean border(int x, int y) {
        return x == this.x()
                || x == this.x() + this.width - 1
                || y == this.y()
                || y == this.y() + this.height - 1;
    }

    @NotNull
    @Contract(value = "-> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private int width, height;

        private final ObjectList<Component> slotComponents = new ObjectArrayList<>();

        private ItemStack border;
        private ItemStack fill;

        @NotNull
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder add(@NonNegative int slot, @NotNull Component component) {
            Preconditions.checkArgument(slot >= 0, "slot cannot be negative: %d", slot);
            Preconditions.checkNotNull(component, "component cannot be null");

            slotComponents.add(component);
            component.position(toX(slot), toY(slot));
            return this;
        }

        @NotNull
        @Contract(value = "_, _, _ -> this", mutates = "this")
        public Builder add(@NonNegative int x, @NonNegative int y, @NotNull Component component) {
            Preconditions.checkArgument(x >= 0, "x cannot be negative: %d", x);
            Preconditions.checkArgument(y >= 0, "y cannot be negative: %d", y);
            Preconditions.checkNotNull(component, "component cannot be null");

            return add(y * 9 + x, component);
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder border(@NotNull ItemStack border) {
            Preconditions.checkNotNull(border, "border cannot be null");

            this.border = border;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder fill(@NotNull ItemStack fill) {
            Preconditions.checkNotNull(fill, "fill cannot be null");

            this.fill = fill;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder width(@Positive int width) {
            Preconditions.checkArgument(width >= 1, "width cannot be less than 1: %d", width);

            this.width = width;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder height(@Positive int height) {
            Preconditions.checkArgument(height >= 1, "height cannot be less than 1: %d", height);

            this.height = height;
            return this;
        }

        @NotNull
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder size(@Positive int width, @Positive int height) {
            Preconditions.checkArgument(width >= 1, "width cannot be less than 1: %d", width);
            Preconditions.checkArgument(height >= 1, "height cannot be less than 1: %d", height);

            this.width = width;
            this.height = height;
            return this;
        }

        @NotNull
        public Grid build() {
            // Check if all components fit inside the grid
            for (Component component : this.slotComponents) {
                int compX = component.x();
                int compY = component.y();
                int compWidth = component.width();
                int compHeight = component.height();

                Preconditions.checkArgument(
                        compX >= 0 && compY >= 0 &&
                                compX + compWidth <= this.width &&
                                compY + compHeight <= this.height,
                        "Component %s does not fit inside the grid of size %dx%d at position (%d, %d) with size %dx%d",
                        component.getClass().getSimpleName(),
                        this.width, this.height,
                        compX, compY,
                        compWidth, compHeight
                );
            }

            return new Grid(
                    this.width, this.height,
                    this.slotComponents,
                    this.border, this.fill
            );
        }
    }
}

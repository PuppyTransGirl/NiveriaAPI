package toutouchien.niveriaapi.menu.component.layout;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;

import java.util.*;

public class Grid extends Component {
    private final List<Component> components = new ArrayList<>();
    private final ItemStack border;
    private final ItemStack fill;

    private Grid(Builder builder) {
		size(builder.width, builder.height);
        this.components.addAll(builder.slotComponents);
        this.border = builder.border;
        this.fill = builder.fill;

        // Add components as children
        this.components.forEach(this::addChild);
    }

    @Override
    public void render(@NotNull MenuContext context) {
        if (!visible())
            return;

        // Render specific slot components
        components.forEach(component -> component.render(context));
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull MenuContext context) {
        if (!enabled())
            return;

        // Check specific slot components first
		Optional<Component> slotComponent = components.stream()
				.filter(c -> event.getSlot() == toSlot(c.x(), c.y()))
				.findFirst();

		if (slotComponent.isEmpty())
			return;

		slotComponent.get().onClick(event, context);
	}

    @Override
	public List<Integer> slots() {
		Set<Integer> slots = new HashSet<>();

		// Add specific slot components
		components.forEach(component -> slots.addAll(component.slots()));

		// Add border and fill slots
		if (border != null || fill != null) {
			for (int y = 0; y < height(); y++) {
				for (int x = 0; x < width(); x++) {
					int slot = toSlot(x + x(), y + y());
					if (slots.contains(slot))
						continue;

					if (border != null && isBorder(x + x(), y + y())) {
						slots.add(slot);
						continue;
					}

					if (fill != null)
						slots.add(slot);
				}
			}
		}

		return new ArrayList<>(slots);
    }

    @Override
    public Map<Integer, ItemStack> items(@NotNull MenuContext context) {
        Map<Integer, ItemStack> items = new HashMap<>();

        // Add specific components items
        components.forEach(component -> items.putAll(component.items(context)));

		// Add border and fill items
		if (border == null && fill == null)
			return items;

		for (int y = 0; y < height(); y++) {
			for (int x = 0; x < width(); x++) {
				int slot = toSlot(x + x(), y + y());
				if (items.containsKey(slot))
					continue;

				if (border != null && isBorder(x + x(), y + y())) {
					items.put(slot, border);
					continue;
				}

				if (fill == null)
					continue;

				items.put(slot, fill);
			}
		}

		return items;
    }

	private boolean isBorder(int x, int y) {
		return y == 0 || y == height() - 1 || x == 0 || x == width() - 1;
	}

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
		private int width, height;
        private final List<Component> slotComponents = new ArrayList<>();
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
            return new Grid(this);
        }
    }
}
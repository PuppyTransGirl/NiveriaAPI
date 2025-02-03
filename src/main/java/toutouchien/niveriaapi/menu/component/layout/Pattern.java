package toutouchien.niveriaapi.menu.component.layout;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;

import java.util.*;

public class Pattern extends Component {
	private final Map<Integer, ItemStack> slotItems = new HashMap<>();
	private final List<Component> components = new ArrayList<>();

	public Pattern(Builder builder) {
		this.components.addAll(builder.components);

		char[][] pattern = builder.pattern;
		size(pattern[0].length, pattern.length);
		initializeSlotComponents(pattern, builder.symbols);

		this.components.forEach(this::addChild);
		size(builder.width, builder.height);
	}

	private void initializeSlotComponents(char[][] pattern, Map<Character, ItemStack> symbols) {
		for (int y = 0; y < pattern.length; y++) {
			for (int x = 0; x < pattern[y].length; x++) {
				char symbol = pattern[y][x];
				ItemStack item = symbols.get(symbol);
				if (item == null)
					continue;

				int slot = toSlot(x + x(), y + y());
				slotItems.put(slot, item);
			}
		}
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

		// Add symbol slot items
		slots.addAll(slotItems.keySet());

		return new ArrayList<>(slots);
	}

	@Override
	public Map<Integer, ItemStack> items(@NotNull MenuContext context) {
		// Add symbol items
		Map<Integer, ItemStack> items = new HashMap<>(slotItems);

		// Add specific components items
		components.forEach(component -> items.putAll(component.items(context)));

		return items;
	}

	public static Builder create() {
		return new Builder();
	}

	public static class Builder {
		private int width, height;
		private char[][] pattern;
		private Map<Character, ItemStack> symbols = new HashMap<>();
		private List<Component> components = new ArrayList<>();

		public Builder pattern(String... pattern) {
			char[][] newPattern = new char[pattern.length][];

			for (int i = 0; i < pattern.length; i++)
				newPattern[i] = pattern[i].toCharArray();

			this.pattern = newPattern;
			return this;
		}

		public Builder pattern(List<String> pattern) {
			char[][] newPattern = new char[pattern.size()][];

			for (int i = 0; i < pattern.size(); i++)
				newPattern[i] = pattern.get(i).toCharArray();

			this.pattern = newPattern;
			return this;
		}

		public Builder symbol(char symbolChar, ItemStack symbolItem) {
			this.symbols.put(symbolChar, symbolItem);
			return this;
		}

		public Builder add(int slot, Component component) {
			components.add(component);
			component.position(toX(slot), toY(slot));
			return this;
		}

		public Builder add(int x, int y, Component component) {
			return add(y * 9 + x, component);
		}

		public Builder size(int width, int height) {
			this.width = width;
			this.height = height;
			return this;
		}

		public Pattern build() {
			return new Pattern(this);
		}
	}
}

package toutouchien.niveriaapi.menu.component.container;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.Menu;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.menu.component.interactive.Button;
import toutouchien.niveriaapi.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Paginator extends Component {
    private final List<Component> components;
    private final int itemsPerPage;
	private Button previousButton;
	private Button nextButton;
    private int currentPage = 0;

    public Paginator(Builder builder) {
		size(builder.width, builder.height);
        this.components = builder.components;
        this.itemsPerPage = builder.itemsPerPage;
		initializeNavigationComponents(builder.previousItem, builder.nextItem);

		this.components.forEach(this::addChild);
    }

	private void initializeNavigationComponents(ItemStack previousItem, ItemStack nextItem) {
		Button previousButton = Button.create()
				.item(previousItem)
				.onClick(event -> {
					currentPage--;
					((Menu) event.getInventory().getHolder(false)).update();
				})
				.build();

		previousButton.position(x(), height() - 1);
		this.addChild(previousButton);
		this.previousButton = previousButton;

		Button nextButton = Button.create()
				.item(nextItem)
				.onClick(event -> {
					currentPage++;
					((Menu) event.getInventory().getHolder(false)).update();
				})
				.build();

		nextButton.position(width() - 1, height() - 1);
		this.addChild(nextButton);
		this.nextButton = nextButton;
	}

    @Override
    public void render(@NotNull MenuContext context) {
		if (!visible())
			return;

        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, components.size());

        for (int i = startIndex; i < endIndex; i++) {
            Component component = components.get(i);
            component.position(x() + (i - startIndex) % 9, y() + (i - startIndex) / 9);
            component.render(context);
		}

		if (currentPage > 0)
			previousButton.render(context);

		if (endIndex < components.size())
			nextButton.render(context);
    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull MenuContext context) {
		if (!enabled())
			return;

		int slot = event.getSlot();
		if (slot == toSlot(previousButton.x(), previousButton.y())) {
			previousButton.onClick(event, context);
			return;
		}

		if (slot == toSlot(nextButton.x(), nextButton.y())) {
			nextButton.onClick(event, context);
			return;
		}

		// TODO: use visible()
		int startIndex = currentPage * itemsPerPage;
		int endIndex = Math.min(startIndex + itemsPerPage, components.size());

		Component slotComponent = null;
		for (int i = startIndex; i < endIndex; i++) {
			Component c = components.get(i);
			if (slot != toSlot(c.x(), c.y()))
				continue;

			slotComponent = c;
			break;
		}

		if (slotComponent == null)
			return;

		slotComponent.onClick(event, context);
    }

    @Override
    public List<Integer> slots() {
        List<Integer> slots = new ArrayList<>();

		int startIndex = currentPage * itemsPerPage;
		int endIndex = Math.min(startIndex + itemsPerPage, components.size());

		for (int i = startIndex; i < endIndex; i++) {
			Component component = components.get(i);
			slots.add(toSlot(component.x(), component.y()));
		}

		if (currentPage > 0)
			slots.add(toSlot(previousButton.x(), previousButton.y()));

		if (endIndex < components.size())
			slots.add(toSlot(nextButton.x(), nextButton.y()));

        return slots;
    }

    @Override
    public Map<Integer, ItemStack> items(@NotNull MenuContext context) {
		Map<Integer, ItemStack> items = new HashMap<>();

		int startIndex = currentPage * itemsPerPage;
		int endIndex = Math.min(startIndex + itemsPerPage, components.size());

		for (int i = startIndex; i < endIndex; i++) {
			Component component = components.get(i);
			items.putAll(component.items(context));
		}

		if (currentPage > 0)
			items.putAll(previousButton.items(context));

		if (endIndex < components.size())
			items.putAll(nextButton.items(context));

		return items;
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
		private int width, height;
        private List<Component> components = new ArrayList<>();
		private int itemsPerPage = 9;
		private ItemStack previousItem = ItemBuilder.of(Material.ARROW).name(net.kyori.adventure.text.Component.text("Previous Page")).itemFlags(ItemFlag.HIDE_ENCHANTS).build();
		private ItemStack nextItem = ItemBuilder.of(Material.ARROW).name(net.kyori.adventure.text.Component.text("Next Page")).itemFlags(ItemFlag.HIDE_ENCHANTS).build();

        public Builder add(Component component) {
            components.add(component);
            return this;
        }

		public Builder set(List<Component> components) {
			this.components = components;
			return this;
		}

        public Builder itemsPerPage(int itemsPerPage) {
            this.itemsPerPage = itemsPerPage;
            return this;
        }

		public Builder previousItem(ItemStack previousItem) {
			this.previousItem = previousItem;
			return this;
		}

		public Builder nextItem(ItemStack nextItem) {
			this.nextItem = nextItem;
			return this;
		}

		public Builder size(int width, int height) {
			this.width = width;
			this.height = height;
			return this;
		}

        public Paginator build() {
            return new Paginator(this);
        }
    }
}
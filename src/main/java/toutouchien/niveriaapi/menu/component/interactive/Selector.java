package toutouchien.niveriaapi.menu.component.interactive;

import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A component that allows cycling through different options when clicked.
 * Each option can have its own display item, name, and associated value.
 */
public class Selector<T> extends Component {
    private final List<Option<T>> options;
    private int currentIndex;
    private final Consumer<SelectionChangeEvent<T>> onSelectionChange;
	private final Function<MenuContext, T> defaultOptionFunction;
	private final Sound sound;

    private Selector(Builder<T> builder) {
        this.options = new ArrayList<>(builder.options);
        this.currentIndex = builder.defaultIndex;
        this.onSelectionChange = builder.onSelectionChange;
		this.defaultOptionFunction = builder.defaultOptionFunction;
		this.sound = builder.sound;

		size(1, 1);
    }

	@Override
	public void mount(@NotNull MenuContext context) {
		super.mount(context);
		if (defaultOptionFunction == null)
			return;

		T defaultOption = defaultOptionFunction.apply(context);
		selection(defaultOption);
	}

	@Override
    public void render(@NotNull MenuContext context) {

    }

    @Override
    public void onClick(@NotNull InventoryClickEvent event, @NotNull MenuContext context) {
        if (!enabled() || options.isEmpty())
			return;

		if (sound != null)
			context.viewer().playSound(sound, Sound.Emitter.self());

        Option<T> oldSelection = currentOption();
        currentIndex = (currentIndex + 1) % options.size();
        Option<T> newSelection = currentOption();

        if (onSelectionChange != null) {
            SelectionChangeEvent<T> changeEvent = new SelectionChangeEvent<>(
                oldSelection.value,
                newSelection.value,
                currentIndex,
                context
            );
            onSelectionChange.accept(changeEvent);
        }

        context.menu().update();
    }

    @Override
    public List<Integer> slots() {
		if (!visible())
			return Collections.emptyList();

        return Collections.singletonList(toSlot(x(), y()));
    }

    @Override
    public Map<Integer, ItemStack> items(@NotNull MenuContext context) {
        Map<Integer, ItemStack> items = new HashMap<>();
        if (!visible() || options.isEmpty())
			return items;

        Option<T> current = currentOption();
        ItemStack item = current.displayItem();
        items.put(toSlot(x(), y()), item);
        
        return items;
    }

    public Option<T> currentOption() {
        return options.get(currentIndex);
    }

    public T currentValue() {
        return currentOption().value;
    }

    public void selection(int index) {
		if (index < 0 || index >= options.size())
			return;

		currentIndex = index;
	}

	public void selection(T value) {
		for (int i = 0; i < options.size(); i++) {
			if (!Objects.equals(options.get(i).value, value))
				continue;

			currentIndex = i;
			break;
		}
    }

    public static class Option<T> {
        private final ItemStack displayItem;
        private final T value;

        public Option(ItemStack displayItem, T value) {
            this.displayItem = displayItem;
            this.value = value;
        }

		public ItemStack displayItem() {
			return displayItem;
		}
	}

	public record SelectionChangeEvent<T>(T oldValue, T newValue, int newIndex, MenuContext context) {
	}

    public static <T> Builder<T> create() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private final List<Option<T>> options = new ArrayList<>();
        private int defaultIndex = 0;
        private Consumer<SelectionChangeEvent<T>> onSelectionChange;
		private Function<MenuContext, T> defaultOptionFunction;
		private Sound sound = Sound.sound(org.bukkit.Sound.UI_BUTTON_CLICK, Sound.Source.MASTER, 1F, 1F);

        public Builder<T> addOption(ItemStack displayItem, T value) {
            options.add(new Option<>(displayItem, value));
            return this;
        }

        public Builder<T> addOption(Material material, T value) {
            return addOption(
                new ItemStack(material),
                value
            );
        }

        public Builder<T> defaultIndex(int index) {
            this.defaultIndex = index;
            return this;
        }

        public Builder<T> onSelectionChange(Consumer<SelectionChangeEvent<T>> handler) {
            this.onSelectionChange = handler;
            return this;
        }

		public Builder<T> defaultOption(Function<MenuContext, T> defaultOptionFunction) {
			this.defaultOptionFunction = defaultOptionFunction;
			return this;
		}

		public Builder<T> sound(Sound sound) {
			this.sound = sound;
			return this;
		}

        public Selector<T> build() {
            if (options.isEmpty())
                throw new IllegalStateException("Selector must have at least one option");

            if (defaultIndex < 0 || defaultIndex >= options.size())
                defaultIndex = 0;

            return new Selector<>(this);
        }
    }
}
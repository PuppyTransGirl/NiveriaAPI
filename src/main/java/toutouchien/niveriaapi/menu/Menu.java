package toutouchien.niveriaapi.menu;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

public abstract class Menu implements InventoryHolder {
    private Inventory inventory;
    private final MenuContext context;
    private final Player player;

    private Component root;

    protected Menu(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        this.player = player;
        this.context = new MenuContext(this);
    }

    public void open() {
        net.kyori.adventure.text.Component title = this.title();
        this.root = this.root(this.context);
        this.inventory = Bukkit.createInventory(this, this.root.height() * 9, title);

        this.root.onAdd(this.context);
        this.root.render(this.context);

        this.player.openInventory(this.inventory);
    }

    public void close(boolean event) {
        this.root.onRemove(this.context);

        if (!event)
            this.player.closeInventory();

        this.context.close();
    }

    public void handleClick(@NotNull NiveriaInventoryClickEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null");

        this.root.onClick(event, this.context);
        this.root.render(this.context);
    }

    @NotNull
    protected abstract net.kyori.adventure.text.Component title();

    @NotNull
    protected abstract Component root(@NotNull MenuContext context);

    @NotNull
    public Player player() {
        return player;
    }

    @NotNull
    public MenuContext context() {
        return context;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

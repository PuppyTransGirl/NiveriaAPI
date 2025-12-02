package toutouchien.niveriaapi.menu;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

import java.util.UUID;

public abstract class Menu implements InventoryHolder {
    private static final Object2ObjectMap<UUID, Menu> openMenus = new Object2ObjectOpenHashMap<>();

    private final Inventory inventory;
    private final MenuContext context;
    private final Player player;

    private final Component root;

    protected Menu(Player player) {
        this.player = player;
        this.context = new MenuContext(this);

        net.kyori.adventure.text.Component title = this.title();
        int rows = this.rows();

        this.inventory = Bukkit.createInventory(this, rows * 9, title);

        this.root = this.root();
    }

    public void open() {
        if (this.root != null) {
            this.root.onAdd(this.context);
            this.root.render(this.context);
        }

        this.player.openInventory(this.inventory);
    }

    public void close(boolean event) {
        if (this.root != null)
            this.root.onRemove(this.context);

        if (!event)
            this.player.closeInventory();

        openMenus.remove(this.player.getUniqueId());
        this.context.close();
    }

    public void handleClick(NiveriaInventoryClickEvent event) {
        if (this.root == null)
            return;

        this.root.onClick(event, this.context);
        this.root.render(this.context);
    }

    protected abstract net.kyori.adventure.text.Component title();

    protected abstract int rows();

    protected abstract Component root();

    public Player player() {
        return player;
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }
}

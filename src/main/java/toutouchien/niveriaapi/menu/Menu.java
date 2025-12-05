package toutouchien.niveriaapi.menu;

import com.google.common.base.Preconditions;
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

    protected Menu(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        this.player = player;
        this.context = new MenuContext(this);

        net.kyori.adventure.text.Component title = this.title();
        this.root = this.root();
        this.inventory = Bukkit.createInventory(this, this.root.height() * 9, title);
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

    public void handleClick(@NotNull NiveriaInventoryClickEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null");

        if (this.root == null)
            return;

        this.root.onClick(event, this.context);
        this.root.render(this.context);
    }

    @NotNull
    protected abstract net.kyori.adventure.text.Component title();

    @NotNull
    protected abstract Component root();

    @NotNull
    public Player player() {
        return player;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

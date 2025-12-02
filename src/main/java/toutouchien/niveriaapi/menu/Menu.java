package toutouchien.niveriaapi.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.component.Component;

public class Menu implements InventoryHolder {
    private final Inventory inventory;
    private final Player player;

    private final Component root;

    public Menu(Player player) {
        this.player = player;

        net.kyori.adventure.text.Component title = this.title();
        int rows = this.rows();

        this.inventory = Bukkit.createInventory(this, rows * 9, title);

        this.root = this.root();
    }

    protected net.kyori.adventure.text.Component title() {
        return net.kyori.adventure.text.Component.text("Menu");
    }

    protected int rows() {
        return 3;
    }

    protected Component root() {
        return null;
    }

    public Player player() {
        return player;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

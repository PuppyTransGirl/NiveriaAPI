package toutouchien.niveriaapi.menu.component;

import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

public abstract class Component {
    private Component parent;

    private boolean visible = true;
    private boolean enabled = true;

    private int x = 0;
    private int y = 0;
    private int width = 1;
    private int height = 1;

    private int updateInterval = -1;

    protected abstract void onAdd(@NotNull MenuContext context);

    protected abstract void onRemove(@NotNull MenuContext context);

    protected abstract void onClick(@NotNull NiveriaInventoryClickEvent event, @NotNull MenuContext context);

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int slot() {
        return y * 9 + x;
    }

    public boolean visible() {
        return visible;
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean interactable() {
        return this.visible && this.enabled;
    }
}

package toutouchien.niveriaapi.menu;

import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.infos.MenuInfos;
import toutouchien.niveriaapi.menu.items.MenuItem;

public abstract class PaginatedMenu extends Menu {
    protected int page;

    protected PaginatedMenu(@NotNull MenuInfos menuInfos) {
        super(menuInfos);

        this.page = 0;
    }

    protected void incrementPage(int i) {
        if (i < 0)
            throw new IllegalArgumentException("Increment value must be non-negative");

        this.page += i;

        this.inventory.clear();

        for (MenuItem menuItem : this.itemsCache = this.items())
            this.inventory.setItem(menuItem.slot(), menuItem.itemStack());
    }

    protected void decreasePage(int i) {
        if (this.page <= 0)
            throw new IllegalArgumentException("Cannot decrease page below 0");

        this.page = Math.max(0, this.page - i);

        this.inventory.clear();

        for (MenuItem menuItem : this.itemsCache = this.items())
            this.inventory.setItem(menuItem.slot(), menuItem.itemStack());
    }

    public int page() {
        return page;
    }

    public boolean firstPage() {
        return this.page == 0;
    }

    public boolean lastPage(int maxPage) {
        if (maxPage < 0)
            throw new IllegalArgumentException("maxPage must be non-negative");

        return this.page == maxPage;
    }
}

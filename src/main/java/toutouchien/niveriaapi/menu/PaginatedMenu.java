package toutouchien.niveriaapi.menu;

import com.google.common.base.Preconditions;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.infos.MenuInfos;
import toutouchien.niveriaapi.menu.items.MenuItem;

public abstract class PaginatedMenu extends Menu {
    protected int page;

    protected PaginatedMenu(@NotNull MenuInfos menuInfos) {
        super(menuInfos);

        this.page = 0;
    }

    protected void incrementPage(@Positive int i) {
        Preconditions.checkArgument(i >= 1, "i cannot be less than 1: %d", i);

        this.page += i;

        this.inventory.clear();

        for (MenuItem menuItem : this.itemsCache = this.items())
            this.inventory.setItem(menuItem.slot(), menuItem.itemStack());
    }

    protected void decreasePage(@Positive int i) {
        Preconditions.checkArgument(i >= 1, "i cannot be less than 1: %d", i);

        this.page = Math.max(0, this.page - i);

        this.inventory.clear();

        for (MenuItem menuItem : this.itemsCache = this.items())
            this.inventory.setItem(menuItem.slot(), menuItem.itemStack());
    }

    @NonNegative
    public int page() {
        return page;
    }

    public boolean firstPage() {
        return this.page == 0;
    }

    public boolean lastPage(@Positive int maxPage) {
        Preconditions.checkArgument(maxPage >= 1, "i cannot be less than 1: %d", maxPage);

        return this.page == maxPage;
    }
}

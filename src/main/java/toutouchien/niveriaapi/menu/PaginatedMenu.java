package toutouchien.niveriaapi.menu;

import com.google.common.base.Preconditions;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.infos.MenuInfos;
import toutouchien.niveriaapi.menu.items.MenuItem;

/**
 * Abstract class representing a paginated menu in the inventory system.
 */
public abstract class PaginatedMenu extends Menu {
    protected int page;

    protected PaginatedMenu(@NotNull MenuInfos menuInfos) {
        super(menuInfos);

        this.page = 0;
    }

    /**
     * Increments the current page by the specified amount.
     *
     * @param i The number of pages to increment by (must be at least 1).
     */
    protected void incrementPage(@Positive int i) {
        Preconditions.checkArgument(i >= 1, "i cannot be less than 1: %d", i);

        this.page += i;

        this.inventory.clear();

        for (MenuItem menuItem : this.itemsCache = this.items())
            this.inventory.setItem(menuItem.slot(), menuItem.itemStack());
    }

    /**
     * Decreases the current page by the specified amount.
     *
     * @param i The number of pages to decrease by (must be at least 1).
     */
    protected void decreasePage(@Positive int i) {
        Preconditions.checkArgument(i >= 1, "i cannot be less than 1: %d", i);

        this.page = Math.max(0, this.page - i);

        this.inventory.clear();

        for (MenuItem menuItem : this.itemsCache = this.items())
            this.inventory.setItem(menuItem.slot(), menuItem.itemStack());
    }

    /**
     * Returns the current page number.
     *
     * @return The current page number (non-negative).
     */
    @NonNegative
    public int page() {
        return page;
    }

    /**
     * Returns whether the current page is the first page.
     *
     * @return true if the current page is the first page, false otherwise.
     */
    public boolean firstPage() {
        return this.page == 0;
    }

    /**
     * Returns whether the current page is the last page.
     *
     * @param maxPage The maximum page number (must be at least 1).
     * @return true if the current page is the last page, false otherwise.
     */
    public boolean lastPage(@Positive int maxPage) {
        Preconditions.checkArgument(maxPage >= 1, "maxPage cannot be less than 1: %d", maxPage);

        return this.page == maxPage;
    }
}

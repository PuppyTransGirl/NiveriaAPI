package toutouchien.niveriaapi.menu;

import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.infos.MenuInfos;

public abstract class PaginatedMenu extends Menu {
	protected int page;

	public PaginatedMenu(@NotNull MenuInfos menuInfos) {
		super(menuInfos);

		page = 0;
	}

	protected void incrementPage(int i) {
		this.page += i;

		inventory.clear();
		(this.itemsCache = items()).forEach(menuItem -> inventory.setItem(menuItem.slot(), menuItem.itemStack()));
	}

	protected void decreasePage(int i) {
		this.page = Math.max(0, this.page - i);

		inventory.clear();
		(this.itemsCache = items()).forEach(menuItem -> inventory.setItem(menuItem.slot(), menuItem.itemStack()));
	}

	public int page() {
		return page;
	}
}

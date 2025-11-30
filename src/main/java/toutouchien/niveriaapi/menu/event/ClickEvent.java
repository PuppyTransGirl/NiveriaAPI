package toutouchien.niveriaapi.menu.event;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a click event in a Niveria inventory.
 */
public interface ClickEvent {

    /**
     * Called when a click event occurs in the Niveria inventory.
     *
     * @param event The Niveria inventory click event.
     */
    void onClick(@NotNull NiveriaInventoryClickEvent event);
}

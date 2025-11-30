package toutouchien.niveriaapi.menu.infos;

import com.google.common.base.Preconditions;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Class representing information about a menu and the player interacting with it.
 */
public class MenuInfos {
    private final Player player;
    private OfflinePlayer playerForInfos;

    /**
     * Constructs a MenuInfos instance for the specified player.
     *
     * @param player The player interacting with the menu.
     */
    public MenuInfos(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        this.player = player;
    }

    /**
     * Constructs a MenuInfos instance for the specified player and an optional player for additional information.
     *
     * @param player         The player interacting with the menu.
     * @param playerForInfos An optional offline player where the information are gathered from.
     */
    public MenuInfos(@NotNull Player player, @Nullable OfflinePlayer playerForInfos) {
        Preconditions.checkNotNull(player, "player cannot be null");

        this.player = player;
        this.playerForInfos = playerForInfos;
    }

    /**
     * Sets the offline player from whom additional information is gathered.
     *
     * @param playerForInfos The offline player for additional information.
     */
    public void playerForInfos(@Nullable OfflinePlayer playerForInfos) {
        this.playerForInfos = playerForInfos;
    }

    /**
     * Returns the player interacting with the menu.
     *
     * @return The player.
     */
    @NotNull
    public Player player() {
        return player;
    }

    /**
     * Returns the UUID of the player interacting with the menu.
     *
     * @return The player's UUID.
     */
    @NotNull
    public UUID uuid() {
        return player.getUniqueId();
    }

    /**
     * Returns the offline player from whom additional information is gathered.
     *
     * @return The offline player, or null if not set.
     */
    @Nullable
    public OfflinePlayer playerForInfos() {
        return playerForInfos;
    }
}

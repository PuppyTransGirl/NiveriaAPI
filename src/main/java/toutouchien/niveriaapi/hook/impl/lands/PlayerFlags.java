package toutouchien.niveriaapi.hook.impl.lands;

import me.angeschossen.lands.api.flags.type.Flags;
import me.angeschossen.lands.api.flags.type.PlayerFlag;
import toutouchien.niveriaapi.utils.StringUtils;

import java.util.Optional;

/**
 * Enum wrapper for Lands API PlayerFlags
 */
public enum PlayerFlags {
    ENTER_MESSAGES(Flags.ENTER_MESSAGES),
    RECEIVE_INVITES(Flags.RECEIVE_INVITES),
    SHOW_INBOX(Flags.SHOW_INBOX);

    private final PlayerFlag flag;

    PlayerFlags(PlayerFlag flag) {
        this.flag = flag;
    }

    /**
     * Find a PlayerFlag by its name
     *
     * @param name The name of the flag (case-insensitive)
     * @return The matching enum value or null if not found
     */
    public static Optional<PlayerFlags> byName(String name) {
        return StringUtils.match(name, PlayerFlags.class);
    }

    /**
     * Find a PlayerFlag enum by its original Lands API PlayerFlag
     *
     * @param playerFlag The original PlayerFlag
     * @return The matching enum value or null if not found
     */
    public static PlayerFlags fromPlayerFlag(PlayerFlag playerFlag) {
        if (playerFlag == null)
            return null;

        for (PlayerFlags flag : values()) {
            if (!flag.flag.equals(playerFlag))
                continue;

            return flag;
        }

        return null;
    }

    /**
     * Get the original Lands API PlayerFlag
     *
     * @return the original PlayerFlag object
     */
    public PlayerFlag flag() {
        return flag;
    }
}
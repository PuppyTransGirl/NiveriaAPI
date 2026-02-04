package toutouchien.niveriaapi.hook.impl.lands;

import com.google.common.base.Preconditions;
import me.angeschossen.lands.api.flags.type.Flags;
import me.angeschossen.lands.api.flags.type.PlayerFlag;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.utils.StringUtils;

import java.util.Optional;

/**
 * Enum wrapper for Lands API PlayerFlags
 */
@NullMarked
public enum PlayerFlags {
    ENTER_MESSAGES(Flags.ENTER_MESSAGES),
    RECEIVE_INVITES(Flags.RECEIVE_INVITES),
    SHOW_INBOX(Flags.SHOW_INBOX);

    private final PlayerFlag flag;

    PlayerFlags(PlayerFlag flag) {
        Preconditions.checkNotNull(flag, "flag cannot be null");

        this.flag = flag;
    }

    /**
     * Find a PlayerFlag enum by its name
     *
     * @param name The name of the enum value
     * @return An Optional containing the matching enum value or empty if not found
     */
    public static Optional<PlayerFlags> byName(String name) {
        Preconditions.checkNotNull(name, "name cannot be null");

        return StringUtils.match(name, PlayerFlags.class);
    }

    /**
     * Find a PlayerFlag enum by its original Lands API PlayerFlag
     *
     * @param playerFlag The original PlayerFlag
     * @return The matching enum value or null if not found
     */
    @Nullable
    public static PlayerFlags fromPlayerFlag(PlayerFlag playerFlag) {
        Preconditions.checkNotNull(playerFlag, "playerFlag cannot be null");

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
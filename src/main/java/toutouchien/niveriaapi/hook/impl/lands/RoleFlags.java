package toutouchien.niveriaapi.hook.impl.lands;

import com.google.common.base.Preconditions;
import me.angeschossen.lands.api.flags.type.Flags;
import me.angeschossen.lands.api.flags.type.RoleFlag;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.utils.EnumUtils;

import java.util.Optional;

/**
 * Enum wrapper for Lands API RoleFlags
 */
@NullMarked
public enum RoleFlags {
    BLOCK_BREAK(Flags.BLOCK_BREAK),
    BLOCK_PLACE(Flags.BLOCK_PLACE),
    ATTACK_PLAYER(Flags.ATTACK_PLAYER),
    ATTACK_ANIMAL(Flags.ATTACK_ANIMAL),
    ATTACK_MONSTER(Flags.ATTACK_MONSTER),
    BLOCK_IGNITE(Flags.BLOCK_IGNITE),
    INTERACT_GENERAL(Flags.INTERACT_GENERAL),
    INTERACT_MECHANISM(Flags.INTERACT_MECHANISM),
    INTERACT_CONTAINER(Flags.INTERACT_CONTAINER),
    INTERACT_DOOR(Flags.INTERACT_DOOR),
    INTERACT_TRAPDOOR(Flags.INTERACT_TRAPDOOR),
    INTERACT_VILLAGER(Flags.INTERACT_VILLAGER),
    FLY(Flags.FLY),
    ELYTRA(Flags.ELYTRA),
    SPAWN_TELEPORT(Flags.SPAWN_TELEPORT),
    LAND_ENTER(Flags.LAND_ENTER),
    VEHICLE_USE(Flags.VEHICLE_USE),
    ITEM_PICKUP(Flags.ITEM_PICKUP),
    ENDER_PEARL(Flags.ENDER_PEARL),
    TRAMPLE_FARMLAND(Flags.TRAMPLE_FARMLAND),
    HARVEST(Flags.HARVEST),
    PLANT(Flags.PLANT),
    SHEAR(Flags.SHEAR),
    PLAYER_TRUST(Flags.PLAYER_TRUST),
    PLAYER_UNTRUST(Flags.PLAYER_UNTRUST),
    PLAYER_SETROLE(Flags.PLAYER_SETROLE),
    LAND_CLAIM(Flags.LAND_CLAIM),
    LAND_CLAIM_BORDER(Flags.LAND_CLAIM_BORDER),
    SPAWN_SET(Flags.SPAWN_SET),
    SETTING_EDIT_LAND(Flags.SETTING_EDIT_LAND),
    SETTING_EDIT_ROLE(Flags.SETTING_EDIT_ROLE),
    SETTING_EDIT_TAXES(Flags.SETTING_EDIT_TAXES),
    SETTING_EDIT_VARIOUS(Flags.SETTING_EDIT_VARIOUS),
    BALANCE_WITHDRAW(Flags.BALANCE_WITHDRAW),
    AREA_ASSIGN(Flags.AREA_ASSIGN),
    PLAYER_BAN(Flags.PLAYER_BAN),
    WAR_MANAGE(Flags.WAR_MANAGE),
    NO_DAMAGE(Flags.NO_DAMAGE),
    NATION_EDIT(Flags.NATION_EDIT);

    private final RoleFlag flag;

    RoleFlags(RoleFlag flag) {
        Preconditions.checkNotNull(flag, "flag cannot be null");

        this.flag = flag;
    }

    /**
     * Find a RoleFlag enum by its name
     *
     * @param name The name of the enum value
     * @return An Optional containing the matching enum value or empty if not found
     */
    public static Optional<RoleFlags> byName(String name) {
        Preconditions.checkNotNull(name, "name cannot be null");

        return EnumUtils.match(name, RoleFlags.class);
    }

    /**
     * Find a RoleFlag enum by its original Lands API RoleFlag
     *
     * @param roleFlag The original RoleFlag
     * @return The matching enum value or null if not found
     */
    @Nullable
    public static RoleFlags fromRoleFlag(RoleFlag roleFlag) {
        Preconditions.checkNotNull(roleFlag, "roleFlag cannot be null");

        for (RoleFlags flag : values()) {
            if (!flag.flag.equals(roleFlag))
                continue;

            return flag;
        }

        return null;
    }

    /**
     * Get the original Lands API RoleFlag
     *
     * @return the original RoleFlag object
     */
    public RoleFlag flag() {
        return flag;
    }
}
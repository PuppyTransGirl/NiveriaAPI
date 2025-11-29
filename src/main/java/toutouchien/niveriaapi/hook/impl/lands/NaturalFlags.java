package toutouchien.niveriaapi.hook.impl.lands;

import com.google.common.base.Preconditions;
import me.angeschossen.lands.api.flags.type.Flags;
import me.angeschossen.lands.api.flags.type.NaturalFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.utils.StringUtils;

import java.util.Optional;

/**
 * Enum wrapper for Lands API NaturalFlags
 */
public enum NaturalFlags {
    ENTITY_GRIEFING(Flags.ENTITY_GRIEFING),
    TNT_GRIEFING(Flags.TNT_GRIEFING),
    PISTON_GRIEFING(Flags.PISTON_GRIEFING),
    MONSTER_SPAWN(Flags.MONSTER_SPAWN),
    PHANTOM_SPAWN(Flags.PHANTOM_SPAWN),
    ANIMAL_SPAWN(Flags.ANIMAL_SPAWN),
    WATERFLOW_ALLOW(Flags.WATERFLOW_ALLOW),
    TITLE_HIDE(Flags.TITLE_HIDE),
    REQUEST_ACCEPT(Flags.REQUEST_ACCEPT),
    FIRE_SPREAD(Flags.FIRE_SPREAD),
    LEAF_DECAY(Flags.LEAF_DECAY),
    PLANT_GROWTH(Flags.PLANT_GROWTH),
    SNOW_MELT(Flags.SNOW_MELT),
    WITHER_ATTACK_ANIMAL(Flags.WITHER_ATTACK_ANIMAL),
    BLOCK_SPREADING(Flags.BLOCK_SPREADING);

    private final NaturalFlag flag;

    NaturalFlags(@NotNull NaturalFlag flag) {
        Preconditions.checkNotNull(flag, "flag cannot be null");

        this.flag = flag;
    }

    /**
     * Find a NaturalFlag enum by its name
     *
     * @param name The name of the enum value
     * @return An Optional containing the matching enum value or empty if not found
     */
    @NotNull
    public static Optional<NaturalFlags> byName(@NotNull String name) {
        Preconditions.checkNotNull(name, "name cannot be null");

        return StringUtils.match(name, NaturalFlags.class);
    }

    /**
     * Find a NaturalFlag enum by its original Lands API NaturalFlag
     *
     * @param naturalFlag The original NaturalFlag
     * @return The matching enum value or null if not found
     */
    @Nullable
    public static NaturalFlags fromNaturalFlag(@NotNull NaturalFlag naturalFlag) {
        Preconditions.checkNotNull(naturalFlag, "naturalFlag cannot be null");

        for (NaturalFlags flag : values()) {
            if (!flag.flag.equals(naturalFlag))
                continue;

            return flag;
        }

        return null;
    }

    /**
     * Get the original Lands API NaturalFlag
     *
     * @return the original NaturalFlag object
     */
    @NotNull
    public NaturalFlag flag() {
        return flag;
    }
}
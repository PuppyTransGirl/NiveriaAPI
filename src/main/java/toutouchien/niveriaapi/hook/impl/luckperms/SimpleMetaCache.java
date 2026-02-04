package toutouchien.niveriaapi.hook.impl.luckperms;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class SimpleMetaCache implements MetaCache {
    @Override
    public boolean booleanMeta(Player player, String metaKey, boolean defaultValue) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(metaKey, "metaKey cannot be null");

        return defaultValue;
    }

    @Override
    public double doubleMeta(Player player, String metaKey, double defaultValue) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(metaKey, "metaKey cannot be null");

        return defaultValue;
    }

    @Nullable
    @Override
    public <T extends Enum<T>> T enumMeta(Player player, String metaKey, T defaultValue) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(metaKey, "metaKey cannot be null");
        Preconditions.checkNotNull(defaultValue, "defaultValue cannot be null");

        return defaultValue;
    }

    @Nullable
    @Override
    public <T extends Enum<T>> T enumMeta(Player player, String metaKey, Class<T> enumClass, @Nullable T defaultValue) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(metaKey, "metaKey cannot be null");
        Preconditions.checkNotNull(enumClass, "enumClass cannot be null");

        return defaultValue;
    }

    @Override
    public int integerMeta(Player player, String metaKey, int defaultValue) {
        return defaultValue;
    }

    @Nullable
    @Override
    public String stringMeta(Player player, String metaKey, @Nullable String defaultValue) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(metaKey, "metaKey cannot be null");

        return defaultValue;
    }

    @Override
    public void invalidateCache(Player player, String metaKey) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(metaKey, "metaKey cannot be null");

        // Do nothing, as luckperms isn't loaded and we don't cache anything
    }

    @Override
    public void invalidateCache(Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        // Do nothing, as luckperms isn't loaded and we don't cache anything
    }
}

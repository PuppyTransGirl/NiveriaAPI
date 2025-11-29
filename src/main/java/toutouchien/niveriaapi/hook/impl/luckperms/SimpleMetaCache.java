package toutouchien.niveriaapi.hook.impl.luckperms;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleMetaCache implements MetaCache {
    @Override
    public boolean booleanMeta(@NotNull Player player, @NotNull String metaKey, boolean defaultValue) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(metaKey, "metaKey cannot be null");

        return defaultValue;
    }

    @Override
    public double doubleMeta(@NotNull Player player, @NotNull String metaKey, double defaultValue) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(metaKey, "metaKey cannot be null");

        return defaultValue;
    }

    @Nullable
    @Override
    public <T extends Enum<T>> T enumMeta(@NotNull Player player, @NotNull String metaKey, @NotNull T defaultValue) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(metaKey, "metaKey cannot be null");
        Preconditions.checkNotNull(defaultValue, "defaultValue cannot be null");

        return defaultValue;
    }

    @Nullable
    @Override
    public <T extends Enum<T>> T enumMeta(@NotNull Player player, @NotNull String metaKey, @NotNull Class<T> enumClass, @Nullable T defaultValue) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(metaKey, "metaKey cannot be null");
        Preconditions.checkNotNull(enumClass, "enumClass cannot be null");

        return defaultValue;
    }

    @Override
    public int integerMeta(@NotNull Player player, @NotNull String metaKey, int defaultValue) {
        return defaultValue;
    }

    @Nullable
    @Override
    public String stringMeta(@NotNull Player player, @NotNull String metaKey, @Nullable String defaultValue) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(metaKey, "metaKey cannot be null");

        return defaultValue;
    }

    @Override
    public void invalidateCache(@NotNull Player player, @NotNull String metaKey) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(metaKey, "metaKey cannot be null");

        // Do nothing, as luckperms isn't loaded and we don't cache anything
    }

    @Override
    public void invalidateCache(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        // Do nothing, as luckperms isn't loaded and we don't cache anything
    }
}

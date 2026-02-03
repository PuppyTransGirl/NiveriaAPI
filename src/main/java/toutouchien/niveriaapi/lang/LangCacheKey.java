package toutouchien.niveriaapi.lang;

import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

@NullMarked
public class LangCacheKey {
    private final Locale locale;
    private final String key;
    private final ObjectList<?> placeholders;

    LangCacheKey(Locale locale, String key, ObjectList<?> placeholders) {
        this.locale = locale;
        this.key = key;
        this.placeholders = placeholders;
    }

    public Locale locale() {
        return locale;
    }

    public String key() {
        return key;
    }

    public ObjectList<?> placeholders() {
        return placeholders;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o)
            return true;

        if (!(o instanceof LangCacheKey that))
            return false;

        return locale.equals(that.locale)
                && key.equals(that.key)
                && placeholders.equals(that.placeholders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                locale,
                key,
                placeholders
        );
    }
}

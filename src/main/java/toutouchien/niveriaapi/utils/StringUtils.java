package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

/**
 * Utility class providing static methods for string manipulations.
 */
public class StringUtils {
    private StringUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Capitalizes the first character of a string and converts the rest to lowercase.
     *
     * @param string The string to capitalize
     * @return The capitalized string, or the original string if null or blank
     */
    @NotNull
    public static String capitalize(@NotNull String string) {
        Preconditions.checkNotNull(string, "string cannot be null");

        if (string.isBlank())
            return string;

        return string.toUpperCase().charAt(0) + string.toLowerCase(Locale.ROOT).substring(1);
    }

    /**
     * Matches a string to an enum constant, ignoring case.
     *
     * @param key       The string to match
     * @param enumClass The enum class to match against
     * @param <T>       The type of the enum
     * @return An Optional containing the matched enum constant, or empty if no match is found
     */
    @NotNull
    public static <T extends Enum<T>> Optional<T> match(@Nullable String key, @NotNull Class<T> enumClass) {
        Preconditions.checkNotNull(enumClass, "enumClass cannot be null");
        if (key == null)
            return Optional.empty();

        try {
            return Optional.of(Enum.valueOf(enumClass, key.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Matches a string to an enum constant, ignoring case.
     *
     * @param key          The string to match
     * @param enumClass    The enum class to match against
     * @param defaultValue The default value to return if no match is found
     * @param <T>          The type of the enum
     * @return The matched enum constant, or the default value if no match is found
     */
    @Nullable
    public static <T extends Enum<T>> T match(@Nullable String key, @NotNull Class<T> enumClass, @Nullable T defaultValue) {
        Preconditions.checkNotNull(enumClass, "enumClass cannot be null");
        if (key == null)
            return defaultValue;

        try {
            return Enum.valueOf(enumClass, key.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    /**
     * Returns the plural form of a word based on the count.
     * Adds "s" to the singular form for pluralization.
     *
     * @param singular The singular form of the word
     * @param count    The count to determine singular or plural
     * @return The appropriate singular or plural form
     */
    @NotNull
    public static String pluralize(@NotNull String singular, int count) {
        Preconditions.checkNotNull(singular, "singular cannot be null");

        return pluralize(singular, singular + "s", count);
    }

    /**
     * Returns the appropriate form of a word based on the count.
     *
     * @param singular The singular form of the word
     * @param plural   The plural form of the word
     * @param count    The count to determine singular or plural
     * @return The appropriate singular or plural form
     */
    @NotNull
    public static String pluralize(@NotNull String singular, @NotNull String plural, int count) {
        Preconditions.checkNotNull(singular, "singular cannot be null");
        Preconditions.checkNotNull(plural, "plural cannot be null");

        return -1 <= count && count <= 1 ? singular : plural;
    }

    /**
     * Compares two semantic-version-like strings component-wise.
     *
     * <p>Behavior:
     * - Versions are split on '.' and compared component by component as integers.
     * - Missing components are treated as 0 (e.g. "1.2" == "1.2.0").
     * - Leading zeros are allowed and parsed normally ("01" -> 1).
     * - If either component isn't a valid integer, this method throws
     * {@link NumberFormatException}. Callers can catch it if they expect
     * non-numeric parts (pre-releases) or validate beforehand.
     *
     * <p>Limitations:
     * - This is a numeric component comparator only. It does not implement
     * full Semantic Versioning precedence rules (no handling of pre-release
     * identifiers or build metadata).
     *
     * @param a first version string (non-null)
     * @param b second version string (non-null)
     * @return a positive integer if a &gt; b, zero if equal, a negative integer if a &lt; b
     * @throws NullPointerException  if a or b is null
     * @throws NumberFormatException if any numeric component cannot be parsed as an int
     */
    public static int compareSemVer(@NotNull String a, @NotNull String b) {
        Preconditions.checkNotNull(a, "a cannot be null");
        Preconditions.checkNotNull(b, "b cannot be null");

        String[] as = a.split("\\.");
        String[] bs = b.split("\\.");

        int n = Math.max(as.length, bs.length);
        for (int i = 0; i < n; i++) {
            int ai = i < as.length ? Integer.parseInt(as[i]) : 0;
            int bi = i < bs.length ? Integer.parseInt(bs[i]) : 0;

            if (ai != bi)
                return Integer.compare(ai, bi);
        }

        return 0;
    }
}

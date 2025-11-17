package toutouchien.niveriaapi.utils.common;

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
	public static String capitalize(String string) {
		if (string == null || string.isBlank())
			return string;

		return string.toUpperCase().charAt(0) + string.toLowerCase().substring(1);
	}

	/**
	 * Converts an enum constant name to a display name by capitalizing each part.
	 * Example: "ENUM_CONSTANT" becomes "EnumConstant"
	 *
	 * @param toConvert The enum constant name to convert
	 * @return The formatted display name
	 */
	public static String enumToDisplayName(String toConvert) {
		String[] parts = toConvert.split("_");
		if(parts.length == 0)
			return "";

		StringBuilder displayName = new StringBuilder();
		for (int i = 0; i < parts.length; i++)
			displayName.append(capitalize(parts[i]));

		return displayName.toString();
	}

	/**
	 * Safely matches a string key to an enum constant.
	 *
	 * @param key The string key to match
	 * @param enumClass The enum class to search in
	 * @param <T> The enum type
	 * @return An Optional containing the matched enum constant, or empty if no match found
	 */
	public static <T extends Enum<T>> Optional<T> match(String key, Class<T> enumClass) {
		try {
			return Optional.of(Enum.valueOf(enumClass, key.toUpperCase(Locale.ROOT)));
		} catch (NullPointerException | IllegalArgumentException e) {
			return Optional.empty();
		}
	}

    /**
     * Safely matches a string key to an enum constant, returning a default value if no match is found.
     *
     * @param key The string key to match
     * @param enumClass The enum class to search in
     * @param <T> The enum type
     * @param defaultValue The default value to return if no match is found
     * @return The matched enum constant, or the default value if no match found
     */
    public static <T extends Enum<T>> T match(String key, Class<T> enumClass, T defaultValue) {
        try {
            return Enum.valueOf(enumClass, key.toUpperCase(Locale.ROOT));
        } catch (NullPointerException | IllegalArgumentException e) {
            return defaultValue;
        }
    }
}

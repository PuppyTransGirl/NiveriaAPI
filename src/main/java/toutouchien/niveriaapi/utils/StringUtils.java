package toutouchien.niveriaapi.utils;

public class StringUtils {
	public static String capitalize(String string) {
		if (string == null || string.isBlank())
			return string;

		return string.toUpperCase().charAt(0) + string.toLowerCase().substring(1);
	}

	public static String enumToDisplayName(String toConvert) {
		String[] parts = toConvert.split("_");
		if(parts.length == 0)
			return "";

		StringBuilder displayName = new StringBuilder();
		for (int i = 0; i < parts.length; i++)
			displayName.append(capitalize(parts[i]));

		return displayName.toString();
	}
}

package toutouchien.niveriaapi.utils;

public class StringUtils {
	public static String capitalize(String string) {
		if (string == null || string.isBlank())
			return string;

		return string.toUpperCase().charAt(0) + string.toLowerCase().substring(1);
	}
}

package toutouchien.niveriaapi.utils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for time conversions and formatting.
 */
public class TimeUtils {
	private TimeUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static long ticks(Duration duration) {
		return duration.toMillis() / 50L;
	}

	public static long ticks(long time, TimeUnit unit) {
		return unit.toMillis(time) / 50L;
	}

	/**
	 * Parses milliseconds into a human-readable string in French.
	 * Example: "1 an 2 mois 3 semaines 4 jours 5 heures 6 minutes 7 secondes"
	 *
	 * @param millis The time in milliseconds
	 * @return A formatted string representation of the duration
	 */
	public static String parseMillis(long millis) {
		if (millis < 1000)
			return "0 seconde";

		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		long weeks = days / 7;
		long months = weeks / 4;
		long years = months / 12;

		seconds %= 60;
		minutes %= 60;
		hours %= 24;
		days %= 7;
		weeks %= 4;
		months %= 12;

		StringBuilder result = new StringBuilder();
		appendUnit(result, years, "an");
		appendUnit(result, months, "mois");
		appendUnit(result, weeks, "semaine");
		appendUnit(result, days, "jour");
		appendUnit(result, hours, "heure");
		appendUnit(result, minutes, "minute");
		appendUnit(result, seconds, "seconde");

		return result.toString().trim();
	}

	private static void appendUnit(StringBuilder result, long value, String unit) {
		if (value <= 0)
			return;

		result.append(value).append(" ").append(unit);

		if (value > 1 && !unit.equals("mois"))
			result.append("s");

		result.append(" ");
	}
}

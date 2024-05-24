package toutouchien.niveriaapi.utils;

public class TimeUtils {
	private static final int MILLISECONDS_PER_SECOND = 1000;
	private static final int TICKS_PER_SECOND = 20;
	private static final int SECONDS_PER_MINUTE = 60;
	private static final int MINUTES_PER_HOUR = 60;
	private static final int HOURS_PER_DAY = 24;

	public static long secondsToTicks(long seconds) {
		return seconds * TICKS_PER_SECOND;
	}

	public static long minutesToTicks(long minutes) {
		return minutes * SECONDS_PER_MINUTE * TICKS_PER_SECOND;
	}

	public static long hoursToTicks(long hours) {
		return hours * MINUTES_PER_HOUR * SECONDS_PER_MINUTE * TICKS_PER_SECOND;
	}

	public static long daysToTicks(long days) {
		return days * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE * TICKS_PER_SECOND;
	}

	public static long secondsToMilliseconds(long seconds) {
		return seconds * MILLISECONDS_PER_SECOND;
	}

	public static long minutesToMilliseconds(long minutes) {
		return minutes * SECONDS_PER_MINUTE * MILLISECONDS_PER_SECOND;
	}

	public static long hoursToMilliseconds(long hours) {
		return hours * MINUTES_PER_HOUR * SECONDS_PER_MINUTE * MILLISECONDS_PER_SECOND;
	}

	public static long daysToMilliseconds(long days) {
		return days * HOURS_PER_DAY * MINUTES_PER_HOUR * SECONDS_PER_MINUTE * MILLISECONDS_PER_SECOND;
	}

	public static String parseSeconds(long seconds) {
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		long weeks = days / 7;
		long months = (long) (weeks / 4.1);
		long years = months / 12;

		seconds %= 60;
		minutes %= 60;
		hours %= 24;
		days %= 7;
		weeks %= (long) 4.1;
		months %= 12;

		StringBuilder result = new StringBuilder();
		if (years > 0) result.append(years).append(" ans ");
		if (months > 0) result.append(months).append(" mois ");
		if (weeks > 0) result.append(weeks).append(" semaines ");
		if (days > 0) result.append(days).append(" jours ");
		if (hours > 0) result.append(hours).append(" heures ");
		if (minutes > 0) result.append(minutes).append(" minutes ");
		if (seconds > 0) result.append(seconds).append(" secondes ");

		return result.toString().trim();
	}
}

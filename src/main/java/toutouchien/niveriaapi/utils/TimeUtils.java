package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.lang.Lang;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for time conversions and formatting.
 */
public class TimeUtils {
    private TimeUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static long ticks(@NotNull Duration duration) {
        Preconditions.checkNotNull(duration, "duration cannot be null");

        return duration.toMillis() / 50L;
    }

    public static long ticks(long time, @NotNull TimeUnit unit) {
        Preconditions.checkNotNull(unit, "unit cannot be null");

        return unit.toMillis(time) / 50L;
    }

    /**
     * Parses milliseconds into a human-readable string in French.
     * Example: "1 an 2 mois 3 semaines 4 jours 5 heures 6 minutes 7 secondes"
     *
     * @param millis The time in milliseconds
     * @return A formatted string representation of the duration
     */
    @NotNull
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
        appendUnit(result, years, "niveriaapi_timeutils_year");
        appendUnit(result, months, "niveriaapi_timeutils_month");
        appendUnit(result, weeks, "niveriaapi_timeutils_week");
        appendUnit(result, days, "niveriaapi_timeutils_day");
        appendUnit(result, hours, "niveriaapi_timeutils_hour");
        appendUnit(result, minutes, "niveriaapi_timeutils_minute");
        appendUnit(result, seconds, "niveriaapi_timeutils_second");

        return result.toString().trim();
    }

    private static void appendUnit(@NotNull StringBuilder result, long value, @NotNull String unit) {
        Preconditions.checkNotNull(result, "result cannot be null");
        Preconditions.checkNotNull(unit, "unit cannot be null");

        if (value <= 0)
            return;

        result.append(value).append(" ");

        String finalUnit = unit;
        if (value > 1)
            finalUnit += "s";

        result.append(Lang.getString(finalUnit)).append(" ");
    }
}

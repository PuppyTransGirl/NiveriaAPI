package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import org.checkerframework.checker.index.qual.Positive;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {
    private MathUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static double decimalRound(double value, @Positive int scale) {
        Preconditions.checkArgument(scale >= 1, "scale cannot be less than 1: %d", scale);

        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_EVEN).doubleValue();
    }

    public static float decimalRound(float value, @Positive int scale) {
        Preconditions.checkArgument(scale >= 1, "scale cannot be less than 1: %d", scale);

        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_EVEN).floatValue();
    }
}

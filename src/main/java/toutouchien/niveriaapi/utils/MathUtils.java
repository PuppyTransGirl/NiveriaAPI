package toutouchien.niveriaapi.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {
	private MathUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static double decimalRound(double value, int scale) {
		return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_EVEN).doubleValue();
	}

	public static float decimalRound(float value, int scale) {
		return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_EVEN).floatValue();
	}
}

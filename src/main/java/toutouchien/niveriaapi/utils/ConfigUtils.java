package toutouchien.niveriaapi.utils;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ConfigUtils {
	// It's to make a 1/16 (0.625) precision like the pixel of a block
	private final static DecimalFormat decimalFormat = new DecimalFormat("#.####", new DecimalFormatSymbols(Locale.US));
	static {
		decimalFormat.setGroupingUsed(false);
	}

	public static void setLocation(ConfigurationSection section, Location location) {
		String world = location.getWorld().getName();
		double x = roundDouble(location.x());
		double y = roundDouble(location.y());
		double z = roundDouble(location.z());
		float yaw = roundFloat(location.getYaw());
		float pitch = roundFloat(location.getPitch());

		section.set("world", world);
		section.set("x", x);
		section.set("y", y);
		section.set("z", z);
		section.set("yaw", yaw);
		section.set("pitch", pitch);
	}

	private static double roundDouble(double d) {
		return Double.parseDouble(decimalFormat.format(d));
	}

	private static float roundFloat(float f) {
		return Float.parseFloat(decimalFormat.format(f));
	}
}

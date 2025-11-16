package toutouchien.niveriaapi.commandold.impl.niveriaapi;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.commandold.CommandData;
import toutouchien.niveriaapi.commandold.SubCommand;
import toutouchien.niveriaapi.utils.ui.ColorUtils;
import toutouchien.niveriaapi.utils.ui.MessageUtils;

import java.text.DecimalFormat;
import java.util.Map;

public class NiveriaAPIPing extends SubCommand {
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

	NiveriaAPIPing() {
		super(new CommandData("ping", "niveriaapi"));
	}

	@Override
	public void execute(@NotNull CommandSender sender, String @NotNull [] args, @NotNull String label) {
		Map<String, Long> pings = NiveriaAPI.instance().mongoManager().ping();
		sender.sendMessage(MessageUtils.infoMessage(Component.text("Ping des bases de données:")));
		pings.forEach((name, ping) -> {
			double pingMs = (double) ping / 1_000_000.0;
			String formattedPing = DECIMAL_FORMAT.format(pingMs);

			sender.sendMessage(
					Component.text()
					.append(Component.text(name, ColorUtils.primaryColor()))
					.append(Component.text(" - ", NamedTextColor.DARK_GRAY))
					.append(Component.text(formattedPing + " ms"))
			);
		});
	}
}

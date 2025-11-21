package toutouchien.niveriaapi.command.impl.niveriaapi;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.command.CommandData;
import toutouchien.niveriaapi.command.SubCommand;
import toutouchien.niveriaapi.lang.Lang;

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
        Lang.sendMessage(sender, "niveriaapi_command_niveriaapi_subcommand_ping");
		pings.forEach((name, ping) -> {
			double pingMs = (double) ping / 1_000_000.0;
			String formattedPing = DECIMAL_FORMAT.format(pingMs);

			Lang.sendMessage(sender, "niveriaapi_command_niveriaapi_subcommand_ping_line", name, formattedPing);
		});
	}
}

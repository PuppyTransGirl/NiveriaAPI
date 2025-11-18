package toutouchien.niveriaapi.command.impl.niveriaapi;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.command.Command;
import toutouchien.niveriaapi.command.CommandData;
import toutouchien.niveriaapi.lang.Lang;

public class NiveriaAPI extends Command {
	public NiveriaAPI() {
		super(new CommandData("niveriaapi", "niveriaapi")
				.description("Permet de gérer le plugin d'API de Niveria.")
				.usage("<ping>")
				.subCommands(new NiveriaAPIPing()));
	}

	@Override
	public void execute(@NotNull CommandSender sender, String @NotNull [] args, @NotNull String label) {
        Lang.sendMessage(sender, "niveria_command_niveriaapi_no_arg");
	}
}

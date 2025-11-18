package toutouchien.niveriaapi.commandold.impl.niveriaapi;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.commandold.Command;
import toutouchien.niveriaapi.commandold.CommandData;
import toutouchien.niveriaapi.utils.MessageUtils;

public class NiveriaAPI extends Command {
	public NiveriaAPI() {
		super(new CommandData("niveriaapi", "niveriaapi")
				.description("Permet de gérer le plugin d'API de Niveria.")
				.usage("<ping>")
				.subCommands(new NiveriaAPIPing()));
	}

	@Override
	public void execute(@NotNull CommandSender sender, String @NotNull [] args, @NotNull String label) {
		sender.sendMessage(MessageUtils.errorMessage(Component.text("Tu n'as pas spécifié de sous-commande.")));
		sender.sendMessage(MessageUtils.infoMessage(Component.text("Les sous-commandes possibles sont ping.")));
	}
}

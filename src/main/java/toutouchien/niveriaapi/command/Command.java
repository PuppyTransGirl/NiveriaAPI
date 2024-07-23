package toutouchien.niveriaapi.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Command extends org.bukkit.command.Command {
	private final CommandData commandData;
	private final Plugin plugin;

	protected Command(CommandData commandData) {
		super(commandData.name());
		this.commandData = commandData;
		this.plugin = commandData.plugin();

		this.setAliases(commandData.aliases())
				.setDescription(commandData.description())
				.setUsage(commandData.usage());
	}

	public void execute(CommandSender sender, String[] args, String label) {

	}

	public void execute(Player player, String[] args, String label) {

	}

	public List<String> complete(CommandSender sender, String[] args, int argIndex) {
		return Collections.emptyList();
	}

	public List<String> complete(Player player, String[] args, int argIndex) {
		return Collections.emptyList();
	}

	public CommandData data() {
		return commandData;
	}

	public Plugin plugin() {
		return plugin;
	}

	public Component usageMessage(String label) {
		return Component.text(commandData.usage().replace("<command>", label), NamedTextColor.RED);
	}

	@Override
	public final boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		if (!commandData.permission().isEmpty()) {
			if (!sender.hasPermission(commandData.permission())) {
				sender.sendMessage(Component.text("Tu n'as pas la permission d'exécuter cette commande.", NamedTextColor.RED));

				return true;
			}
		}

		List<SubCommand> subCommands = commandData.subCommands();
		if (!subCommands.isEmpty() && args.length > 0) {
			String[] finalArgs = args;
			SubCommand subCommand = subCommands.stream()
					.filter(sc -> sc.data().name().equalsIgnoreCase(finalArgs[0]))
					.findAny().orElse(null);

			if (subCommand != null) {
				args = Arrays.copyOfRange(args, 1, args.length);

				if (subCommand.data().playerRequired()) {
					if (!(sender instanceof Player)) {
						sender.sendMessage(Component.text("Seuls les joueurs peuvent exécuter cette sous-commande.", NamedTextColor.RED));
						return true;
					}

					subCommand.execute((Player) sender, args, commandLabel);
					return true;
				}

				subCommand.execute(sender, args, commandLabel);
				return true;
			}
		}

		if (commandData.playerRequired()) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(Component.text("Seuls les joueurs peuvent exécuter cette commande.", NamedTextColor.RED));
				return true;
			}

			execute((Player) sender, args, commandLabel);
			return true;
		}

		execute(sender, args, commandLabel);
		return true;
	}

	@Override
	public final @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		int argIndex = args.length - 1;

		if (commandData.playerRequired()) {
			if (!(sender instanceof Player))
				return Collections.emptyList();

			List<SubCommand> subCommands = commandData.subCommands();
			if (!subCommands.isEmpty()) {
				if (argIndex == 0) {
					return subCommands.stream()
							.map(subCommand -> subCommand.data().name())
							.toList();
				}

				String[] finalArgs = args;
				SubCommand subCommand = subCommands.stream()
						.filter(sc -> sc.data().name().equalsIgnoreCase(finalArgs[0]))
						.findAny().orElse(null);

				if (subCommand != null) {
					args = Arrays.copyOfRange(args, 1, args.length);
					argIndex -= 1;
					return subCommand.complete(((Player) sender), args, argIndex);
				}
			}

			return complete((Player) sender, args, argIndex);
		}

		List<SubCommand> subCommands = commandData.subCommands();
		if (!subCommands.isEmpty()) {
			if (argIndex == 0) {
				return subCommands.stream()
						.map(subCommand -> subCommand.data().name())
						.toList();
			}

			String[] finalArgs = args;
			SubCommand subCommand = subCommands.stream()
					.filter(sc -> sc.data().name().equalsIgnoreCase(finalArgs[0]))
					.findAny().orElse(null);

			if (subCommand != null) {
				args = Arrays.copyOfRange(args, 1, args.length);
				argIndex -= 1;
				return subCommand.complete(sender, args, argIndex);
			}
		}

		return complete(sender, args, argIndex);
	}

	@Override
	public final @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args, @Nullable Location location) throws IllegalArgumentException {
		return super.tabComplete(sender, alias, args, location);
	}
}

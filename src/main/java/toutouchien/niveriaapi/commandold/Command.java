package toutouchien.niveriaapi.commandold;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Command extends org.bukkit.command.Command {
	private final CommandData commandData;

	protected Command(CommandData commandData) {
		super(commandData.name());
		this.commandData = commandData;

		this.setAliases(commandData.aliases())
				.setDescription(commandData.description())
				.setUsage(commandData.usage());
	}

	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args, @NotNull String @NotNull [] fullArgs, @NotNull String label) {
		execute(sender, args, label);
	}

	public void execute(@NotNull Player player, @NotNull String @NotNull [] args, @NotNull String @NotNull [] fullArgs, @NotNull String label) {
		execute(player, args, label);
	}

	public void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args, @NotNull String label) {
	}

	public void execute(@NotNull Player player, @NotNull String @NotNull [] args, @NotNull String label) {
	}

	public List<String> complete(@NotNull CommandSender sender, @NotNull String @NotNull [] args, @NotNull String @NotNull [] fullArgs, int argIndex) {
		return complete(sender, args, argIndex);
	}

	public List<String> complete(@NotNull Player player, @NotNull String @NotNull [] args, @NotNull String @NotNull [] fullArgs, int argIndex) {
		return complete(player, args, argIndex);
	}

	public List<String> complete(@NotNull CommandSender sender, @NotNull String @NotNull [] args, int argIndex) {
		return null;
	}

	public List<String> complete(@NotNull Player player, @NotNull String @NotNull [] args, int argIndex) {
		return null;
	}

	public CommandData data() {
		return commandData;
	}

	public Component usageMessage(String label) {
		return Component.text(commandData.usage().replace("<command>", label), NamedTextColor.RED);
	}

	@Override
	public final boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String @NotNull [] args) {
		// Check permission
		if (!commandData.permission().isEmpty() && !sender.hasPermission(commandData.permission())) {
			sender.sendMessage(Component.text("Tu n'as pas la permission d'exécuter cette commande.", NamedTextColor.RED));
			return true;
		}

		List<SubCommand> subCommands = commandData.subCommands();
		if (!subCommands.isEmpty() && args.length > 0) {
			SubCommand subCommand = subCommands.stream()
					.filter(sc -> sc.data().name().equalsIgnoreCase(args[0]) || sc.data().aliases().contains(args[0]))
					.findAny()
					.orElse(null);

			if (subCommand != null) {
				// If this sub-command expects a parameter before nested sub-commands...
				if (subCommand.data().hasParameterBeforeSubcommands()) {
					if (args.length < 2) {
						// Not enough arguments: missing the parameter
						sender.sendMessage(usageMessage(commandLabel));
						return true;
					}
					// args[1] is the parameter; remaining args may include a nested sub-command
					String[] remainingArgs = Arrays.copyOfRange(args, 2, args.length);
					if (!subCommand.data().subCommands().isEmpty() && remainingArgs.length > 0) {
						SubCommand nestedSubCommand = subCommand.data().subCommands().stream()
								.filter(sc -> sc.data().name().equalsIgnoreCase(remainingArgs[0]) || sc.data().aliases().contains(remainingArgs[0]))
								.findAny()
								.orElse(null);
						if (nestedSubCommand != null) {
							// Found a nested sub-command; execute it.
							String[] nestedRemainingArgs = Arrays.copyOfRange(remainingArgs, 1, remainingArgs.length);
							if (nestedSubCommand.data().playerRequired() && !(sender instanceof Player)) {
								sender.sendMessage(Component.text("Seuls les joueurs peuvent exécuter cette sous-commande.", NamedTextColor.RED));
								return true;
							}
							if (sender instanceof Player player)
								nestedSubCommand.execute(player, nestedRemainingArgs, args, commandLabel);
							else
								nestedSubCommand.execute(sender, nestedRemainingArgs, args, commandLabel);
							return true;
						}
					}
					// No nested sub-command found: execute the sub-command itself.
					// Pass the parameter (and any additional arguments) to the sub-command.
					String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
					if (subCommand.data().playerRequired() && !(sender instanceof Player)) {
						sender.sendMessage(Component.text("Seuls les joueurs peuvent exécuter cette sous-commande.", NamedTextColor.RED));
						return true;
					}
					if (sender instanceof Player player)
						subCommand.execute(player, subArgs, args, commandLabel);
					else
						subCommand.execute(sender, subArgs, args, commandLabel);
					return true;
				} else {
					// Regular sub-command execution (without parameter-before-subcommand)
					String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);
					if (!subCommand.data().subCommands().isEmpty() && remainingArgs.length != 0)
						return subCommand.execute(sender, commandLabel, remainingArgs);

					if (subCommand.data().playerRequired()) {
						if (!(sender instanceof Player)) {
							sender.sendMessage(Component.text("Seuls les joueurs peuvent exécuter cette sous-commande.", NamedTextColor.RED));
							return true;
						}
						subCommand.execute((Player) sender, remainingArgs, args, commandLabel);
						return true;
					}
					subCommand.execute(sender, remainingArgs, args, commandLabel);
					return true;
				}
			}
		}

		if (commandData.playerRequired()) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(Component.text("Seuls les joueurs peuvent exécuter cette commande.", NamedTextColor.RED));
				return true;
			}
			execute((Player) sender, args, args, commandLabel);
			return true;
		}

		execute(sender, args, args, commandLabel);
		return true;
	}

	@Override
	public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		return tabComplete(sender, alias, args, args);
	}

	public final @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args, String[] fullArgs) throws IllegalArgumentException {
		int argIndex = args.length - 1;

		if (commandData.playerRequired() && !(sender instanceof Player))
			return Collections.emptyList();

		List<SubCommand> subCommands = commandData.subCommands();
		if (subCommands.isEmpty()) {
			if (commandData.playerRequired())
				return complete((Player) sender, args, fullArgs, argIndex);

			return complete(sender, args, fullArgs, argIndex);
		}

		if (argIndex == 0) {
			List<String> completion;
			if (sender instanceof Player player)
				completion = complete(player, args, fullArgs, argIndex);
			else
				completion = complete(sender, args, fullArgs, argIndex);

			if (completion != null)
				return completion;

			return subCommands.stream()
					.map(subCommand -> subCommand.data().name())
					.toList();
		}

		SubCommand subCommand = subCommands.stream()
				.filter(sc -> sc.data().name().equalsIgnoreCase(args[0]) || sc.data().aliases().contains(args[0]))
				.findAny()
				.orElse(null);

		if (subCommand == null) {
			if (commandData.playerRequired())
				return complete((Player) sender, args, fullArgs, argIndex);
			return complete(sender, args, fullArgs, argIndex);
		}

		String[] remainingArgs = Arrays.copyOfRange(args, 1, args.length);

		if (subCommand.data().hasParameterBeforeSubcommands()) {
			if (remainingArgs.length == 1)
				return completionOrEmpty(subCommand, sender, remainingArgs, fullArgs, 0);

			if (remainingArgs.length == 2) {
				List<String> customCompletion = completionOrNull(subCommand, sender, remainingArgs, fullArgs, 1);
				if (customCompletion != null) { // not null means the command completion actually returned something and not our api
					return customCompletion;
				}

				// Fall back to subcommand names if no custom completion
				return subCommand.data().subCommands().stream()
						.map(nestedCmd -> nestedCmd.data().name())
						.toList();
			}

			if (remainingArgs.length > 2) {
				SubCommand nestedSubCommand = findSubCommand(subCommand.data().subCommands(), remainingArgs[1]);

				if (nestedSubCommand != null) {
					String[] nestedRemainingArgs = Arrays.copyOfRange(remainingArgs, 2, remainingArgs.length);
					return completionOrEmpty(nestedSubCommand, sender, nestedRemainingArgs, fullArgs, nestedRemainingArgs.length - 1);
				}
			}
		} else {
			if (!subCommand.data().subCommands().isEmpty()) {
				if (remainingArgs.length == 1) {
					return subCommand.data().subCommands().stream()
							.map(nestedCmd -> nestedCmd.data().name())
							.toList();
				}

				if (remainingArgs.length > 1) {
					SubCommand nestedSubCommand = findSubCommand(subCommand.data().subCommands(), remainingArgs[0]);

					if (nestedSubCommand != null) {
						String[] nestedRemainingArgs = Arrays.copyOfRange(remainingArgs, 1, remainingArgs.length);
						return nestedSubCommand.tabComplete(sender, alias, nestedRemainingArgs);
					}
				}
			}
		}

		return completionOrEmpty(subCommand, sender, remainingArgs, fullArgs, remainingArgs.length - 1);
	}

	private SubCommand findSubCommand(List<SubCommand> subCommands, String arg) {
		return subCommands.stream()
				.filter(sc -> sc.data().name().equalsIgnoreCase(arg) || sc.data().aliases().contains(arg))
				.findAny()
				.orElse(null);
	}

	private List<String> completionOrEmpty(SubCommand subCommand, CommandSender sender, String[] args, String[] fullArgs, int argIndex) {
		List<String> completion;
		if (sender instanceof Player player)
			completion = subCommand.complete(player, args, fullArgs, argIndex);
		else
			completion = subCommand.complete(sender, args, fullArgs, argIndex);

		return completion == null ? Collections.emptyList() : completion;
	}

	private List<String> completionOrNull(SubCommand subCommand, CommandSender sender, String[] args, String[] fullArgs, int argIndex) {
		List<String> completion;
		if (sender instanceof Player player)
			completion = subCommand.complete(player, args, fullArgs, argIndex);
		else
			completion = subCommand.complete(sender, args, fullArgs, argIndex);

		return completion;
	}

	@Override
	public final @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args, @Nullable Location location) throws IllegalArgumentException {
		return super.tabComplete(sender, alias, args, location);
	}
}
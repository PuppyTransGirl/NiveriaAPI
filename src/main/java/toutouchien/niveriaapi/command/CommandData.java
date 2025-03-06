package toutouchien.niveriaapi.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CommandData {
	private final String name;
	private final String pluginName;

	private String description;
	private String usage;
	private String permission;
	private boolean playerRequired;
	private List<String> aliases;
	private List<SubCommand> subCommands;
	private boolean hasParameterBeforeSubcommands;

	public CommandData(String name, String pluginName) {
		this.name = name;
		this.pluginName = pluginName;
		this.subCommands = new ArrayList<>();

		this.description = "";
		this.usage = "/<command>";
		this.permission = pluginName + ".command." + name;
		this.playerRequired = false;
		this.aliases = Collections.emptyList();
	}

	public CommandData description(String description) {
		this.description = description;
		return this;
	}

	public CommandData usage(String usage) {
		this.usage = "/<command> " + usage;
		return this;
	}

	public CommandData permission(String permission) {
		this.permission = permission;
		return this;
	}

	public CommandData playerRequired(boolean playerRequired) {
		this.playerRequired = playerRequired;
		return this;
	}

	public CommandData aliases(String... aliases) {
		this.aliases = Arrays.asList(aliases);
		return this;
	}

	public CommandData subCommands(SubCommand... subCommands) {
		this.subCommands = Arrays.asList(subCommands);
		return this;
	}

	public CommandData hasParameterBeforeSubcommands(boolean hasParameterBeforeSubcommands) {
		this.hasParameterBeforeSubcommands = hasParameterBeforeSubcommands;
		return this;
	}

	public String name() {
		return name;
	}

	public String pluginName() {
		return pluginName;
	}

	public String description() {
		return description;
	}

	public String usage() {
		return usage;
	}

	public String permission() {
		return permission;
	}

	public boolean playerRequired() {
		return playerRequired;
	}

	public List<String> aliases() {
		return aliases;
	}

	public List<SubCommand> subCommands() {
		return subCommands;
	}

	public boolean hasParameterBeforeSubcommands() {
		return hasParameterBeforeSubcommands;
	}
}

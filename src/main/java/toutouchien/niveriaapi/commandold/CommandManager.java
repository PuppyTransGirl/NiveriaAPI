package toutouchien.niveriaapi.commandold;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandManager {
    private final Set<Command> commands = new HashSet<>();

    public void registerCommand(Command command) {
        CommandMap commandMap = Bukkit.getCommandMap();
        String fallbackPrefix = command.data().pluginName();

        commandMap.register(fallbackPrefix, command);
    }

    public void registerCommands(List<Command> commands) {
        if(commands.isEmpty())
            return;

        CommandMap commandMap = Bukkit.getCommandMap();
        String fallbackPrefix = commands.get(0).data().pluginName();

        // Why java
        List<org.bukkit.command.Command> bukkitCommands = commands.stream()
                .map(apiCommand -> (org.bukkit.command.Command) apiCommand)
                .toList();

        commandMap.registerAll(fallbackPrefix, bukkitCommands);
    }

    public Set<Command> commands() {
        return commands;
    }
}

package toutouchien.niveriaapi;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import toutouchien.niveriaapi.command.CommandManager;
import toutouchien.niveriaapi.delay.DelayManager;
import toutouchien.niveriaapi.menu.MenuListener;

public final class NiveriaAPI extends JavaPlugin {
    private static NiveriaAPI INSTANCE;

    private CommandManager commandManager;
    private DelayManager delayManager;

    @Override
    public void onEnable() {
        INSTANCE = this;

        this.commandManager = new CommandManager();
        (this.delayManager = new DelayManager(this)).initialize();

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new MenuListener(), this);
    }

    public CommandManager commandManager() {
        return commandManager;
    }

    public DelayManager delayManager() {
        return this.delayManager;
    }

    public static NiveriaAPI instance() {
        return INSTANCE;
    }
}

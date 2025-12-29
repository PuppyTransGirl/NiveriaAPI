package toutouchien.homeplugin;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import toutouchien.homeplugin.commands.DeleteHomeCommand;
import toutouchien.homeplugin.commands.HomeCommand;
import toutouchien.homeplugin.commands.HomesCommand;
import toutouchien.homeplugin.commands.SetHomeCommand;
import toutouchien.homeplugin.managers.HomeManager;
import toutouchien.niveriaapi.lang.Lang;

import java.util.Arrays;

public class HomePlugin extends JavaPlugin {
    private static HomePlugin instance;

    private HomeManager homeManager;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            Commands registrar = commands.registrar();
            Arrays.asList(
                    DeleteHomeCommand.get(),
                    HomeCommand.get(),
                    HomesCommand.get(),
                    SetHomeCommand.get()
            ).forEach(registrar::register);
        });

        Lang.load(this);

        (this.homeManager = new HomeManager(this.getDataFolder())).start();
    }

    @Override
    public void onDisable() {
        this.homeManager.stop();
    }

    public HomeManager homeManager() {
        return this.homeManager;
    }

    public static HomePlugin instance() {
        return instance;
    }
}

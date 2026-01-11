package toutouchien.homeplugin;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import toutouchien.homeplugin.commands.*;
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
                    HomePluginCommand.get(),

                    DeleteHomeCommand.get(),
                    HomeCommand.get(),
                    HomesCommand.get(),
                    SetHomeCommand.get()
            ).forEach(registrar::register);
        });

        Lang.load(this);

        (this.homeManager = new HomeManager(this.getDataFolder())).start();
    }

    public void reload() {
        Lang.reload(this);
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

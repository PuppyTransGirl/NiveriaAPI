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

    public static Lang LANG;

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

        LANG = Lang.builder(this)
                .addDefaultLanguageFiles("en_US.yml", "fr_FR.yml")
                .build();

        (this.homeManager = new HomeManager(this.getDataFolder())).start();
    }

    public void reload() {
        LANG.reload();
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

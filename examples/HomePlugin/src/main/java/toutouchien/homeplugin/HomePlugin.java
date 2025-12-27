package toutouchien.homeplugin;

import org.bukkit.plugin.java.JavaPlugin;
import toutouchien.homeplugin.managers.HomeManager;

public class HomePlugin extends JavaPlugin {
    private static HomePlugin instance;

    private HomeManager homeManager;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
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

package toutouchien.homeplugin.managers;

import it.unimi.dsi.fastutil.objects.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import toutouchien.homeplugin.models.Home;
import toutouchien.niveriaapi.utils.StringUtils;

import java.io.File;
import java.util.UUID;

public class HomeManager {
    private final File homesFolder;
    private final Object2ObjectMap<UUID, ObjectSet<Home>> homes;

    public HomeManager(File dataFolder) {
        this.homesFolder = new File(dataFolder, "homes");
        this.homes = new Object2ObjectOpenHashMap<>();
    }

    public void start() {
        this.loadHomes();
    }

    public boolean homeExists(UUID uuid, String name) {
        ObjectSet<Home> playerHomes = this.homes.get(uuid);
        if (playerHomes == null)
            return false;

        for (Home home :  playerHomes) {
            if (home.name().equalsIgnoreCase(name))
                return true;
        }

        return false;
    }

    public void createHome(Player player, String homeName) {
        Home newHome = new Home(
                homeName,
                player.getLocation(),
                Material.GRASS_BLOCK
        );

        this.homes.putIfAbsent(player.getUniqueId(), new ObjectOpenHashSet<>()).add(newHome);
    }

    public void loadHomes() {
        File[] files = this.homesFolder.listFiles();
        if (files == null)
            return;

        Object2ObjectMap<UUID, ObjectSet<Home>> tempHomes = new Object2ObjectOpenHashMap<>();

        for (File homeFile : files) {
            FileConfiguration homeConfig = YamlConfiguration.loadConfiguration(homeFile);
            ObjectSet<Home> playerHomes = new ObjectOpenHashSet<>();

            for (String homeName : homeConfig.getKeys(false)) {
                ConfigurationSection section = homeConfig.getConfigurationSection(homeName);
                playerHomes.add(new Home(
                        homeName,
                        section.getLocation("location"),
                        StringUtils.match(section.getString("icon"), Material.class, Material.GRASS_BLOCK)
                ));
            }

            tempHomes.put(UUID.fromString(homeFile.getName()), playerHomes);
        }

        this.homes.putAll(tempHomes);
    }
}

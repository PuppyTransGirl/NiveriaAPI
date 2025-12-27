package toutouchien.homeplugin.managers;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import toutouchien.homeplugin.models.Home;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.delay.Delay;
import toutouchien.niveriaapi.delay.DelayBuilder;
import toutouchien.niveriaapi.lang.Lang;
import toutouchien.niveriaapi.utils.StringUtils;

import java.io.File;
import java.util.UUID;
import java.util.function.Consumer;

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

    public void createHome(Player player, String homeName) {
        Home newHome = new Home(
                homeName,
                player.getLocation(),
                Material.GRASS_BLOCK
        );

        this.homes.putIfAbsent(player.getUniqueId(), new ObjectOpenHashSet<>()).add(newHome);
    }

    public void deleteHome(UUID uuid, String homeName) {
        ObjectSet<Home> playerHomes = this.homes.get(uuid);
        if (playerHomes == null)
            return;

        for (Home home : playerHomes) {
            if (home.name().equalsIgnoreCase(homeName)) {
                playerHomes.remove(home);
                this.homes.put(uuid, playerHomes);
                return;
            }
        }
    }

    public void teleportHome(Player player, String homeName) {
        Home home = this.home(player.getUniqueId(), homeName);
        if (home == null)
            return;

        Consumer<Player> teleportationConsumer = p -> {
            p.teleportAsync(home.location()).thenAcceptAsync(result -> {
                Lang.sendMessage(p, "homeplugin.home.teleported");
            });
        };

        Delay delay = DelayBuilder.of(player)
                .delay(3)
                .chat(true)
                .successConsumer(teleportationConsumer)
                .build();

        NiveriaAPI.instance().delayManager().start(delay);
    }

    public boolean homeExists(UUID uuid, String homeName) {
        ObjectSet<Home> playerHomes = this.homes.get(uuid);
        if (playerHomes == null)
            return false;

        for (Home home : playerHomes) {
            if (home.name().equalsIgnoreCase(homeName))
                return true;
        }

        return false;
    }

    public Home home(UUID uuid, String homeName) {
        ObjectSet<Home> playerHomes = this.homes.get(uuid);
        if (playerHomes == null)
            return null;

        for (Home home : playerHomes) {
            if (home.name().equalsIgnoreCase(homeName))
                return home;
        }

        return null;
    }

    public ObjectSet<Home> homes(UUID uuid) {
        return this.homes.putIfAbsent(uuid, new ObjectOpenHashSet<>());
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

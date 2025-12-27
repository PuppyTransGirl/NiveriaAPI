package toutouchien.homeplugin.models;

import org.bukkit.Location;
import org.bukkit.Material;

public class Home {
    private String name;
    private Location location;
    private Material icon;

    public Home(String name, Location location, Material icon) {
        this.name = name;
        this.location = location;
        this.icon = icon;
    }

    public void name(String name) {
        this.name = name;
    }

    public void location(Location location) {
        this.location = location;
    }

    public void icon(Material icon) {
        this.icon = icon;
    }

    public String name() {
        return this.name;
    }

    public Location location() {
        return this.location;
    }

    public Material icon() {
        return this.icon;
    }
}
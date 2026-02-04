package toutouchien.homeplugin.models;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Home {
    private String name;
    private Location location;
    private Material icon;

    public Home(String name, Location location, Material icon) {
        Preconditions.checkNotNull(name, "name cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkNotNull(icon, "icon cannot be null");

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
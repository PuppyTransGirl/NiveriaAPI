package toutouchien.homeplugin.models;

import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class Home {
    private String name;
    private Location location;
    private Material icon;

    public Home(@NotNull String name, @NotNull Location location, @NotNull Material icon) {
        Preconditions.checkNotNull(name, "name cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkNotNull(icon, "icon cannot be null");

        this.name = name;
        this.location = location;
        this.icon = icon;
    }

    public void name(@NotNull String name) {
        this.name = name;
    }

    public void location(@NotNull Location location) {
        this.location = location;
    }

    public void icon(@NotNull Material icon) {
        this.icon = icon;
    }

    @NotNull
    public String name() {
        return this.name;
    }

    @NotNull
    public Location location() {
        return this.location;
    }

    @NotNull
    public Material icon() {
        return this.icon;
    }
}
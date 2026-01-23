package toutouchien.niveriaapi.updatechecker;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.lang.Lang;
import toutouchien.niveriaapi.utils.StringUtils;
import toutouchien.niveriaapi.utils.Task;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class UpdateChecker {
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    private final URI uri;
    private final JavaPlugin plugin;
    private final String currentVersion;
    private final String langKey;

    private String latestVersion;
    private boolean newestVersion;

    public UpdateChecker(@NotNull String modrinthID, @NotNull JavaPlugin plugin, @NotNull String langKey) {
        try {
            this.uri = new URI("https://api.modrinth.com/v2/project/%s/version?include_changelog=false&game_versions=[\"%s\"]".formatted(
                    modrinthID,
                    Bukkit.getMinecraftVersion()
            ));
        } catch (URISyntaxException e) {
            // should not happen
            throw new RuntimeException(e);
        }

        this.currentVersion = plugin.getPluginMeta().getVersion();
        this.plugin = plugin;
        this.langKey = langKey;

        this.startTask();
    }

    private void startTask() {
        Task.asyncRepeat(task -> {
            this.latestVersion = this.latestVersion();
            this.newestVersion = this.latestVersion != null && StringUtils.compareSemVer(this.currentVersion, this.latestVersion) < 0;

            if (this.newestVersion)
                return;

            Lang.sendMessage(Bukkit.getConsoleSender(), this.langKey);
            Bukkit.getPluginManager().registerEvents(new UpdateCheckerListener(
                    this.newestVersion,
                    this.plugin,
                    this.langKey,
                    this.currentVersion,
                    this.latestVersion
            ), this.plugin);
            task.cancel();
        }, this.plugin, 0L, 24L, TimeUnit.HOURS);
    }

    @SuppressWarnings("java:S2142")
    @Nullable
    private String latestVersion() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(this.uri)
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> resp = this.client.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200)
                return null;

            JsonElement root = gson.fromJson(resp.body(), JsonElement.class);
            JsonArray array = root.getAsJsonArray();
            JsonObject latestObject = array.get(0).getAsJsonObject();

            return latestObject.get("version_number").getAsString();
        } catch (Exception e) {
            return null;
        }
    }
}

package toutouchien.niveriaapi.updatechecker;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.lang.Lang;
import toutouchien.niveriaapi.utils.StringUtils;
import toutouchien.niveriaapi.utils.Task;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static toutouchien.niveriaapi.NiveriaAPI.LANG;

@NullMarked
public class UpdateChecker {
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = new Gson();

    private final JavaPlugin plugin;
    private final URI uri;
    private final String currentVersion;
    private final String langKey;

    private String latestVersion;
    private boolean noNewVersion;

    @SuppressWarnings("java:S112")
    public UpdateChecker(JavaPlugin plugin, String modrinthID, String langKey) {
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkNotNull(modrinthID, "modrinthID cannot be null");
        Preconditions.checkNotNull(langKey, "langKey cannot be null");

        this.plugin = plugin;

        try {
            String gameVersionsArray = "[\"%s\"]".formatted(Bukkit.getMinecraftVersion());
            this.uri = new URI("https://api.modrinth.com/v2/project/%s/version?include_changelog=false&game_versions=%s".formatted(
                    modrinthID,
                    URLEncoder.encode(gameVersionsArray, StandardCharsets.UTF_8)
            ));
        } catch (URISyntaxException e) {
            // should not happen
            throw new RuntimeException(e);
        }

        this.currentVersion = plugin.getPluginMeta().getVersion();
        this.langKey = langKey;

        this.startTask();
    }

    private void startTask() {
        Task.asyncRepeat(task -> {
            this.latestVersion = this.latestVersion();
            this.noNewVersion = this.latestVersion == null || StringUtils.compareSemVer(this.currentVersion, this.latestVersion) >= 0;

            if (this.noNewVersion)
                return;

            LANG.sendMessage(Bukkit.getConsoleSender(), this.langKey,
                    Lang.unparsedPlaceholder(this.plugin.getName().toLowerCase(Locale.ROOT) + "_current_version", this.currentVersion),
                    Lang.unparsedPlaceholder(this.plugin.getName().toLowerCase(Locale.ROOT) + "_latest_version", this.latestVersion)
            );

            Bukkit.getPluginManager().registerEvents(new UpdateCheckerListener(
                    this.plugin,
                    this.langKey,
                    this.currentVersion,
                    this.latestVersion
            ), this.plugin);

            task.cancel();
        }, this.plugin, 0L, 24L, TimeUnit.HOURS);
    }

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

            // Can happen when the newest versions don't support the server version anymore
            if (array.isEmpty())
                return null;

            JsonObject latestObject = array.get(0).getAsJsonObject();

            return latestObject.get("version_number").getAsString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}

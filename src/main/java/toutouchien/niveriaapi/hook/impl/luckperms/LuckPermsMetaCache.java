package toutouchien.niveriaapi.hook.impl.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.platform.PlayerAdapter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.NiveriaAPI;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LuckPermsMetaCache implements MetaCache {
	private final NiveriaAPI plugin;
	private final LuckPerms luckPerms;
	private final Map<String, Map<String, Object>> cache = new ConcurrentHashMap<>();

	public LuckPermsMetaCache(NiveriaAPI plugin, LuckPerms luckPerms) {
		this.plugin = plugin;
		this.luckPerms = luckPerms;

		registerNodeMutateListener();
	}

	private void registerNodeMutateListener() {
		luckPerms.getEventBus().subscribe(plugin, UserDataRecalculateEvent.class, event -> {
			cache.remove(event.getUser().getUsername());
		});
	}

	@Override
	public boolean booleanMeta(@NotNull Player player, @NotNull String metaKey, boolean defaultValue) {
		Map<String, Object> playerMetas = cache.computeIfAbsent(player.getName().toLowerCase(Locale.ROOT), k -> new HashMap<>());
		Object cachedValue = playerMetas.get(metaKey);
		if (cachedValue != null)
			return (Boolean) cachedValue;

		PlayerAdapter<Player> playerAdapter = luckPerms.getPlayerAdapter(Player.class);
		CachedMetaData metaData = playerAdapter.getMetaData(player);

		String metaValue = metaData.getMetaValue(metaKey);

		boolean finalValue = metaValue == null ? defaultValue : Boolean.parseBoolean(metaValue);
		playerMetas.put(metaKey, finalValue);
		return finalValue;
	}

	@Override
	public double doubleMeta(@NotNull Player player, @NotNull String metaKey, double defaultValue) {
		Map<String, Object> playerMetas = cache.computeIfAbsent(player.getName().toLowerCase(Locale.ROOT), k -> new HashMap<>());
		Object cachedValue = playerMetas.get(metaKey);
		if (cachedValue != null)
			return (Double) cachedValue;

		PlayerAdapter<Player> playerAdapter = luckPerms.getPlayerAdapter(Player.class);
		CachedMetaData metaData = playerAdapter.getMetaData(player);

		String metaValue = metaData.getMetaValue(metaKey);
		double finalValue = defaultValue;

		if (metaValue != null) {
			try {
				finalValue = Double.parseDouble(metaValue);
			} catch (NumberFormatException ignored) {
				// If parsing fails, we keep the default value
			}
		}

		playerMetas.put(metaKey, finalValue);
		return finalValue;
	}

	@Nullable
	@Override
	public <T extends Enum<T>> T enumMeta(@NotNull Player player, @NotNull String metaKey, @NotNull T defaultValue) {
		Class<T> enumClass = defaultValue.getDeclaringClass();
		return enumMeta(player, metaKey, enumClass, defaultValue);
	}

    @Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T enumMeta(@NotNull Player player, @NotNull String metaKey, @NotNull Class<T> enumClass, @Nullable T defaultValue) {
		Map<String, Object> playerMetas = cache.computeIfAbsent(player.getName().toLowerCase(Locale.ROOT), k -> new HashMap<>());
		Object cachedValue = playerMetas.get(metaKey);
		if (cachedValue != null)
			return (T) cachedValue;

		PlayerAdapter<Player> playerAdapter = luckPerms.getPlayerAdapter(Player.class);
		CachedMetaData metaData = playerAdapter.getMetaData(player);

		String metaValue = metaData.getMetaValue(metaKey);
		T finalValue = defaultValue;
		if (metaValue != null) {
			try {
				finalValue = Enum.valueOf(enumClass, metaValue.toUpperCase(Locale.ROOT));
			} catch (IllegalArgumentException ignored) {
				// If parsing fails, we keep the default value
			}
		}

		playerMetas.put(metaKey, finalValue);
		return finalValue;
	}

	@Override
	public int integerMeta(@NotNull Player player, @NotNull String metaKey, int defaultValue) {
		Map<String, Object> playerMetas = cache.computeIfAbsent(player.getName().toLowerCase(Locale.ROOT), k -> new HashMap<>());
		Object cachedValue = playerMetas.get(metaKey);
		if (cachedValue != null)
			return (Integer) cachedValue;

		PlayerAdapter<Player> playerAdapter = luckPerms.getPlayerAdapter(Player.class);
		CachedMetaData metaData = playerAdapter.getMetaData(player);

		String metaValue = metaData.getMetaValue(metaKey);
		int finalValue = defaultValue;

		if (metaValue != null) {
			try {
				finalValue = Integer.parseInt(metaValue);
			} catch (NumberFormatException ignored) {
				// If parsing fails, we keep the default value
			}
		}

		playerMetas.put(metaKey, finalValue);
		return finalValue;
	}

	@Nullable
	@Override
	public String stringMeta(@NotNull Player player, @NotNull String metaKey, @Nullable String defaultValue) {
		Map<String, Object> playerMetas = cache.computeIfAbsent(player.getName().toLowerCase(Locale.ROOT), k -> new HashMap<>());
		Object cachedValue = playerMetas.get(metaKey);
		if (cachedValue != null)
			return cachedValue.toString();

		PlayerAdapter<Player> playerAdapter = luckPerms.getPlayerAdapter(Player.class);
		CachedMetaData metaData = playerAdapter.getMetaData(player);

		String metaValue = metaData.getMetaValue(metaKey);

		String finalValue = metaValue == null ? defaultValue : metaValue;
		playerMetas.put(metaKey, finalValue);
		return finalValue;
	}

	@Override
	public void invalidateCache(@NotNull Player player, @NotNull String metaKey) {
		cache.computeIfAbsent(player.getName().toLowerCase(Locale.ROOT), k -> new HashMap<>()).remove(metaKey);
	}

	@Override
	public void invalidateCache(@NotNull Player player) {
		cache.remove(player.getName().toLowerCase(Locale.ROOT));
	}
}
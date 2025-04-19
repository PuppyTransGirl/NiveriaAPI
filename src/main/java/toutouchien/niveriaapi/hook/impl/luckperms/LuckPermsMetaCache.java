package toutouchien.niveriaapi.hook.impl.luckperms;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;
import org.bukkit.entity.Player;
import toutouchien.niveriaapi.NiveriaAPI;

import java.util.*;
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
		luckPerms.getEventBus().subscribe(plugin, NodeMutateEvent.class, event -> {
			cache.remove(event.getTarget().getIdentifier().getName());
		});
	}

	@Override
	public boolean booleanMeta(Player player, String metaKey, boolean defaultValue) {
		Map<String, Object> playerMetas = cache.computeIfAbsent(player.getName(), k -> new HashMap<>());
		Object cachedValue = playerMetas.get(metaKey);
		if (cachedValue != null)
			return (Boolean) cachedValue;

		User user = luckPerms.getUserManager().getUser(player.getUniqueId());
		if (user == null)
			return defaultValue;

		MetaNode metaNode = user.getNodes(NodeType.META).stream()
				.filter(node -> node.getMetaKey().equals(metaKey))
				.findFirst()
				.orElse(null);

		boolean metaValue = metaNode != null && Boolean.parseBoolean(metaNode.getMetaValue());
		playerMetas.put(metaKey, metaValue);
		return metaValue;
	}

	@Override
	public double doubleMeta(Player player, String metaKey, double defaultValue) {
		Map<String, Object> playerMetas = cache.computeIfAbsent(player.getName(), k -> new HashMap<>());
		Object cachedValue = playerMetas.get(metaKey);
		if (cachedValue != null)
			return (Double) cachedValue;

		User user = luckPerms.getUserManager().getUser(player.getUniqueId());
		if (user == null)
			return defaultValue;

		MetaNode metaNode = user.getNodes(NodeType.META).stream()
				.filter(node -> node.getMetaKey().equals(metaKey))
				.findFirst()
				.orElse(null);

		double metaValue = defaultValue;
		if (metaNode != null) {
			try {
				metaValue = Double.parseDouble(metaNode.getMetaValue());
			} catch (NumberFormatException ignored) {}
		}

		playerMetas.put(metaKey, metaValue);
		return metaValue;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T enumMeta(Player player, String metaKey, Class<T> enumClass, T defaultValue) {
		Map<String, Object> playerMetas = cache.computeIfAbsent(player.getName(), k -> new HashMap<>());
		Object cachedValue = playerMetas.get(metaKey);
		if (cachedValue != null)
			return (T) cachedValue;

		User user = luckPerms.getUserManager().getUser(player.getUniqueId());
		if (user == null)
			return defaultValue;

		MetaNode metaNode = user.getNodes(NodeType.META).stream()
				.filter(node -> node.getMetaKey().equals(metaKey))
				.findFirst()
				.orElse(null);

		T metaValue = defaultValue;
		if (metaNode != null) {
			try {
				metaValue = Enum.valueOf(enumClass, metaNode.getMetaValue().toUpperCase());
			} catch (IllegalArgumentException ignored) {}
		}

		playerMetas.put(metaKey, metaValue);
		return metaValue;
	}

	@Override
	public int integerMeta(Player player, String metaKey, int defaultValue) {
		Map<String, Object> playerMetas = cache.computeIfAbsent(player.getName(), k -> new HashMap<>());
		Object cachedValue = playerMetas.get(metaKey);
		if (cachedValue != null)
			return (Integer) cachedValue;

		User user = luckPerms.getUserManager().getUser(player.getUniqueId());
		if (user == null)
			return defaultValue;

		MetaNode metaNode = user.getNodes(NodeType.META).stream()
				.filter(node -> node.getMetaKey().equals(metaKey))
				.findFirst()
				.orElse(null);

		int metaValue = defaultValue;
		if (metaNode != null) {
			try {
				metaValue = Integer.parseInt(metaNode.getMetaValue());
			} catch (NumberFormatException ignored) {}
		}

		playerMetas.put(metaKey, metaValue);
		return metaValue;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> listMeta(Player player, String metaKey) {
		Map<String, Object> playerMetas = cache.computeIfAbsent(player.getName(), k -> new HashMap<>());
		Object cachedValue = playerMetas.get(metaKey);
		if (cachedValue != null)
			return (List<String>) cachedValue;

		User user = luckPerms.getUserManager().getUser(player.getUniqueId());
		if (user == null)
			return Collections.emptyList();

		MetaNode metaNode = user.getNodes(NodeType.META).stream()
				.filter(node -> node.getMetaKey().equals(metaKey))
				.findFirst()
				.orElse(null);

		List<String> metaValues = metaNode != null ? Arrays.asList(metaNode.getMetaValue().split(",")) : new ArrayList<>();
		playerMetas.put(metaKey, metaValues);
		return metaValues;
	}

	@Override
	public String stringMeta(Player player, String metaKey, String defaultValue) {
		Map<String, Object> playerMetas = cache.computeIfAbsent(player.getName(), k -> new HashMap<>());
		Object cachedValue = playerMetas.get(metaKey);
		if (cachedValue != null)
			return cachedValue.toString();

		User user = luckPerms.getUserManager().getUser(player.getUniqueId());
		if (user == null)
			return defaultValue;

		MetaNode metaNode = user.getNodes(NodeType.META).stream()
				.filter(node -> node.getMetaKey().equals(metaKey))
				.findFirst()
				.orElse(null);

		String metaValue = metaNode == null ? defaultValue : metaNode.getMetaValue();
		playerMetas.put(metaKey, metaValue);
		return metaValue;
	}

	@Override
	public void invalidateCache(Player player, String metaKey) {
		cache.computeIfAbsent(player.getName(), k -> new HashMap<>()).remove(metaKey);
	}

	@Override
	public void invalidateCache(Player player) {
		cache.remove(player.getName());
	}
}
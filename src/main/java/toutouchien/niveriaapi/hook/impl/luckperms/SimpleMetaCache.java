package toutouchien.niveriaapi.hook.impl.luckperms;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SimpleMetaCache implements MetaCache {
	@Override
	public boolean booleanMeta(Player player, String metaKey, boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public double doubleMeta(Player player, String metaKey, double defaultValue) {
		return defaultValue;
	}

	@Override
	public <T extends Enum<T>> T enumMeta(Player player, String metaKey, Class<T> enumClass, T defaultValue) {
		return defaultValue;
	}

	@Override
	public int integerMeta(Player player, String metaKey, int defaultValue) {
		return defaultValue;
	}

	@Override
	public List<String> listMeta(Player player, String metaKey) {
		return Collections.emptyList();
	}

	@Override
	public String stringMeta(Player player, String metaKey, String defaultValue) {
		return defaultValue;
	}

	@Override
	public void invalidateCache(Player player, String metaKey) {}

	@Override
	public void invalidateCache(Player player) {}
}

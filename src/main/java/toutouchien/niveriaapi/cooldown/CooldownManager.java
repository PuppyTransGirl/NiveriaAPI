package toutouchien.niveriaapi.cooldown;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.utils.TimeUtils;

import java.util.*;

public class CooldownManager implements Listener {
	private final Map<String, Cooldown> cooldowns = new HashMap<>();

	public CooldownManager(@NotNull NiveriaAPI plugin) {
		new BukkitRunnable() {
			@Override
			public void run() {
				clearCooldowns();
			}
		}.runTaskTimerAsynchronously(plugin, TimeUtils.minutesToTicks(3), TimeUtils.minutesToTicks(3));
	}

	public void registerCooldown(@NotNull String id, @NotNull Cooldown cooldown) {
		Objects.requireNonNull(id, "ID must not be null");
		Objects.requireNonNull(cooldown, "Cooldown must not be null");

		if (cooldown.expirationTime() == 0)
			return;

		cooldowns.put(id, cooldown);
	}

	@Nullable
	public Cooldown getCooldown(@Nullable String id, @Nullable UUID uuid) {
		clearCooldowns();

		int cooldownsSize = cooldowns.size();

		if (cooldownsSize > 100) {
			return cooldowns.entrySet().stream()
					.filter(entry -> entry.getKey().equals(id) && entry.getValue().uuid().equals(uuid))
					.map(Map.Entry::getValue)
					.findFirst()
					.orElse(null);
		}


		List<Map.Entry<String, Cooldown>> entries = new ArrayList<>(cooldowns.entrySet());
		for (int i = 0; i < cooldownsSize; i++) {
			Map.Entry<String, Cooldown> entry = entries.get(i);
			Cooldown cooldown = entry.getValue();

			if (!entry.getKey().equals(id) || !cooldown.uuid().equals(uuid))
				continue;

			return cooldown;
		}

		return null;
	}

	public boolean isInCooldown(@Nullable String id, @Nullable UUID uuid) {
		clearCooldowns();

		return cooldowns.entrySet().stream()
				.anyMatch(entry -> entry.getKey().equals(id) && entry.getValue().uuid().equals(uuid));
	}

	@Nullable
	public Cooldown getCooldown(@Nullable String id, @Nullable Player player) {
		if (player == null)
			return null;

		clearCooldowns();

		int cooldownsSize = cooldowns.size();

		if (cooldownsSize > 100) {
			return cooldowns.entrySet().stream()
					.filter(entry -> entry.getKey().equals(id) && entry.getValue().uuid().equals(player.getUniqueId()))
					.map(Map.Entry::getValue)
					.findFirst()
					.orElse(null);
		}


		List<Map.Entry<String, Cooldown>> entries = new ArrayList<>(cooldowns.entrySet());
		for (int i = 0; i < cooldownsSize; i++) {
			Map.Entry<String, Cooldown> entry = entries.get(i);
			Cooldown cooldown = entry.getValue();

			if (!entry.getKey().equals(id) || !cooldown.uuid().equals(player.getUniqueId()))
				continue;

			return cooldown;
		}

		return null;
	}

	public boolean isInCooldown(@Nullable String id, @Nullable Player player) {
		clearCooldowns();

		return cooldowns.entrySet().stream()
				.anyMatch(entry -> {
					Cooldown cooldown = entry.getValue();
					return entry.getKey().equals(id) && cooldown.player() != null && cooldown.player().equals(player);
				});
	}

	private void clearCooldowns() {
		long currentTime = System.currentTimeMillis();
		cooldowns.values().removeIf(cooldown -> cooldown.expirationTime() <= currentTime);
	}
}
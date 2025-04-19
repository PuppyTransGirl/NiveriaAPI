package toutouchien.niveriaapi.utils.game;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class NMSUtils {
	private NMSUtils() {
		throw new IllegalStateException("Utility class");
	}

	@NotNull
	public static ServerPlayer getNMSPlayer(@NotNull Player player) {
		return ((CraftPlayer) player).getHandle();
	}

	@NotNull
	public static ServerGamePacketListenerImpl getConnection(@NotNull Player player) {
		return getNMSPlayer(player).connection;
	}

	public static void sendPacket(@NotNull Player player, @NotNull Packet<?> packet) {
		getConnection(player).send(packet);
	}

	public static void sendPackets(@NotNull Player player, @NotNull Packet<?>... packets) {
		ServerGamePacketListenerImpl connection = getConnection(player);
		Arrays.stream(packets).forEach(connection::send);
	}

	public static void sendNonNullPackets(@NotNull Player player, @Nullable Packet<?>... packets) {
		if (packets == null)
			return;

		ServerGamePacketListenerImpl connection = getConnection(player);
		Arrays.stream(packets).filter(Objects::nonNull).forEach(connection::send);
	}

	public static void sendPackets(@NotNull Player player, @NotNull Collection<Packet<?>> packets) {
		ServerGamePacketListenerImpl connection = getConnection(player);
		packets.forEach(connection::send);
	}

	public static void sendNonNullPackets(@NotNull Player player, @Nullable Collection<Packet<?>> packets) {
		if (packets == null)
			return;

		ServerGamePacketListenerImpl connection = getConnection(player);
		packets.stream().filter(Objects::nonNull).forEach(connection::send);
	}
}

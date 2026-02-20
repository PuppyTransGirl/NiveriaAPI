package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.annotations.Shivery;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * Utility class for interacting with Minecraft's NMS (Net Minecraft Server) classes.
 */
@Shivery
@NullMarked
public final class NMSUtils {
    private NMSUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Retrieves the NMS ServerPlayer instance from a Bukkit Player.
     *
     * @param player The Bukkit Player.
     * @return The corresponding NMS ServerPlayer.
     */
    public static ServerPlayer nmsPlayer(Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        return ((CraftPlayer) player).getHandle();
    }

    /**
     * Retrieves the ServerGamePacketListenerImpl (player connection) from a Bukkit Player.
     *
     * @param player The Bukkit Player.
     * @return The corresponding ServerGamePacketListenerImpl.
     */
    public static ServerGamePacketListenerImpl connection(Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        return nmsPlayer(player).connection;
    }

    /**
     * Sends a single packet to the specified player.
     *
     * @param player The Bukkit Player.
     * @param packet The packet to send.
     */
    public static void sendPacket(Player player, Packet<?> packet) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(packet, "packet cannot be null");

        connection(player).send(packet);
    }

    /**
     * Sends multiple packets to the specified player.
     *
     * @param player  The Bukkit Player.
     * @param packets The packets to send.
     */
    public static void sendPackets(Player player, Packet<?>... packets) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(packets, "packets cannot be null");

        ServerGamePacketListenerImpl connection = connection(player);
        Arrays.stream(packets).forEach(connection::send);
    }

    /**
     * Sends multiple non-null packets to the specified player.
     *
     * @param player  The Bukkit Player.
     * @param packets The packets to send.
     */
    public static void sendNonNullPackets(Player player, @Nullable Packet<?>... packets) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(packets, "packets cannot be null");

        ServerGamePacketListenerImpl connection = connection(player);
        Arrays.stream(packets).filter(Objects::nonNull).forEach(connection::send);
    }

    /**
     * Sends a collection of packets to the specified player.
     *
     * @param player  The Bukkit Player.
     * @param packets The collection of packets to send.
     */
    public static void sendPackets(Player player, Collection<Packet<?>> packets) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(packets, "packets cannot be null");

        ServerGamePacketListenerImpl connection = connection(player);
        packets.forEach(connection::send);
    }

    /**
     * Sends a collection of non-null packets to the specified player.
     *
     * @param player  The Bukkit Player.
     * @param packets The collection of packets to send.
     */
    public static void sendNonNullPackets(Player player, Collection<@Nullable Packet<?>> packets) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(packets, "packets cannot be null");

        ServerGamePacketListenerImpl connection = connection(player);
        packets.stream().filter(Objects::nonNull).forEach(connection::send);
    }
}

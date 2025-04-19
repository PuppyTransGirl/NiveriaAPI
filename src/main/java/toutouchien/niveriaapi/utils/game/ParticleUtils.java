package toutouchien.niveriaapi.utils.game;

import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.bukkit.*;
import org.bukkit.craftbukkit.CraftParticle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import toutouchien.niveriaapi.utils.base.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ParticleUtils {
    private ParticleUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> void spawnParticleForPlayer(Player player, Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed, T data, boolean force) {
        NMSUtils.sendPacket(player, new ClientboundLevelParticlesPacket(
                CraftParticle.createParticleParam(particle, data),
                force,
                false,
                location.x(),
                location.y(),
                location.z(),
                (float) offsetX,
                (float) offsetY,
                (float) offsetZ,
                (float) speed,
                count
        ));
    }

    // Display a particle visible only to specific players
    public static <T> void spawnParticleForPlayers(Collection<Player> players, Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed, T data, boolean force) {
        ClientboundLevelParticlesPacket particlesPacket = new ClientboundLevelParticlesPacket(
                CraftParticle.createParticleParam(particle, data),
                force,
                false,
                location.x(),
                location.y(),
                location.z(),
                (float) offsetX,
                (float) offsetY,
                (float) offsetZ,
                (float) speed,
                count
        );

        players.forEach(player -> NMSUtils.sendPacket(player, particlesPacket));
    }

    // Display a single particle at a location
    public static <T> void spawnParticle(Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed, T data, boolean force) {
        spawnParticleForPlayers(location.getWorld().getPlayers(), location, particle, count, offsetX, offsetY, offsetZ, speed, data, force);
    }

    // Display a colored particle
    public static void spawnColoredParticle(Location location, Color color, int count) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
        spawnParticle(location, Particle.DUST, count, 0, 0, 0, 0, dustOptions, false);
    }

    // Display a material particle
    public static void spawnMaterialParticle(Location location, Material material, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        spawnParticle(location, Particle.BLOCK, count, offsetX, offsetY, offsetZ, speed, material.createBlockData(), false);
    }

    // Create a line of particles between two locations
    public static void drawLine(Location start, Location end, Particle particle, double spacing, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        if (!start.getWorld().equals(end.getWorld()))
            throw new IllegalArgumentException("Locations must be in the same world");

        Vector direction = end.clone().subtract(start).toVector();
        double distance = direction.length();
        direction.normalize();

        for (double d = 0; d <= distance; d += spacing) {
            Location particleLocation = start.clone().add(direction.clone().multiply(d));
            spawnParticle(particleLocation, particle, count, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    // Create a circle of particles
    public static void drawCircle(Location center, Particle particle, double radius, int points, double offsetX, double offsetY, double offsetZ, double speed) {
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.x() + radius * Math.cos(angle);
            double z = center.z() + radius * Math.sin(angle);
            Location particleLocation = new Location(center.getWorld(), x, center.y(), z);
            spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    // Create a vertical circle of particles
    public static void drawVerticalCircle(Location center, Particle particle, double radius, int points, Vector normal, double offsetX, double offsetY, double offsetZ, double speed) {
        Vector normalizedNormal = normal.clone().normalize();

        // Create perpendicular vectors
        Vector perpendicular1 = new Vector(0, 1, 0);
        if (Math.abs(normalizedNormal.dot(perpendicular1)) > 0.99)
            perpendicular1 = new Vector(1, 0, 0);

        Vector perpendicular2 = normalizedNormal.clone().crossProduct(perpendicular1).normalize();
        perpendicular1 = perpendicular2.clone().crossProduct(normalizedNormal).normalize();

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            Vector direction = perpendicular1.clone().multiply(Math.cos(angle)).add(perpendicular2.clone().multiply(Math.sin(angle)));
            Location particleLocation = center.clone().add(direction.multiply(radius));
            spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    // Create a sphere of particles
    public static void drawSphere(Location center, Particle particle, double radius, int rings, int pointsPerRing, double offsetX, double offsetY, double offsetZ, double speed) {
        for (int i = 0; i < rings; i++) {
            double phi = Math.PI * i / (rings - 1);
            double ringRadius = radius * Math.sin(phi);
            double y = center.y() + radius * Math.cos(phi);

            for (int j = 0; j < pointsPerRing; j++) {
                double theta = 2 * Math.PI * j / pointsPerRing;
                double x = center.x() + ringRadius * Math.cos(theta);
                double z = center.z() + ringRadius * Math.sin(theta);

                Location particleLocation = new Location(center.getWorld(), x, y, z);
                spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            }
        }
    }

    // Create a helix of particles
    public static void drawHelix(Location start, Vector direction, Particle particle, double radius, double height, int turns, int pointsPerTurn, double offsetX, double offsetY, double offsetZ, double speed) {
        Vector normalizedDirection = direction.clone().normalize();

        // Create perpendicular vectors
        Vector perpendicular1 = new Vector(0, 1, 0);
        if (Math.abs(normalizedDirection.dot(perpendicular1)) > 0.99)
            perpendicular1 = new Vector(1, 0, 0);

        Vector perpendicular2 = normalizedDirection.clone().crossProduct(perpendicular1).normalize();
        perpendicular1 = perpendicular2.clone().crossProduct(normalizedDirection).normalize();

        int totalPoints = turns * pointsPerTurn;
        for (int i = 0; i < totalPoints; i++) {
            double angle = 2 * Math.PI * i / pointsPerTurn;
            double heightOffset = height * i / totalPoints;

            Vector circleOffset = perpendicular1.clone().multiply(radius * Math.cos(angle))
                    .add(perpendicular2.clone().multiply(radius * Math.sin(angle)));

            Location particleLocation = start.clone().add(normalizedDirection.clone().multiply(heightOffset)).add(circleOffset);
            spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    // Create a spiral of particles
    public static void drawSpiral(Location center, Particle particle, double startRadius, double endRadius, double height, int turns, int pointsPerTurn, double offsetX, double offsetY, double offsetZ, double speed) {
        int totalPoints = turns * pointsPerTurn;

        for (int i = 0; i < totalPoints; i++) {
            double fraction = (double) i / totalPoints;
            double angle = 2 * Math.PI * turns * fraction;
            double radius = startRadius + (endRadius - startRadius) * fraction;

            double x = center.x() + radius * Math.cos(angle);
            double y = center.y() + height * fraction;
            double z = center.z() + radius * Math.sin(angle);

            Location particleLocation = new Location(center.getWorld(), x, y, z);
            spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    // Create a vortex of particles
    public static void drawVortex(Location center, Vector direction, Particle particle, double startRadius, double endRadius, double length, int turns, int pointsPerTurn, double offsetX, double offsetY, double offsetZ, double speed) {
        Vector normalizedDirection = direction.clone().normalize();

        // Create perpendicular vectors
        Vector perpendicular1 = new Vector(0, 1, 0);
        if (Math.abs(normalizedDirection.dot(perpendicular1)) > 0.99)
            perpendicular1 = new Vector(1, 0, 0);

        Vector perpendicular2 = normalizedDirection.clone().crossProduct(perpendicular1).normalize();
        perpendicular1 = perpendicular2.clone().crossProduct(normalizedDirection).normalize();

        int totalPoints = turns * pointsPerTurn;
        for (int i = 0; i < totalPoints; i++) {
            double fraction = (double) i / totalPoints;
            double angle = 2 * Math.PI * turns * fraction;
            double radius = startRadius + (endRadius - startRadius) * fraction;

            Vector circleOffset = perpendicular1.clone().multiply(radius * Math.cos(angle))
                    .add(perpendicular2.clone().multiply(radius * Math.sin(angle)));

            Location particleLocation = center.clone().add(normalizedDirection.clone().multiply(length * fraction)).add(circleOffset);
            spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    // Create a wave of particles
    public static void drawWave(Location center, Particle particle, double length, double width, double amplitude, int waves, int pointsPerWave, double offsetX, double offsetY, double offsetZ, double speed) {
        for (int i = 0; i < waves * pointsPerWave; i++) {
            double x = center.x() + length * i / (waves * pointsPerWave);
            double z = center.z();

            for (int j = 0; j < width / 0.5; j++) {
                double z2 = z - width / 2 + j * 0.5;
                double y = center.y() + amplitude * Math.sin(2 * Math.PI * i / pointsPerWave);

                Location particleLocation = new Location(center.getWorld(), x, y, z2);
                spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            }
        }
    }

    // Create a polygon of particles
    public static void drawPolygon(Location center, Particle particle, double radius, int sides, double offsetX, double offsetY, double offsetZ, double speed) {
        List<Location> corners = new ArrayList<>();
        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides;
            double x = center.x() + radius * Math.cos(angle);
            double z = center.z() + radius * Math.sin(angle);
            corners.add(new Location(center.getWorld(), x, center.y(), z));
        }

        for (int i = 0; i < sides; i++)
            drawLine(corners.get(i), corners.get((i + 1) % sides), particle, 0.2, 1, offsetX, offsetY, offsetZ, speed);
    }

    // Create a star of particles
    public static void drawStar(Location center, Particle particle, double outerRadius, double innerRadius, int points, double offsetX, double offsetY, double offsetZ, double speed) {
        List<Location> corners = new ArrayList<>();
        for (int i = 0; i < 2 * points; i++) {
            double angle = Math.PI * i / points;
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double x = center.x() + radius * Math.cos(angle);
            double z = center.z() + radius * Math.sin(angle);
            corners.add(new Location(center.getWorld(), x, center.y(), z));
        }

        for (int i = 0; i < 2 * points; i++)
            drawLine(corners.get(i), corners.get((i + 1) % (2 * points)), particle, 0.2, 1, offsetX, offsetY, offsetZ, speed);
    }

    // Create a cube of particles
    public static void drawCube(Location corner1, Location corner2, Particle particle, double spacing, double offsetX, double offsetY, double offsetZ, double speed) {
        World world = corner1.getWorld();
        if (!world.equals(corner2.getWorld()))
            throw new IllegalArgumentException("Locations must be in the same world");

        double minX = Math.min(corner1.x(), corner2.x());
        double minY = Math.min(corner1.y(), corner2.y());
        double minZ = Math.min(corner1.z(), corner2.z());
        double maxX = Math.max(corner1.x(), corner2.x());
        double maxY = Math.max(corner1.y(), corner2.y());
        double maxZ = Math.max(corner1.z(), corner2.z());

        // Draw edges
        for (double x = minX; x <= maxX; x += spacing) {
            spawnParticle(new Location(world, x, minY, minZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, x, minY, maxZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, x, maxY, minZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, x, maxY, maxZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }

        for (double y = minY; y <= maxY; y += spacing) {
            spawnParticle(new Location(world, minX, y, minZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, minX, y, maxZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, maxX, y, minZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, maxX, y, maxZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }

        for (double z = minZ; z <= maxZ; z += spacing) {
            spawnParticle(new Location(world, minX, minY, z), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, minX, maxY, z), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, maxX, minY, z), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, maxX, maxY, z), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    // Create a particle that follows an entity
    public static BukkitTask followEntity(Entity entity, Plugin plugin, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed, long durationTicks, long interval) {
        BukkitTask task = Task.syncRepeat(() -> {
			if (!entity.isValid())
				return;

            spawnParticle(entity.getLocation().add(0, 1, 0), particle, count, offsetX, offsetY, offsetZ, speed, null, false);
		}, plugin, 0, interval);

        if (durationTicks == -1)
            return task;

        Task.asyncLater(task::cancel, plugin, durationTicks);
        return task;
    }

    // Create a parametric curve of particles
    public static void drawParametricCurve(Location center, Particle particle, Function<Double, Vector> parametricFunction, double tStart, double tEnd, double tStep, double offsetX, double offsetY, double offsetZ, double speed) {
        for (double t = tStart; t <= tEnd; t += tStep) {
            Vector position = parametricFunction.apply(t);
            Location particleLocation = center.clone().add(position);
            spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    // Create a particle animation over time
    public static void animateParticles(Plugin plugin, List<Location> frames, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed, long ticksPerFrame, long durationTicks, boolean loop) {
        final int[] currentFrame = {0};

        BukkitTask task = Task.syncRepeat(() -> {
            if (currentFrame[0] >= frames.size() && loop)
                currentFrame[0] = 0; // Loop animation (optional)

            Location location = frames.get(currentFrame[0]);
            spawnParticle(location, particle, count, offsetX, offsetY, offsetZ, speed, null, false);

            currentFrame[0]++;
        }, plugin, 0, ticksPerFrame);

        if (durationTicks == -1)
            return;

        // Schedule cancellation after durationTicks
        Task.asyncLater(task::cancel, plugin, durationTicks);
    }


    // Create a gradient of particles
    public static void drawGradient(Location start, Location end, int steps, Function<Double, Color> colorFunction, double offsetX, double offsetY, double offsetZ, double speed) {
        if (!start.getWorld().equals(end.getWorld()))
            throw new IllegalArgumentException("Locations must be in the same world");
        
        Vector direction = end.clone().subtract(start).toVector();
        double distance = direction.length();
        direction.normalize();
        
        for (int i = 0; i <= steps; i++) {
            double fraction = (double) i / steps;
            Location particleLocation = start.clone().add(direction.clone().multiply(distance * fraction));
            
            Color color = colorFunction.apply(fraction);
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);

            spawnParticle(particleLocation, Particle.DUST, 1, offsetX, offsetY, offsetZ, speed, dustOptions, false);
        }
    }
}
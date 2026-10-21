package org.Cry_Legende.sHGambleBar.effect;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.logging.Logger;

/**
 * Une couche de particules : un type de particule, une forme, et ses parametres d'emission.
 * Un effet complet est compose de plusieurs couches jouees en parallele.
 */
public class ParticleLayer {

    private final Particle particle;
    private final ParticleShape shape;
    private final int count;
    private final double radius;
    private final double height;
    private final double turns;
    private final double speed;
    private final Vector offset;
    private final int durationTicks;
    private final int intervalTicks;
    private final int delayTicks;
    private final double rotationPerTick;
    private final boolean followPlayer;
    private final boolean atEyeLevel;
    private final boolean visibleToOthers;

    // Donnees specifiques a certaines particules
    private final Color color;
    private final Color toColor;
    private final float size;
    private final Material blockMaterial;

    private ParticleLayer(Particle particle, ParticleShape shape, int count, double radius, double height,
                          double turns, double speed, Vector offset, int durationTicks, int intervalTicks,
                          int delayTicks, double rotationPerTick, boolean followPlayer, boolean atEyeLevel,
                          boolean visibleToOthers, Color color, Color toColor, float size, Material blockMaterial) {
        this.particle = particle;
        this.shape = shape;
        this.count = count;
        this.radius = radius;
        this.height = height;
        this.turns = turns;
        this.speed = speed;
        this.offset = offset;
        this.durationTicks = durationTicks;
        this.intervalTicks = intervalTicks;
        this.delayTicks = delayTicks;
        this.rotationPerTick = rotationPerTick;
        this.followPlayer = followPlayer;
        this.atEyeLevel = atEyeLevel;
        this.visibleToOthers = visibleToOthers;
        this.color = color;
        this.toColor = toColor;
        this.size = size;
        this.blockMaterial = blockMaterial;
    }

    public static ParticleLayer load(ConfigurationSection section, Logger logger) {
        String particleName = section.getString("type", "happy_villager");
        Particle particle = ParticleTypes.resolve(particleName);
        if (particle == null) {
            logger.warning("Particule inconnue '" + particleName + "' dans " + section.getCurrentPath()
                + ". Utilisation de happy_villager.");
            particle = ParticleTypes.resolve("happy_villager");
        }

        Material block = null;
        String blockName = section.getString("block-material");
        if (blockName != null) {
            block = Material.matchMaterial(blockName);
            if (block == null) {
                logger.warning("Materiau inconnu '" + blockName + "' dans " + section.getCurrentPath() + ".");
            }
        }

        return new ParticleLayer(
            particle,
            ParticleShape.parse(section.getString("shape"), ParticleShape.POINT),
            Math.max(1, section.getInt("count", 20)),
            section.getDouble("radius", 0.6),
            section.getDouble("height", 1.0),
            section.getDouble("turns", 2.0),
            section.getDouble("speed", 0.0),
            new Vector(section.getDouble("offset.x", 0.0),
                section.getDouble("offset.y", 0.0),
                section.getDouble("offset.z", 0.0)),
            Math.max(1, section.getInt("duration-ticks", 1)),
            Math.max(1, section.getInt("interval-ticks", 1)),
            Math.max(0, section.getInt("delay-ticks", 0)),
            section.getDouble("rotation-per-tick", 0.0),
            section.getBoolean("follow-player", true),
            section.getBoolean("at-eye-level", false),
            section.getBoolean("visible-to-others", true),
            parseColor(section.getString("color")),
            parseColor(section.getString("fade-color")),
            (float) section.getDouble("size", 1.0),
            block
        );
    }

    private static Color parseColor(String hex) {
        if (hex == null || hex.isEmpty()) return null;
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            int rgb = Integer.parseInt(clean, 16);
            return Color.fromRGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public int getDurationTicks() {
        return durationTicks;
    }

    public int getIntervalTicks() {
        return intervalTicks;
    }

    public int getDelayTicks() {
        return delayTicks;
    }

    public boolean shouldFollowPlayer() {
        return followPlayer;
    }

    /**
     * Emet une passe de particules.
     *
     * @param player  joueur cible (pour l'affichage prive)
     * @param anchor  position d'ancrage
     * @param elapsed ticks ecoules depuis le debut de la couche
     */
    public void emit(Player player, Location anchor, int elapsed) {
        World world = anchor.getWorld();
        if (world == null) return;

        Location base = anchor.clone().add(offset);
        if (atEyeLevel) {
            base.add(0, player.getEyeHeight(), 0);
        }

        double progress = durationTicks <= 1 ? 1.0 : Math.min(1.0, elapsed / (double) durationTicks);
        double angleOffset = Math.toRadians(rotationPerTick * elapsed);

        List<Vector> points = shape.build(count, radius, height, turns, angleOffset, progress);
        Object data = buildData();

        for (Vector point : points) {
            Location spawn = base.clone().add(point);
            if (visibleToOthers) {
                world.spawnParticle(particle, spawn, 1, 0, 0, 0, speed, data);
            } else {
                player.spawnParticle(particle, spawn, 1, 0, 0, 0, speed, data);
            }
        }
    }

    private Object buildData() {
        if (particle.getDataType() == Particle.DustOptions.class) {
            return new Particle.DustOptions(color != null ? color : Color.WHITE, size);
        }
        if (particle.getDataType() == Particle.DustTransition.class) {
            return new Particle.DustTransition(
                color != null ? color : Color.WHITE,
                toColor != null ? toColor : Color.BLACK,
                size);
        }
        if (particle.getDataType() == org.bukkit.block.data.BlockData.class) {
            Material material = blockMaterial != null && blockMaterial.isBlock() ? blockMaterial : Material.STONE;
            return material.createBlockData();
        }
        if (particle.getDataType() == ItemStack.class) {
            Material material = blockMaterial != null ? blockMaterial : Material.STONE;
            return new ItemStack(material);
        }
        return null;
    }
}

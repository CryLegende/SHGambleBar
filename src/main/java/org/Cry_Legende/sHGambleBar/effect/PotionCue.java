package org.Cry_Legende.sHGambleBar.effect;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Effet de potion applique au joueur (remplace les options drunkEffect /
 * blindnessEffect de l'ancien plugin, qui appliquaient CONFUSION et BLINDNESS
 * pendant N secondes lors d'une defaite).
 */
public class PotionCue {

    // Anciens noms Bukkit (avant 1.20.5) -> cle Minecraft
    private static final Map<String, String> LEGACY = Map.of(
        "slow", "slowness",
        "fast_digging", "haste",
        "slow_digging", "mining_fatigue",
        "increase_damage", "strength",
        "heal", "instant_health",
        "harm", "instant_damage",
        "jump", "jump_boost",
        "confusion", "nausea",
        "damage_resistance", "resistance");

    private final PotionEffectType type;
    private final int durationTicks;
    private final int amplifier;
    private final boolean ambient;
    private final boolean particles;

    private PotionCue(PotionEffectType type, int durationTicks, int amplifier,
                      boolean ambient, boolean particles) {
        this.type = type;
        this.durationTicks = durationTicks;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.particles = particles;
    }

    /**
     * @return null si le type de potion est inconnu.
     */
    public static PotionCue load(ConfigurationSection section, Logger logger) {
        String raw = section.getString("type", "");
        PotionEffectType type = resolve(raw);
        if (type == null) {
            logger.warning("Effet de potion inconnu : " + raw);
            return null;
        }
        return new PotionCue(
            type,
            Math.max(1, section.getInt("duration-ticks", 60)),
            Math.max(0, section.getInt("amplifier", 0)),
            section.getBoolean("ambient", false),
            section.getBoolean("particles", true)
        );
    }

    /**
     * Accepte la cle Minecraft (nausea, minecraft:nausea) et les anciens noms Bukkit (CONFUSION,
     * SLOW, ...). PotionEffectType.getByName (deprecie) ne lit plus que la cle : les anciens noms
     * documentes dans config.yml (CONFUSION de l'effet "lose") ne passaient que grace a la
     * reecriture de compatibilite du serveur.
     */
    private static PotionEffectType resolve(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String clean = raw.trim().toLowerCase(Locale.ROOT);
        if (clean.startsWith("minecraft:")) {
            clean = clean.substring("minecraft:".length());
        }
        clean = LEGACY.getOrDefault(clean, clean);
        NamespacedKey key = NamespacedKey.fromString(clean);
        return key == null ? null : Registry.MOB_EFFECT.get(key);
    }

    public void apply(Player player) {
        player.addPotionEffect(new PotionEffect(type, durationTicks, amplifier, ambient, particles));
    }
}

package org.Cry_Legende.sHGambleBar.effect;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * Effet de potion applique au joueur (remplace les options drunkEffect /
 * blindnessEffect de l'ancien plugin, qui appliquaient CONFUSION et BLINDNESS
 * pendant N secondes lors d'une defaite).
 */
public class PotionCue {

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
        PotionEffectType type = PotionEffectType.getByName(raw.toUpperCase(Locale.ROOT));
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

    public void apply(Player player) {
        player.addPotionEffect(new PotionEffect(type, durationTicks, amplifier, ambient, particles));
    }
}

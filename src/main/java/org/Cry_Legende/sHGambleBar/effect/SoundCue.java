package org.Cry_Legende.sHGambleBar.effect;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Un son a jouer, eventuellement repete avec une variation de pitch
 * (pour les montees en tension type roulette).
 */
public class SoundCue {

    private final String sound;
    private final float volume;
    private final float pitch;
    private final float pitchStep;
    private final int delayTicks;
    private final int repeat;
    private final int intervalTicks;
    private final boolean global;

    private SoundCue(String sound, float volume, float pitch, float pitchStep,
                     int delayTicks, int repeat, int intervalTicks, boolean global) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.pitchStep = pitchStep;
        this.delayTicks = delayTicks;
        this.repeat = repeat;
        this.intervalTicks = intervalTicks;
        this.global = global;
    }

    public static SoundCue load(ConfigurationSection section) {
        return new SoundCue(
            resolveKey(section.getString("sound", "ENTITY_PLAYER_LEVELUP")),
            (float) section.getDouble("volume", 1.0),
            (float) section.getDouble("pitch", 1.0),
            (float) section.getDouble("pitch-step", 0.0),
            Math.max(0, section.getInt("delay-ticks", 0)),
            Math.max(1, section.getInt("repeat", 1)),
            Math.max(1, section.getInt("interval-ticks", 4)),
            section.getBoolean("global", false)
        );
    }

    /**
     * Accepte le nom Bukkit (ENTITY_PLAYER_LEVELUP) ou la cle Minecraft (entity.player.levelup).
     * Partage par les actions [sound] et l'animation TITLE.
     * Sound.valueOf / Sound#getKey sont marques pour suppression : le nom est lu par reflexion
     * et la cle via le registre des sons.
     */
    public static String resolveKey(String raw) {
        String clean = raw.trim();
        if (clean.contains(".") || clean.contains(":")) {
            return clean.toLowerCase(Locale.ROOT);
        }
        try {
            Sound sound = (Sound) Sound.class.getField(clean.toUpperCase(Locale.ROOT)).get(null);
            NamespacedKey key = sound == null ? null : Registry.SOUND_EVENT.getKey(sound);
            if (key != null) return key.toString();
        } catch (ReflectiveOperationException | ClassCastException ignored) {
            // Nom inconnu : repli ci-dessous, comme avant.
        }
        return clean.toLowerCase(Locale.ROOT).replace('_', '.');
    }

    public int getDelayTicks() {
        return delayTicks;
    }

    public int getRepeat() {
        return repeat;
    }

    public int getIntervalTicks() {
        return intervalTicks;
    }

    /**
     * Joue la n-ieme repetition (index base 0).
     */
    public void play(Player player, int index) {
        float finalPitch = clampPitch(pitch + pitchStep * index);
        if (global) {
            Location location = player.getLocation();
            if (location.getWorld() != null) {
                location.getWorld().playSound(location, sound, volume, finalPitch);
            }
        } else {
            player.playSound(player.getLocation(), sound, volume, finalPitch);
        }
    }

    private static float clampPitch(float value) {
        return Math.max(0.5f, Math.min(2.0f, value));
    }
}

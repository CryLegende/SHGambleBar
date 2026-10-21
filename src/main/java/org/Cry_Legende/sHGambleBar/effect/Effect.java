package org.Cry_Legende.sHGambleBar.effect;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Un effet nomme : particules, sons et effets de potion.
 * Charge depuis la section "effects.<id>" de config.yml.
 */
public class Effect {

    private final String id;
    private final List<ParticleLayer> particles;
    private final List<SoundCue> sounds;
    private final List<PotionCue> potions;

    private Effect(String id, List<ParticleLayer> particles, List<SoundCue> sounds, List<PotionCue> potions) {
        this.id = id;
        this.particles = particles;
        this.sounds = sounds;
        this.potions = potions;
    }

    public static Effect load(String id, ConfigurationSection section, Logger logger) {
        List<ParticleLayer> particles = new ArrayList<>();
        ConfigurationSection particleSection = section.getConfigurationSection("particles");
        if (particleSection != null) {
            for (String key : particleSection.getKeys(false)) {
                ConfigurationSection layer = particleSection.getConfigurationSection(key);
                if (layer != null) {
                    particles.add(ParticleLayer.load(layer, logger));
                }
            }
        }

        List<SoundCue> sounds = new ArrayList<>();
        ConfigurationSection soundSection = section.getConfigurationSection("sounds");
        if (soundSection != null) {
            for (String key : soundSection.getKeys(false)) {
                ConfigurationSection cue = soundSection.getConfigurationSection(key);
                if (cue != null) {
                    sounds.add(SoundCue.load(cue));
                }
            }
        }

        List<PotionCue> potions = new ArrayList<>();
        ConfigurationSection potionSection = section.getConfigurationSection("potion-effects");
        if (potionSection != null) {
            for (String key : potionSection.getKeys(false)) {
                ConfigurationSection cue = potionSection.getConfigurationSection(key);
                if (cue == null) continue;
                PotionCue potion = PotionCue.load(cue, logger);
                if (potion != null) potions.add(potion);
            }
        }

        return new Effect(id, particles, sounds, potions);
    }

    public String getId() {
        return id;
    }

    public List<ParticleLayer> getParticles() {
        return particles;
    }

    public List<SoundCue> getSounds() {
        return sounds;
    }

    public List<PotionCue> getPotions() {
        return potions;
    }

    public boolean isEmpty() {
        return particles.isEmpty() && sounds.isEmpty() && potions.isEmpty();
    }
}

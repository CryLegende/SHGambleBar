package org.Cry_Legende.sHGambleBar.effect;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Resolution du nom d'une particule.
 * Accepte le nom Bukkit actuel (HAPPY_VILLAGER), l'ancien nom Bukkit (VILLAGER_HAPPY,
 * SMOKE_NORMAL, ...) et le nom Minecraft (happy_villager, minecraft:happy_villager) tel
 * qu'utilise dans la commande /particle.
 * Les anciens noms Bukkit ont disparu de l'enum Particle en 1.20.5 : ils sont traduits ici
 * vers le nom Minecraft au lieu de dependre de la reecriture de compatibilite du serveur.
 */
public final class ParticleTypes {

    private static final Map<String, String> LEGACY = new HashMap<>();

    static {
        legacy("VILLAGER_HAPPY", "happy_villager");
        legacy("VILLAGER_ANGRY", "angry_villager");
        legacy("REDSTONE", "dust");
        legacy("EXPLOSION_NORMAL", "poof");
        legacy("EXPLOSION_LARGE", "explosion");
        legacy("EXPLOSION_HUGE", "explosion_emitter");
        legacy("SMOKE_NORMAL", "smoke");
        legacy("SMOKE_LARGE", "large_smoke");
        legacy("ENCHANTMENT_TABLE", "enchant");
        legacy("CRIT_MAGIC", "enchanted_hit");
        legacy("FIREWORKS_SPARK", "firework");
        legacy("TOTEM", "totem_of_undying");
        legacy("SPELL", "effect");
        legacy("SPELL_INSTANT", "instant_effect");
        legacy("SPELL_MOB", "entity_effect");
        legacy("SPELL_MOB_AMBIENT", "entity_effect");
        legacy("SPELL_WITCH", "witch");
        legacy("WATER_SPLASH", "splash");
        legacy("WATER_BUBBLE", "bubble");
        legacy("WATER_WAKE", "fishing");
        legacy("SUSPENDED", "underwater");
        legacy("SUSPENDED_DEPTH", "underwater");
        legacy("DRIP_WATER", "dripping_water");
        legacy("DRIP_LAVA", "dripping_lava");
        legacy("SLIME", "item_slime");
        legacy("SNOWBALL", "item_snowball");
        legacy("SNOW_SHOVEL", "item_snowball");
        legacy("BLOCK_CRACK", "block");
        legacy("BLOCK_DUST", "block");
        legacy("ITEM_CRACK", "item");
        legacy("TOWN_AURA", "mycelium");
        legacy("WATER_DROP", "rain");
        legacy("MOB_APPEARANCE", "elder_guardian");
        // Nom Minecraft retire en 1.20.5 (fusionne avec entity_effect)
        legacy("AMBIENT_ENTITY_EFFECT", "entity_effect");
    }

    private ParticleTypes() {
    }

    private static void legacy(String bukkitName, String minecraftName) {
        LEGACY.put(bukkitName.toLowerCase(Locale.ROOT), minecraftName);
    }

    /**
     * @return la particule correspondante, ou null si le nom est inconnu.
     */
    public static Particle resolve(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        String clean = raw.trim().toLowerCase(Locale.ROOT);
        if (clean.startsWith("minecraft:")) {
            clean = clean.substring("minecraft:".length());
        }
        clean = LEGACY.getOrDefault(clean, clean);

        NamespacedKey key = NamespacedKey.fromString(clean);
        Particle particle = key == null ? null : Registry.PARTICLE_TYPE.get(key);
        if (particle != null) return particle;
        try {
            return Particle.valueOf(clean.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

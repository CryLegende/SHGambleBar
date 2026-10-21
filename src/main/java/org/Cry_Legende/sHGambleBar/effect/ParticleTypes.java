package org.Cry_Legende.sHGambleBar.effect;

import org.bukkit.Particle;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Resolution du nom d'une particule.
 * Accepte le nom Bukkit (VILLAGER_HAPPY), le nom Minecraft (happy_villager,
 * minecraft:happy_villager) tel qu'utilise dans la commande /particle.
 */
public final class ParticleTypes {

    private static final Map<String, Particle> ALIASES = new HashMap<>();

    static {
        alias("happy_villager", "VILLAGER_HAPPY");
        alias("angry_villager", "VILLAGER_ANGRY");
        alias("dust", "REDSTONE");
        alias("poof", "EXPLOSION_NORMAL");
        alias("explosion", "EXPLOSION_LARGE");
        alias("explosion_emitter", "EXPLOSION_HUGE");
        alias("smoke", "SMOKE_NORMAL");
        alias("large_smoke", "SMOKE_LARGE");
        alias("enchant", "ENCHANTMENT_TABLE");
        alias("enchanted_hit", "CRIT_MAGIC");
        alias("firework", "FIREWORKS_SPARK");
        alias("totem_of_undying", "TOTEM");
        alias("effect", "SPELL");
        alias("instant_effect", "SPELL_INSTANT");
        alias("entity_effect", "SPELL_MOB");
        alias("ambient_entity_effect", "SPELL_MOB_AMBIENT");
        alias("witch", "SPELL_WITCH");
        alias("splash", "WATER_SPLASH");
        alias("bubble", "WATER_BUBBLE");
        alias("bubble_pop", "BUBBLE_POP");
        alias("fishing", "WATER_WAKE");
        alias("underwater", "SUSPENDED");
        alias("dripping_water", "DRIP_WATER");
        alias("dripping_lava", "DRIP_LAVA");
        alias("item_slime", "SLIME");
        alias("item_snowball", "SNOWBALL");
        alias("block", "BLOCK_CRACK");
        alias("block_marker", "BLOCK_MARKER");
        alias("item", "ITEM_CRACK");
        alias("mycelium", "TOWN_AURA");
        alias("note", "NOTE");
        alias("portal", "PORTAL");
        alias("rain", "WATER_DROP");
        alias("elder_guardian", "MOB_APPEARANCE");
        alias("dragon_breath", "DRAGON_BREATH");
        alias("end_rod", "END_ROD");
        alias("damage_indicator", "DAMAGE_INDICATOR");
        alias("sweep_attack", "SWEEP_ATTACK");
        alias("falling_dust", "FALLING_DUST");
        alias("spit", "SPIT");
        alias("squid_ink", "SQUID_INK");
        alias("crit", "CRIT");
        alias("heart", "HEART");
        alias("flame", "FLAME");
        alias("soul", "SOUL");
        alias("soul_fire_flame", "SOUL_FIRE_FLAME");
        alias("glow", "GLOW");
        alias("wax_on", "WAX_ON");
        alias("wax_off", "WAX_OFF");
        alias("electric_spark", "ELECTRIC_SPARK");
        alias("scrape", "SCRAPE");
        alias("cherry_leaves", "CHERRY_LEAVES");
        alias("sculk_soul", "SCULK_SOUL");
        alias("sculk_charge_pop", "SCULK_CHARGE_POP");
        alias("dust_color_transition", "DUST_COLOR_TRANSITION");
    }

    private ParticleTypes() {
    }

    private static void alias(String minecraftName, String bukkitName) {
        try {
            ALIASES.put(minecraftName, Particle.valueOf(bukkitName));
        } catch (IllegalArgumentException ignored) {
            // La particule n'existe pas sur cette version : l'alias est simplement ignore.
        }
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
        Particle alias = ALIASES.get(clean);
        if (alias != null) return alias;
        try {
            return Particle.valueOf(clean.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

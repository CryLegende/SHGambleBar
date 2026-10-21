package org.Cry_Legende.sHGambleBar.effect;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Charge les effets nommes de config.yml et se charge de les jouer.
 * Remplace les anciens "execute at %player% run particle ..." des rewardCommands.
 */
public class EffectManager {

    private final SHGambleBar plugin;
    private final Map<String, Effect> effects = new HashMap<>();

    public EffectManager(SHGambleBar plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        effects.clear();
        ConfigurationSection root = plugin.getConfigManager().section("effects");
        if (root == null) return;

        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) continue;
            Effect effect = Effect.load(id, section, plugin.getLogger());
            effects.put(id.toLowerCase(Locale.ROOT), effect);
        }

        if (plugin.getConfigManager().isDebug()) {
            plugin.getLogger().info("Effets charges : " + effects.keySet());
        }
    }

    public Effect get(String id) {
        if (id == null || id.isEmpty()) return null;
        return effects.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean exists(String id) {
        return get(id) != null;
    }

    public java.util.Set<String> getIds() {
        return effects.keySet();
    }

    /**
     * Joue l'effet nomme a la position du joueur.
     */
    public void play(Player player, String effectId) {
        play(player, effectId, player.getLocation());
    }

    public void play(Player player, String effectId, Location anchor) {
        Effect effect = get(effectId);
        if (effect == null) {
            if (effectId != null && !effectId.isEmpty() && plugin.getConfigManager().isDebug()) {
                plugin.getLogger().warning("Effet inconnu demande : " + effectId);
            }
            return;
        }
        play(player, effect, anchor);
    }

    public void play(Player player, Effect effect, Location anchor) {
        if (effect == null || effect.isEmpty()) return;

        for (ParticleLayer layer : effect.getParticles()) {
            runLayer(player, layer, anchor);
        }
        for (SoundCue cue : effect.getSounds()) {
            runSound(player, cue);
        }
        for (PotionCue potion : effect.getPotions()) {
            potion.apply(player);
        }
    }

    private void runLayer(Player player, ParticleLayer layer, Location anchor) {
        Location fixedAnchor = anchor.clone();

        new BukkitRunnable() {
            int elapsed = 0;

            @Override
            public void run() {
                if (!player.isOnline() || elapsed > layer.getDurationTicks()) {
                    cancel();
                    return;
                }
                Location target = layer.shouldFollowPlayer() ? player.getLocation() : fixedAnchor;
                layer.emit(player, target, elapsed);
                elapsed += layer.getIntervalTicks();
            }
        }.runTaskTimer(plugin, layer.getDelayTicks(), layer.getIntervalTicks());
    }

    private void runSound(Player player, SoundCue cue) {
        if (cue.getRepeat() == 1) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) cue.play(player, 0);
            }, cue.getDelayTicks());
            return;
        }

        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (!player.isOnline() || index >= cue.getRepeat()) {
                    cancel();
                    return;
                }
                cue.play(player, index);
                index++;
            }
        }.runTaskTimer(plugin, cue.getDelayTicks(), cue.getIntervalTicks());
    }
}

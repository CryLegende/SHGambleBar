package org.Cry_Legende.sHGambleBar.animation;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.Cry_Legende.sHGambleBar.bar.Drink;
import org.Cry_Legende.sHGambleBar.game.GambleResult;
import org.Cry_Legende.sHGambleBar.util.ColorUtils;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Locale;

/**
 * Suspense via title/subtitle : une suite d'images defile avec un son
 * dont le pitch monte progressivement, puis le resultat est revele.
 */
public class TitleAnimation implements DrinkAnimation {

    private final SHGambleBar plugin;

    public TitleAnimation(SHGambleBar plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "TITLE";
    }

    @Override
    public void play(Player player, Drink drink, GambleResult result, Runnable onReveal) {
        ConfigurationSection section = plugin.getConfigManager().section("animation.title");
        if (section == null) {
            onReveal.run();
            return;
        }

        List<String> frames = section.getStringList("frames");
        if (frames.isEmpty()) {
            onReveal.run();
            return;
        }

        int interval = Math.max(1, section.getInt("frame-interval-ticks", 4));
        int duration = Math.max(interval, section.getInt("duration-ticks", 60));
        int totalFrames = duration / interval;

        String titleTemplate = section.getString("title", "");
        String subtitleTemplate = section.getString("subtitle", "%frame%");
        String drinkName = ColorUtils.colorize(drink.getName());

        String soundName = section.getString("tick-sound.sound", "BLOCK_NOTE_BLOCK_HAT");
        float volume = (float) section.getDouble("tick-sound.volume", 0.7D);
        float basePitch = (float) section.getDouble("tick-sound.pitch", 0.8D);
        float pitchStep = (float) section.getDouble("tick-sound.pitch-step", 0.02D);
        boolean soundEnabled = section.getBoolean("tick-sound.enabled", true);
        String soundKey = resolveSound(soundName);

        boolean freeze = section.getBoolean("freeze-player", false);
        if (freeze) {
            plugin.getGambleService().freeze(player, duration);
        }

        new BukkitRunnable() {
            int frame = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                if (frame >= totalFrames) {
                    cancel();
                    player.resetTitle();
                    onReveal.run();
                    return;
                }

                String frameText = frames.get(frame % frames.size());
                player.sendTitle(
                    ColorUtils.colorize(apply(titleTemplate, frameText, drinkName)),
                    ColorUtils.colorize(apply(subtitleTemplate, frameText, drinkName)),
                    0, interval + 5, 0);

                if (soundEnabled) {
                    float pitch = Math.max(0.5f, Math.min(2.0f, basePitch + pitchStep * frame));
                    player.playSound(player.getLocation(), soundKey, volume, pitch);
                }
                frame++;
            }
        }.runTaskTimer(plugin, 0L, interval);
    }

    private String apply(String template, String frame, String drinkName) {
        return template.replace("%frame%", frame).replace("%drink%", drinkName);
    }

    private String resolveSound(String raw) {
        String clean = raw.trim();
        if (clean.contains(".") || clean.contains(":")) {
            return clean.toLowerCase(Locale.ROOT);
        }
        try {
            return Sound.valueOf(clean.toUpperCase(Locale.ROOT)).getKey().toString();
        } catch (IllegalArgumentException e) {
            return clean.toLowerCase(Locale.ROOT).replace('_', '.');
        }
    }
}

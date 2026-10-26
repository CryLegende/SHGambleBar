package org.Cry_Legende.sHGambleBar.animation;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.Cry_Legende.sHGambleBar.bar.Drink;
import org.Cry_Legende.sHGambleBar.effect.SoundCue;
import org.Cry_Legende.sHGambleBar.game.GambleResult;
import org.Cry_Legende.sHGambleBar.util.ColorUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Suspense via title/subtitle : une suite d'images defile avec un son
 * dont le pitch monte progressivement, puis le resultat est revele.
 */
public class TitleAnimation implements DrinkAnimation {

    private final SHGambleBar plugin;
    private final Map<UUID, Session> sessions = new HashMap<>();

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
        String soundKey = SoundCue.resolveKey(soundName);

        boolean freeze = section.getBoolean("freeze-player", false);
        if (freeze) {
            plugin.getGambleService().freeze(player, duration);
        }

        // Une seule animation a la fois par joueur : la precedente est soldee.
        Session running = sessions.get(player.getUniqueId());
        if (running != null) {
            running.finish();
        }
        Session session = new Session(player, onReveal);
        sessions.put(player.getUniqueId(), session);

        session.task = new BukkitRunnable() {
            int frame = 0;

            @Override
            public void run() {
                if (session.done) {
                    cancel();
                    return;
                }
                if (!player.isOnline()) {
                    session.finish();
                    return;
                }
                if (frame >= totalFrames) {
                    player.resetTitle();
                    session.finish();
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

    /**
     * Deconnexion : le resultat est applique tout de suite (le joueur a deja paye).
     * Avant, la tache s'arretait sans reveler et un gain etait perdu.
     */
    @Override
    public void cancel(Player player) {
        Session session = sessions.get(player.getUniqueId());
        if (session != null) session.finish();
    }

    /**
     * Arret du plugin : les taches sont annulees par le serveur, on solde donc les animations en cours.
     */
    public void finishAll() {
        for (Session session : new ArrayList<>(sessions.values())) {
            try {
                session.finish();
            } catch (RuntimeException e) {
                plugin.getLogger().log(Level.SEVERE, "Solde de l'animation de " + session.player.getName() + " echoue.", e);
            }
        }
    }

    private String apply(String template, String frame, String drinkName) {
        return template.replace("%frame%", frame).replace("%drink%", drinkName);
    }

    /**
     * Une animation en cours. La revelation est appliquee exactement une fois.
     */
    private class Session {

        private final Player player;
        private final Runnable onReveal;
        private boolean done;
        private BukkitTask task;

        Session(Player player, Runnable onReveal) {
            this.player = player;
            this.onReveal = onReveal;
        }

        void finish() {
            if (done) return;
            done = true;
            if (task != null) task.cancel();
            sessions.remove(player.getUniqueId(), this);
            onReveal.run();
        }
    }
}

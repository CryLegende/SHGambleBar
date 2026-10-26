package org.Cry_Legende.sHGambleBar.animation;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Registre des animations disponibles.
 */
public class AnimationManager {

    private final SHGambleBar plugin;
    private final Map<String, DrinkAnimation> animations = new LinkedHashMap<>();
    private final DrinkAnimation none = new NoneAnimation();
    private final RouletteAnimation roulette;
    private final TitleAnimation title;

    public AnimationManager(SHGambleBar plugin) {
        this.plugin = plugin;
        this.roulette = new RouletteAnimation(plugin);
        this.title = new TitleAnimation(plugin);

        register(none);
        register(title);
        register(roulette);
    }

    private void register(DrinkAnimation animation) {
        animations.put(animation.getId().toUpperCase(Locale.ROOT), animation);
    }

    public RouletteAnimation getRoulette() {
        return roulette;
    }

    public Set<String> getIds() {
        return animations.keySet();
    }

    /**
     * @return l'animation demandee, ou NONE si l'identifiant est inconnu.
     */
    public DrinkAnimation get(String id) {
        if (id == null || id.isEmpty()) return none;
        DrinkAnimation animation = animations.get(id.toUpperCase(Locale.ROOT));
        if (animation == null) {
            if (plugin.getConfigManager().isDebug()) {
                plugin.getLogger().warning("Animation inconnue : " + id + " (types : " + getIds() + ")");
            }
            return none;
        }
        if (!plugin.getConfigManager().getBoolean("animation.enabled", true)) {
            return none;
        }
        return animation;
    }

    /**
     * Nettoyage a la deconnexion : le resultat en attente est applique.
     */
    public void handleQuit(Player player) {
        for (DrinkAnimation animation : animations.values()) {
            animation.cancel(player);
        }
    }

    public void shutdown() {
        roulette.finishAll();
        title.finishAll();
    }
}

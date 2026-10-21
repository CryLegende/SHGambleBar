package org.Cry_Legende.sHGambleBar.animation;

import org.Cry_Legende.sHGambleBar.bar.Drink;
import org.Cry_Legende.sHGambleBar.game.GambleResult;
import org.bukkit.entity.Player;

/**
 * Animation de suspense jouee entre la consommation et la revelation du resultat.
 * Le resultat est deja tire au sort : l'animation ne fait que le mettre en scene.
 */
public interface DrinkAnimation {

    String getId();

    /**
     * @param onReveal a executer une fois l'animation terminee, pour appliquer le resultat.
     *                 Doit toujours etre appelee exactement une fois.
     */
    void play(Player player, Drink drink, GambleResult result, Runnable onReveal);

    /**
     * Nettoyage si le joueur se deconnecte pendant l'animation.
     */
    default void cancel(Player player) {
    }
}

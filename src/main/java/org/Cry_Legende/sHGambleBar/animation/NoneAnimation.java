package org.Cry_Legende.sHGambleBar.animation;

import org.Cry_Legende.sHGambleBar.bar.Drink;
import org.Cry_Legende.sHGambleBar.game.GambleResult;
import org.bukkit.entity.Player;

/**
 * Aucune animation : le resultat est revele immediatement (comportement GambleBarPro).
 */
public class NoneAnimation implements DrinkAnimation {

    @Override
    public String getId() {
        return "NONE";
    }

    @Override
    public void play(Player player, Drink drink, GambleResult result, Runnable onReveal) {
        onReveal.run();
    }
}

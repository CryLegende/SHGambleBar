package org.Cry_Legende.sHGambleBar.action;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;

/**
 * Execute une liste d'actions dans l'ordre. Les actions [delay] decalent
 * toutes les actions suivantes.
 */
public final class ActionRunner {

    private ActionRunner() {
    }

    public static void run(Plugin plugin, Player player, List<Action> actions,
                           Map<String, String> placeholders) {
        if (actions == null || actions.isEmpty()) return;

        int delay = 0;
        for (Action action : actions) {
            if (action.getType() == Action.Type.DELAY) {
                delay += action.getDelayTicks();
                continue;
            }
            // Plugin en cours d'arret (resultat solde au onDisable) : runTaskLater leverait une
            // exception, l'action est donc executee tout de suite plutot que perdue.
            if (delay <= 0 || !plugin.isEnabled()) {
                dispatch(player, action, placeholders);
            } else {
                Bukkit.getScheduler().runTaskLater(plugin,
                    () -> dispatch(player, action, placeholders), delay);
            }
        }
    }

    private static void dispatch(Player player, Action action, Map<String, String> placeholders) {
        if (action.getType().requiresPlayer() && !player.isOnline()) return;
        action.execute(player, placeholders);
    }
}

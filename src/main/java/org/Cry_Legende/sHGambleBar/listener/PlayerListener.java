package org.Cry_Legende.sHGambleBar.listener;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Chargement/dechargement des statistiques et immobilisation pendant l'animation.
 */
public class PlayerListener implements Listener {

    private final SHGambleBar plugin;

    public PlayerListener(SHGambleBar plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getStatsManager().loadAsync(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getAnimationManager().handleQuit(player);
        plugin.getGambleService().handleQuit(player);
        plugin.getStatsManager().unloadAsync(player.getUniqueId());
    }

    /**
     * Seuls les deplacements sont bloques : la rotation de la tete reste libre.
     */
    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!plugin.getGambleService().isFrozen(event.getPlayer())) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;
        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) return;

        Location blocked = from.clone();
        blocked.setYaw(to.getYaw());
        blocked.setPitch(to.getPitch());
        event.setTo(blocked);
    }
}

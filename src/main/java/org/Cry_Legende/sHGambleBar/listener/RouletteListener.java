package org.Cry_Legende.sHGambleBar.listener;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.Cry_Legende.sHGambleBar.animation.RouletteHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/**
 * Protege le GUI de la roulette : aucun clic possible, et si le joueur ferme
 * l'inventaire avant la fin, le resultat est applique immediatement.
 * getHolder(false) : ces handlers voient tous les inventaires du serveur, sans
 * instantane de bloc a chaque clic dans un coffre.
 */
public class RouletteListener implements Listener {

    private final SHGambleBar plugin;

    public RouletteListener(SHGambleBar plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder(false) instanceof RouletteHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder(false) instanceof RouletteHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof RouletteHolder)) return;
        if (!(event.getPlayer() instanceof Player player)) return;
        plugin.getAnimationManager().getRoulette().handleClose(player);
    }
}

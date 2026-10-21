package org.Cry_Legende.sHGambleBar.listener;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.Cry_Legende.sHGambleBar.bar.Bar;
import org.Cry_Legende.sHGambleBar.bar.BarHolder;
import org.Cry_Legende.sHGambleBar.bar.Drink;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/**
 * Achat d'une fiole depuis l'inventaire d'un bar.
 */
public class BarListener implements Listener {

    private final SHGambleBar plugin;

    public BarListener(SHGambleBar plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BarHolder holder)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null
            || !event.getClickedInventory().equals(event.getView().getTopInventory())) {
            return;
        }

        Bar bar = plugin.getBarManager().getBar(holder.getBarId());
        if (bar == null) return;

        Drink drink = bar.getDrinkAtSlot(event.getSlot());
        if (drink == null) return;

        plugin.getGambleService().purchase(player, drink);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof BarHolder) {
            event.setCancelled(true);
        }
    }
}

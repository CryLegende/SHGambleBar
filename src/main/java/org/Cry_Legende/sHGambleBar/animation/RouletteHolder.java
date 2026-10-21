package org.Cry_Legende.sHGambleBar.animation;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Holder de la roulette : permet au listener de bloquer tous les clics
 * et de savoir si une fermeture est provoquee par l'animation elle-meme.
 */
public class RouletteHolder implements InventoryHolder {

    private Inventory inventory;
    private boolean closedByPlugin;

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public boolean isClosedByPlugin() {
        return closedByPlugin;
    }

    public void setClosedByPlugin(boolean closedByPlugin) {
        this.closedByPlugin = closedByPlugin;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

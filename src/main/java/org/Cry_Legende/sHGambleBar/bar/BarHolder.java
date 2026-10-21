package org.Cry_Legende.sHGambleBar.bar;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Holder permettant d'identifier de facon fiable un inventaire de bar,
 * sans comparer des titres ou des instances partagees.
 */
public class BarHolder implements InventoryHolder {

    private final String barId;
    private Inventory inventory;

    public BarHolder(String barId) {
        this.barId = barId;
    }

    public String getBarId() {
        return barId;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

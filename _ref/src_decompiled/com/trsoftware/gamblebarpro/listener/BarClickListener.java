/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.entity.Player
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.inventory.InventoryClickEvent
 *  org.bukkit.event.inventory.InventoryType
 *  org.bukkit.inventory.ItemStack
 */
package com.trsoftware.gamblebarpro.listener;

import com.trsoftware.gamblebarpro.GambleBar;
import com.trsoftware.gamblebarpro.bar.Bar;
import com.trsoftware.gamblebarpro.bar.EconomyManager;
import java.util.regex.Matcher;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

public class BarClickListener
implements Listener {
    public GambleBar plugin;

    public BarClickListener(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    @EventHandler
    public void onBarClick(InventoryClickEvent inventoryClickEvent) {
        int n;
        if (this.plugin.isVersion18()) {
            if (inventoryClickEvent.getCurrentItem() == null) {
                return;
            }
            for (n = 0; n < this.plugin.bm.bars.size(); ++n) {
                if (!inventoryClickEvent.getInventory().equals(this.plugin.bm.barInv.get(n))) continue;
                if (!inventoryClickEvent.getClickedInventory().getType().equals((Object)InventoryType.CHEST)) {
                    inventoryClickEvent.setCancelled(true);
                    return;
                }
                for (int i = 0; i < this.plugin.bm.drinks.size(); ++i) {
                    if (!this.plugin.bm.drinks.get(i).getDrinkItem().getType().equals((Object)inventoryClickEvent.getCurrentItem().getType()) || this.plugin.bm.drinks.get(i).getDrinkSlot() != inventoryClickEvent.getSlot() || this.plugin.bm.drinks.get(i).getDrinkColor() != inventoryClickEvent.getCurrentItem().getDurability()) continue;
                    inventoryClickEvent.setCancelled(true);
                    String string = String.format("%,d", this.plugin.bm.drinks.get(i).getDrinkPrice());
                    if (this.canPlayerAfford((Player)inventoryClickEvent.getWhoClicked(), this.plugin.bm.drinks.get(i).getDrinkPrice(), this.plugin.bm.getBarById(this.plugin.bm.drinks.get(i).getDrinkBar()))) {
                        if (inventoryClickEvent.getWhoClicked().getInventory().firstEmpty() == -1) {
                            this.plugin.sendMessage((Player)inventoryClickEvent.getWhoClicked(), this.plugin.pmessages.getString("fullInventory"));
                            inventoryClickEvent.getWhoClicked().closeInventory();
                            return;
                        }
                        if (this.plugin.bm.getBarById(this.plugin.bm.drinks.get(i).getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.VAULT)) {
                            GambleBar.economy.withdrawPlayer((OfflinePlayer)((Player)inventoryClickEvent.getWhoClicked()), (double)this.plugin.bm.drinks.get(i).getDrinkPrice());
                            this.plugin.sendMessage((Player)inventoryClickEvent.getWhoClicked(), this.plugin.pmessages.getString("boughtDrink").replaceAll("%drink%", Matcher.quoteReplacement(inventoryClickEvent.getCurrentItem().getItemMeta().getDisplayName())).replaceAll("%price%", string));
                        } else if (this.plugin.bm.getBarById(this.plugin.bm.drinks.get(i).getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.PLAYERPOINTS)) {
                            this.plugin.ppAPI.take(inventoryClickEvent.getWhoClicked().getUniqueId(), (int)this.plugin.bm.drinks.get(i).getDrinkPrice());
                            this.plugin.sendMessage((Player)inventoryClickEvent.getWhoClicked(), this.plugin.pmessages.getString("boughtDrinkPP").replaceAll("%drink%", Matcher.quoteReplacement(inventoryClickEvent.getCurrentItem().getItemMeta().getDisplayName())).replaceAll("%price%", string));
                        }
                        inventoryClickEvent.getWhoClicked().getInventory().addItem(new ItemStack[]{inventoryClickEvent.getCurrentItem()});
                        inventoryClickEvent.getWhoClicked().closeInventory();
                    } else if (this.plugin.bm.getBarById(this.plugin.bm.drinks.get(i).getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.VAULT)) {
                        this.plugin.sendMessage((Player)inventoryClickEvent.getWhoClicked(), this.plugin.pmessages.getString("insufficientFunds").replaceAll("%drink%", Matcher.quoteReplacement(inventoryClickEvent.getCurrentItem().getItemMeta().getDisplayName())).replaceAll("%price%", string));
                    } else if (this.plugin.bm.getBarById(this.plugin.bm.drinks.get(i).getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.PLAYERPOINTS)) {
                        this.plugin.sendMessage((Player)inventoryClickEvent.getWhoClicked(), this.plugin.pmessages.getString("insufficientFundsPP").replaceAll("%drink%", Matcher.quoteReplacement(inventoryClickEvent.getCurrentItem().getItemMeta().getDisplayName())).replaceAll("%price%", string));
                    }
                    return;
                }
                inventoryClickEvent.setCancelled(true);
                return;
            }
        }
        if (inventoryClickEvent.getCurrentItem() == null) {
            return;
        }
        for (n = 0; n < this.plugin.bm.bars.size(); ++n) {
            if (!inventoryClickEvent.getInventory().equals(this.plugin.bm.barInv.get(n))) continue;
            if (!inventoryClickEvent.getClickedInventory().getType().equals((Object)InventoryType.CHEST)) {
                inventoryClickEvent.setCancelled(true);
                return;
            }
            for (int i = 0; i < this.plugin.bm.drinks.size(); ++i) {
                if (!this.plugin.bm.drinks.get(i).getDrinkItem().getType().equals((Object)inventoryClickEvent.getCurrentItem().getType()) || this.plugin.bm.drinks.get(i).getDrinkSlot() != inventoryClickEvent.getSlot() || !this.plugin.bm.drinks.get(i).getDrinkMeta().getDisplayName().equals(inventoryClickEvent.getCurrentItem().getItemMeta().getDisplayName()) || !this.plugin.bm.drinks.get(i).getDrinkMeta().getLore().equals(inventoryClickEvent.getCurrentItem().getItemMeta().getLore())) continue;
                inventoryClickEvent.setCancelled(true);
                String string = String.format("%,d", this.plugin.bm.drinks.get(i).getDrinkPrice());
                if (this.canPlayerAfford((Player)inventoryClickEvent.getWhoClicked(), this.plugin.bm.drinks.get(i).getDrinkPrice(), this.plugin.bm.getBarById(this.plugin.bm.drinks.get(i).getDrinkBar()))) {
                    if (inventoryClickEvent.getWhoClicked().getInventory().firstEmpty() == -1) {
                        this.plugin.sendMessage((Player)inventoryClickEvent.getWhoClicked(), this.plugin.pmessages.getString("fullInventory"));
                        if (!this.plugin.getConfig().contains("autoCloseBar") || this.plugin.getConfig().getBoolean("autoCloseBar")) {
                            inventoryClickEvent.getWhoClicked().closeInventory();
                        }
                        return;
                    }
                    if (this.plugin.bm.getBarById(this.plugin.bm.drinks.get(i).getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.VAULT)) {
                        GambleBar.economy.withdrawPlayer((OfflinePlayer)((Player)inventoryClickEvent.getWhoClicked()), (double)this.plugin.bm.drinks.get(i).getDrinkPrice());
                        this.plugin.sendMessage((Player)inventoryClickEvent.getWhoClicked(), this.plugin.pmessages.getString("boughtDrink").replaceAll("%drink%", Matcher.quoteReplacement(inventoryClickEvent.getCurrentItem().getItemMeta().getDisplayName())).replaceAll("%price%", string));
                    } else if (this.plugin.bm.getBarById(this.plugin.bm.drinks.get(i).getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.PLAYERPOINTS)) {
                        this.plugin.ppAPI.take(inventoryClickEvent.getWhoClicked().getUniqueId(), (int)this.plugin.bm.drinks.get(i).getDrinkPrice());
                        this.plugin.sendMessage((Player)inventoryClickEvent.getWhoClicked(), this.plugin.pmessages.getString("boughtDrinkPP").replaceAll("%drink%", Matcher.quoteReplacement(inventoryClickEvent.getCurrentItem().getItemMeta().getDisplayName())).replaceAll("%price%", string));
                    }
                    inventoryClickEvent.getWhoClicked().getInventory().addItem(new ItemStack[]{inventoryClickEvent.getCurrentItem()});
                    if (!this.plugin.getConfig().contains("autoCloseBar") || this.plugin.getConfig().getBoolean("autoCloseBar")) {
                        inventoryClickEvent.getWhoClicked().closeInventory();
                    }
                } else if (this.plugin.bm.getBarById(this.plugin.bm.drinks.get(i).getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.VAULT)) {
                    this.plugin.sendMessage((Player)inventoryClickEvent.getWhoClicked(), this.plugin.pmessages.getString("insufficientFunds").replaceAll("%drink%", Matcher.quoteReplacement(inventoryClickEvent.getCurrentItem().getItemMeta().getDisplayName())).replaceAll("%price%", string));
                } else if (this.plugin.bm.getBarById(this.plugin.bm.drinks.get(i).getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.PLAYERPOINTS)) {
                    this.plugin.sendMessage((Player)inventoryClickEvent.getWhoClicked(), this.plugin.pmessages.getString("insufficientFundsPP").replaceAll("%drink%", Matcher.quoteReplacement(inventoryClickEvent.getCurrentItem().getItemMeta().getDisplayName())).replaceAll("%price%", string));
                }
                return;
            }
            inventoryClickEvent.setCancelled(true);
            return;
        }
    }

    private boolean canPlayerAfford(Player player, long l, Bar bar) {
        if (bar.getEconomyType() == null || bar.getEconomyType().equals((Object)EconomyManager.EconomyType.VAULT)) {
            return GambleBar.economy.getBalance((OfflinePlayer)player) >= (double)l;
        }
        if (bar.getEconomyType().equals((Object)EconomyManager.EconomyType.PLAYERPOINTS)) {
            return this.plugin.ppAPI.look(player.getUniqueId()) >= (int)l;
        }
        return false;
    }
}


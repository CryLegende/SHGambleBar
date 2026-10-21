/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerItemConsumeEvent
 *  org.bukkit.inventory.ItemStack
 */
package com.trsoftware.gamblebarpro.listener;

import com.trsoftware.gamblebarpro.GambleBar;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class DrinkConsumeListener
implements Listener {
    public GambleBar plugin;

    public DrinkConsumeListener(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    @EventHandler
    public void onDrink(PlayerItemConsumeEvent playerItemConsumeEvent) {
        if (playerItemConsumeEvent.getItem().getType().equals((Object)Material.AIR)) {
            return;
        }
        if (this.plugin.isVersion18()) {
            for (int i = 0; i < this.plugin.bm.drinks.size(); ++i) {
                if (!this.plugin.bm.drinks.get(i).getDrinkItem().getType().equals((Object)playerItemConsumeEvent.getItem().getType()) || playerItemConsumeEvent.getItem().getDurability() != this.plugin.bm.drinks.get(i).getDrinkColor() || !this.plugin.bm.drinks.get(i).getDrinkItem().getItemMeta().getLore().equals(playerItemConsumeEvent.getItem().getItemMeta().getLore())) continue;
                if (this.plugin.dm.isWorldBlacklisted(playerItemConsumeEvent.getPlayer())) {
                    return;
                }
                if (this.plugin.dm.isOnCoolDown(playerItemConsumeEvent.getPlayer(), i)) {
                    return;
                }
                playerItemConsumeEvent.getPlayer().getInventory().removeItem(new ItemStack[]{playerItemConsumeEvent.getItem()});
                playerItemConsumeEvent.setCancelled(true);
                this.plugin.dm.playerDrink(playerItemConsumeEvent.getPlayer(), this.plugin.bm.drinks.get(i));
                return;
            }
        } else {
            for (int i = 0; i < this.plugin.bm.drinks.size(); ++i) {
                if (!this.plugin.bm.drinks.get(i).getDrinkItem().equals((Object)playerItemConsumeEvent.getItem())) continue;
                if (playerItemConsumeEvent.getPlayer().getInventory().getItemInOffHand().equals((Object)playerItemConsumeEvent.getItem()) && !playerItemConsumeEvent.getPlayer().getInventory().getItemInMainHand().equals((Object)playerItemConsumeEvent.getItem())) {
                    if (this.plugin.pmessages.isSet("noOffHand")) {
                        this.plugin.sendMessage(playerItemConsumeEvent.getPlayer(), this.plugin.pmessages.getString("noOffHand"));
                    }
                    playerItemConsumeEvent.setCancelled(true);
                    return;
                }
                if (this.plugin.dm.isWorldBlacklisted(playerItemConsumeEvent.getPlayer())) {
                    return;
                }
                if (this.plugin.dm.isOnCoolDown(playerItemConsumeEvent.getPlayer(), i)) {
                    return;
                }
                playerItemConsumeEvent.getPlayer().getInventory().removeItem(new ItemStack[]{playerItemConsumeEvent.getItem()});
                playerItemConsumeEvent.setCancelled(true);
                this.plugin.dm.playerDrink(playerItemConsumeEvent.getPlayer(), this.plugin.bm.drinks.get(i));
                return;
            }
        }
    }
}


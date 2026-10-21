/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Material
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Villager
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerInteractEntityEvent
 *  org.bukkit.inventory.EquipmentSlot
 */
package com.trsoftware.gamblebarpro.listener;

import com.trsoftware.gamblebarpro.ColorUtils;
import com.trsoftware.gamblebarpro.GambleBar;
import java.io.IOException;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class GamblerClickListener
implements Listener {
    public GambleBar plugin;

    public GamblerClickListener(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    @EventHandler
    public void onGamblerClick(PlayerInteractEntityEvent playerInteractEntityEvent) {
        if (!this.plugin.isVersion18()) {
            if (playerInteractEntityEvent.getHand().equals((Object)EquipmentSlot.HAND)) {
                Villager villager;
                Player player = playerInteractEntityEvent.getPlayer();
                if (playerInteractEntityEvent.getRightClicked() instanceof Villager && (villager = (Villager)playerInteractEntityEvent.getRightClicked()).getScoreboardTags().toString().contains("gb:")) {
                    if (player.hasPermission("gamblebar.admin.remove") && player.getInventory().getItemInMainHand().getType().equals((Object)Material.GOLDEN_CARROT)) {
                        playerInteractEntityEvent.setCancelled(true);
                        playerInteractEntityEvent.getRightClicked().remove();
                        this.plugin.sendMessage(player, this.plugin.pmessages.getString("gamblerDeleted"));
                        return;
                    }
                    playerInteractEntityEvent.setCancelled(true);
                    String string = villager.getScoreboardTags().toString();
                    int n = string.length();
                    int n2 = n - 1;
                    this.plugin.obc.openBar(player, String.valueOf(villager.getScoreboardTags().toString().subSequence(4, n2)));
                }
            }
        } else {
            Player player = playerInteractEntityEvent.getPlayer();
            if (playerInteractEntityEvent.getRightClicked() instanceof Villager) {
                Villager villager = (Villager)playerInteractEntityEvent.getRightClicked();
                if (!villager.isCustomNameVisible()) {
                    return;
                }
                for (int i = 0; i < this.plugin.gn.npcList.size(); ++i) {
                    String[] stringArray = this.plugin.gn.npcList.get(i).split(":");
                    if (!villager.getCustomName().equals(ColorUtils.translateColorCodes(stringArray[0].replaceAll("_", " ")))) continue;
                    String string = stringArray[1];
                    if (player.hasPermission("gamblebar.admin.remove") && player.getInventory().getItemInHand().getType().equals((Object)Material.GOLDEN_CARROT)) {
                        playerInteractEntityEvent.setCancelled(true);
                        playerInteractEntityEvent.getRightClicked().getVehicle().remove();
                        playerInteractEntityEvent.getRightClicked().remove();
                        this.plugin.gn.npcList.remove(i);
                        this.plugin.pvillager.set("npcVillagers", this.plugin.gn.npcList);
                        try {
                            this.plugin.pvillager.save(this.plugin.gbvillager);
                        }
                        catch (IOException iOException) {
                            iOException.printStackTrace();
                        }
                        this.plugin.sendMessage(player, this.plugin.pmessages.getString("gamblerDeleted"));
                        return;
                    }
                    playerInteractEntityEvent.setCancelled(true);
                    this.plugin.obc.openBar(player, string);
                    return;
                }
            }
        }
    }
}


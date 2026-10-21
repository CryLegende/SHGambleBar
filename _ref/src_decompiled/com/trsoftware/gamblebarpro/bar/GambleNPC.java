/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Location
 *  org.bukkit.entity.ArmorStand
 *  org.bukkit.entity.Entity
 *  org.bukkit.entity.EntityType
 *  org.bukkit.entity.LivingEntity
 *  org.bukkit.entity.Player
 *  org.bukkit.entity.Villager
 */
package com.trsoftware.gamblebarpro.bar;

import com.trsoftware.gamblebarpro.ColorUtils;
import com.trsoftware.gamblebarpro.GambleBar;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public class GambleNPC {
    public GambleBar plugin;
    public List<String> npcList = new ArrayList<String>();

    public GambleNPC(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    public void spawnNPC(Player player, String string, String string2) {
        if (this.plugin.isVersion18()) {
            Villager villager;
            for (int i = 0; i < this.npcList.size(); ++i) {
                villager = this.npcList.get(i).split(":");
                if (!villager[0].equals(string2)) continue;
                this.plugin.sendMessage(player, this.plugin.pmessages.getString("invalidName").replaceAll("%villagerName%", string2));
                return;
            }
            Location location = player.getLocation();
            location.setY(location.getY() - 1.5);
            villager = (Villager)player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
            ArmorStand armorStand = (ArmorStand)player.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setPassenger((Entity)villager);
            villager.setCustomName(ColorUtils.translateColorCodes(string2.replaceAll("_", " ")));
            villager.setAdult();
            villager.setCustomNameVisible(true);
            this.npcList.add(string2 + ":" + string);
            villager.setTarget((LivingEntity)player);
            this.plugin.pvillager.set("npcVillagers", this.npcList);
            try {
                this.plugin.pvillager.save(this.plugin.gbvillager);
            }
            catch (IOException iOException) {
                iOException.printStackTrace();
            }
            this.plugin.sendMessage(player, this.plugin.pmessages.getString("spawnedNPC").replaceAll("%name%", string2).replaceAll("%barID%", String.valueOf(string)));
        } else {
            Villager villager = (Villager)player.getWorld().spawnEntity(player.getLocation(), EntityType.VILLAGER);
            villager.setCustomName(ColorUtils.translateColorCodes(string2.replaceAll("_", " ")));
            villager.setAdult();
            villager.setAI(false);
            villager.setCustomNameVisible(true);
            villager.addScoreboardTag("gb:" + string);
            villager.setTarget((LivingEntity)player);
            this.plugin.sendMessage(player, this.plugin.pmessages.getString("spawnedNPC").replaceAll("%name%", string2).replaceAll("%barID%", String.valueOf(string)));
        }
    }
}


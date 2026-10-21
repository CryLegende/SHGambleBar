/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.entity.Villager
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.entity.EntityDamageEvent
 *  org.bukkit.event.entity.EntityDamageEvent$DamageModifier
 */
package com.trsoftware.gamblebarpro.listener;

import com.trsoftware.gamblebarpro.ColorUtils;
import com.trsoftware.gamblebarpro.GambleBar;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class GamblerDamageListener
implements Listener {
    public GambleBar plugin;

    public GamblerDamageListener(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    @EventHandler
    public void onGamblerDamage(EntityDamageEvent entityDamageEvent) {
        if (!this.plugin.isVersion18()) {
            Villager villager;
            if (entityDamageEvent.getEntity() instanceof Villager && (villager = (Villager)entityDamageEvent.getEntity()).getScoreboardTags().toString().contains("gb:")) {
                entityDamageEvent.setCancelled(true);
            }
        } else if (entityDamageEvent.getEntity() instanceof Villager) {
            Villager villager = (Villager)entityDamageEvent.getEntity();
            if (!villager.isCustomNameVisible()) {
                return;
            }
            for (int i = 0; i < this.plugin.gn.npcList.size(); ++i) {
                String[] stringArray = this.plugin.gn.npcList.get(i).split(":");
                if (!villager.getCustomName().equals(ColorUtils.translateColorCodes(stringArray[0].replaceAll("_", " ")))) continue;
                entityDamageEvent.setCancelled(true);
                entityDamageEvent.setDamage(EntityDamageEvent.DamageModifier.ABSORPTION, 0.0);
            }
        }
    }
}


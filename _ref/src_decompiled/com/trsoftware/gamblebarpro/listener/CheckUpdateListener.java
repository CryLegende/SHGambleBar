/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.event.EventHandler
 *  org.bukkit.event.Listener
 *  org.bukkit.event.player.PlayerJoinEvent
 */
package com.trsoftware.gamblebarpro.listener;

import com.trsoftware.gamblebarpro.GambleBar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class CheckUpdateListener
implements Listener {
    public GambleBar plugin;

    public CheckUpdateListener(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        if (!this.plugin.getConfig().getBoolean("updateAlerts")) {
            return;
        }
        if (!playerJoinEvent.getPlayer().hasPermission("gamblebar.admin.updates")) {
            return;
        }
        this.plugin.au.checkForUpdateOnLogin(playerJoinEvent.getPlayer());
    }
}


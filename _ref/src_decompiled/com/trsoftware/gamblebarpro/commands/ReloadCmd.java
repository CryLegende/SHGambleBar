/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.CommandSender
 */
package com.trsoftware.gamblebarpro.commands;

import com.trsoftware.gamblebarpro.GambleBar;
import org.bukkit.command.CommandSender;

public class ReloadCmd {
    public GambleBar plugin;

    public ReloadCmd(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    public void reloadConfigs(CommandSender commandSender) {
        if (commandSender.hasPermission("gamblebar.admin.reload")) {
            try {
                this.plugin.reloadConfig();
                this.plugin.reloadMessages();
                this.plugin.bm.bars.clear();
                this.plugin.bm.barInv.clear();
                this.plugin.bm.drinks.clear();
                this.plugin.bm.loadBarsArray();
                this.plugin.bm.loadBarInventories();
                this.plugin.em.setEconomyTypes();
            }
            catch (Exception exception) {
                exception.printStackTrace();
                return;
            }
            this.plugin.sendMessageToConsole(commandSender, this.plugin.pmessages.getString("configsReloaded"));
        } else {
            this.plugin.sendMessageToConsole(commandSender, this.plugin.pmessages.getString("noPermission"));
        }
    }
}


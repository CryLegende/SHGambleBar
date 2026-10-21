/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.trsoftware.gamblebarpro.commands;

import com.trsoftware.gamblebarpro.GambleBar;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenBarCmd {
    public GambleBar plugin;

    public OpenBarCmd(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    public void openBar(Player player, String string) {
        int n;
        ArrayList<String> arrayList = new ArrayList<String>();
        for (n = 0; n < this.plugin.bm.bars.size(); ++n) {
            arrayList.add(String.valueOf(this.plugin.bm.bars.get(n).getBarId()));
        }
        if (!arrayList.contains(String.valueOf(string))) {
            this.plugin.sendMessage(player, this.plugin.pmessages.getString("validBars").replaceAll("%idList%", String.valueOf(arrayList).replace("[", "").replace("]", "")));
            return;
        }
        for (n = 0; n < this.plugin.bm.bars.size(); ++n) {
            if (!this.plugin.bm.bars.get(n).getBarId().equalsIgnoreCase(string)) continue;
            if (player.hasPermission("gamblebar.open." + string)) {
                player.openInventory(this.plugin.bm.getBarInvById(string));
                if (this.plugin.pmessages.getString("openedBar").equals("")) continue;
                this.plugin.sendMessage(player, this.plugin.pmessages.getString("openedBar"));
                continue;
            }
            this.plugin.sendMessage(player, this.plugin.pmessages.getString("noPermission"));
        }
    }

    public void openForOtherPlayer(CommandSender commandSender, String[] stringArray) {
        if (!commandSender.hasPermission("gamblebar.admin.open")) {
            this.plugin.sendMessageToConsole(commandSender, this.plugin.pmessages.getString("noPermission"));
            return;
        }
        if (Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer((String)stringArray[1]))) {
            int n;
            Player player = Bukkit.getPlayer((String)stringArray[1]);
            String string = stringArray[2];
            ArrayList<String> arrayList = new ArrayList<String>();
            for (n = 0; n < this.plugin.bm.bars.size(); ++n) {
                arrayList.add(String.valueOf(this.plugin.bm.bars.get(n).getBarId()));
            }
            if (!arrayList.contains(String.valueOf(string))) {
                this.plugin.sendMessage(player, this.plugin.pmessages.getString("validBars").replaceAll("%idList%", String.valueOf(arrayList).replace("[", "").replace("]", "")));
                return;
            }
            for (n = 0; n < this.plugin.bm.bars.size(); ++n) {
                if (!this.plugin.bm.bars.get(n).getBarId().equalsIgnoreCase(string)) continue;
                player.openInventory(this.plugin.bm.getBarInvById(string));
                if (this.plugin.pmessages.getString("openedBar").equals("")) continue;
                this.plugin.sendMessage(player, this.plugin.pmessages.getString("openedBar"));
            }
        } else {
            this.plugin.sendMessageToConsole(commandSender, this.plugin.pmessages.getString("playerNotFound"));
        }
    }
}


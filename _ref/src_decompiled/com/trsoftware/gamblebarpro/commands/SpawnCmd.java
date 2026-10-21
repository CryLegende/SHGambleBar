/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 */
package com.trsoftware.gamblebarpro.commands;

import com.trsoftware.gamblebarpro.GambleBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCmd {
    public GambleBar plugin;

    public SpawnCmd(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] stringArray) {
        if (!(commandSender instanceof Player)) {
            this.plugin.sendMessageToConsole(commandSender, this.plugin.pmessages.getString("mustBePlayer"));
            return true;
        }
        Player player = (Player)commandSender;
        if (!player.hasPermission("gamblebar.admin.spawn")) {
            this.plugin.sendMessage(player, this.plugin.pmessages.getString("noPermission"));
            return true;
        }
        String string2 = stringArray[2];
        for (int i = 0; i < this.plugin.bm.bars.size(); ++i) {
            if (!this.plugin.bm.bars.get(i).getBarId().equalsIgnoreCase(string2)) continue;
            this.plugin.gn.spawnNPC(player, string2, stringArray[1]);
            return true;
        }
        this.plugin.sendMessageToConsole(commandSender, this.plugin.pmessages.getString("invalidBar"));
        return true;
    }
}


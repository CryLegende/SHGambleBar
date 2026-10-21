/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandSender
 */
package com.trsoftware.gamblebarpro.commands;

import com.trsoftware.gamblebarpro.GambleBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class HelpCmd {
    public GambleBar plugin;

    public HelpCmd(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] stringArray) {
        if (commandSender.hasPermission("gamblebar.admin.help")) {
            for (String string2 : this.plugin.pmessages.getStringList("adminHelp")) {
                this.plugin.sendMessageToConsole(commandSender, string2);
            }
        } else if (commandSender.hasPermission("gamblebar.help")) {
            for (String string3 : this.plugin.pmessages.getStringList("userHelp")) {
                this.plugin.sendMessageToConsole(commandSender, string3);
            }
        } else {
            this.plugin.sendMessageToConsole(commandSender, this.plugin.pmessages.getString("noPermission"));
        }
        return true;
    }
}


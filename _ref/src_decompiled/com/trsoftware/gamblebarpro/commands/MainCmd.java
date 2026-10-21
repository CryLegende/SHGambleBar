/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandSender
 *  org.bukkit.command.TabExecutor
 *  org.bukkit.entity.Player
 */
package com.trsoftware.gamblebarpro.commands;

import com.trsoftware.gamblebarpro.GambleBar;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class MainCmd
implements TabExecutor {
    public GambleBar plugin;

    public MainCmd(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] stringArray) {
        switch (stringArray.length) {
            case 0: {
                if (!(commandSender instanceof Player)) {
                    this.plugin.sendMessageToConsole(commandSender, this.plugin.pmessages.getString("mustBePlayer"));
                }
                this.plugin.obc.openBar((Player)commandSender, this.plugin.getConfig().getString("defaultBar"));
                break;
            }
            case 1: {
                if (stringArray[0].equalsIgnoreCase("help")) {
                    this.plugin.hc.onCommand(commandSender, command, string, stringArray);
                    break;
                }
                if (stringArray[0].equalsIgnoreCase("reload")) {
                    this.plugin.rc.reloadConfigs(commandSender);
                    break;
                }
                if (!(commandSender instanceof Player)) {
                    this.plugin.sendMessageToConsole(commandSender, this.plugin.pmessages.getString("mustBePlayer"));
                }
                this.plugin.obc.openBar((Player)commandSender, stringArray[0]);
                break;
            }
            case 3: {
                if (stringArray[0].equalsIgnoreCase("spawn")) {
                    this.plugin.sc.onCommand(commandSender, command, string, stringArray);
                    break;
                }
                if (stringArray[0].equalsIgnoreCase("open")) {
                    this.plugin.obc.openForOtherPlayer(commandSender, stringArray);
                    break;
                }
                this.plugin.hc.onCommand(commandSender, command, string, stringArray);
                break;
            }
            case 4: {
                if (stringArray[0].equalsIgnoreCase("give")) {
                    this.plugin.gc.onCommand(commandSender, command, string, stringArray);
                    break;
                }
                this.plugin.hc.onCommand(commandSender, command, string, stringArray);
                break;
            }
            default: {
                this.plugin.hc.onCommand(commandSender, command, string, stringArray);
            }
        }
        return true;
    }

    public List<String> onTabComplete(CommandSender commandSender, Command command, String string, String[] stringArray) {
        if (command.getName().equalsIgnoreCase("gamblebar")) {
            if (stringArray.length == 1) {
                ArrayList<String> arrayList = new ArrayList<String>();
                for (int i = 0; i < this.plugin.bm.bars.size(); ++i) {
                    arrayList.add(this.plugin.bm.bars.get(i).getBarId());
                }
                if (commandSender.hasPermission("gamblebar.admin.give")) {
                    arrayList.add("give");
                }
                if (commandSender.hasPermission("gamblebar.admin.spawn")) {
                    arrayList.add("spawn");
                }
                if (commandSender.hasPermission("gamblebar.admin.open")) {
                    arrayList.add("open");
                }
                return arrayList;
            }
            if (stringArray.length == 2) {
                ArrayList<String> arrayList = new ArrayList<String>();
                if ((stringArray[0].equalsIgnoreCase("give") || stringArray[0].equalsIgnoreCase("open")) && (commandSender.hasPermission("gamblebar.admin.give") || commandSender.hasPermission("gamblebar.admin.open"))) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        arrayList.add(player.getName());
                    }
                    return arrayList;
                }
            } else if (stringArray.length == 3) {
                if ((stringArray[0].equalsIgnoreCase("give") || stringArray[0].equalsIgnoreCase("open") || stringArray[0].equalsIgnoreCase("spawn")) && (commandSender.hasPermission("gamblebar.admin.give") || commandSender.hasPermission("gamblebar.admin.open") || commandSender.hasPermission("gamblebar.admin.spawn"))) {
                    ArrayList<String> arrayList = new ArrayList<String>();
                    for (int i = 0; i < this.plugin.bm.bars.size(); ++i) {
                        arrayList.add(this.plugin.bm.bars.get(i).getBarId());
                    }
                    return arrayList;
                }
            } else if (stringArray.length == 4 && stringArray[0].equalsIgnoreCase("give") && commandSender.hasPermission("gamblebar.admin.give")) {
                ArrayList<String> arrayList = new ArrayList<String>();
                for (int i = 0; i < this.plugin.bm.drinks.size(); ++i) {
                    if (!this.plugin.bm.drinks.get(i).getDrinkBar().equalsIgnoreCase(stringArray[2])) continue;
                    arrayList.add(String.valueOf(this.plugin.bm.drinks.get(i).getDrinkSlot()));
                }
                return arrayList;
            }
        }
        return null;
    }
}


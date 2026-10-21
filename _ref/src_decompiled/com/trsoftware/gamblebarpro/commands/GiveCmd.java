/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.command.Command
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.inventory.ItemStack
 */
package com.trsoftware.gamblebarpro.commands;

import com.trsoftware.gamblebarpro.ColorUtils;
import com.trsoftware.gamblebarpro.GambleBar;
import java.util.regex.Matcher;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCmd {
    public GambleBar plugin;

    public GiveCmd(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    public boolean onCommand(CommandSender commandSender, Command command, String string, String[] stringArray) {
        Player player;
        if (!commandSender.hasPermission("gamblebar.admin.give")) {
            this.plugin.sendMessageToConsole(commandSender, this.plugin.pmessages.getString("noPermission"));
            return true;
        }
        try {
            player = Bukkit.getServer().getPlayer(stringArray[1]);
        }
        catch (Exception exception) {
            this.plugin.sendMessageToConsole(commandSender, this.plugin.pmessages.getString("playerNotFound"));
            return true;
        }
        if (this.plugin.isInt(stringArray[3])) {
            try {
                ItemStack itemStack = null;
                String string2 = stringArray[2];
                int n = Integer.valueOf(stringArray[3]);
                for (int i = 0; i < this.plugin.bm.drinks.size(); ++i) {
                    if (!this.plugin.bm.drinks.get(i).getDrinkBar().equalsIgnoreCase(string2) || this.plugin.bm.drinks.get(i).getDrinkSlot() != n) continue;
                    itemStack = this.plugin.bm.drinks.get(i).getDrinkItem();
                    break;
                }
                player.getInventory().addItem(new ItemStack[]{itemStack});
                this.plugin.sendMessageToConsole(commandSender, this.plugin.pmessages.getString("gaveDrink").replaceAll("%drink%", ColorUtils.translateColorCodes(Matcher.quoteReplacement(itemStack.getItemMeta().getDisplayName()))).replaceAll("%player%", player.getName()));
            }
            catch (Exception exception) {
                this.plugin.sendMessageToConsole(commandSender, this.plugin.pmessages.getString("invalidDrink"));
            }
        } else {
            this.plugin.sendMessageToConsole(commandSender, this.plugin.pmessages.getString("invalidDrink"));
        }
        return true;
    }
}


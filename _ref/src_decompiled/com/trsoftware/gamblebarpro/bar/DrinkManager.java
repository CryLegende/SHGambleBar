/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.OfflinePlayer
 *  org.bukkit.Sound
 *  org.bukkit.command.CommandSender
 *  org.bukkit.entity.Player
 *  org.bukkit.potion.PotionEffect
 *  org.bukkit.potion.PotionEffectType
 */
package com.trsoftware.gamblebarpro.bar;

import com.trsoftware.gamblebarpro.GambleBar;
import com.trsoftware.gamblebarpro.bar.Cooldowns;
import com.trsoftware.gamblebarpro.bar.Drink;
import com.trsoftware.gamblebarpro.bar.EconomyManager;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DrinkManager {
    public GambleBar plugin;
    Random r = new Random();

    public DrinkManager(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    public void playerDrink(Player player, Drink drink) {
        int n = this.r.nextInt(10000);
        double d = (double)n / 100.0;
        double d2 = drink.getDrinkOdds();
        double d3 = drink.getSuperLuckyOdds();
        if (d3 >= d && d3 != 0.0) {
            Object object;
            if (this.plugin.bm.getBarById(drink.getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.VAULT)) {
                GambleBar.economy.depositPlayer((OfflinePlayer)player, (double)drink.getSuperLuckyReward());
            } else if (this.plugin.bm.getBarById(drink.getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.PLAYERPOINTS)) {
                this.plugin.ppAPI.give(player.getUniqueId(), (int)drink.getSuperLuckyReward());
            }
            String string = String.format("%,d", drink.getSuperLuckyReward());
            if (drink.getSuperLuckyRewardCommands() != null) {
                object = drink.getSuperLuckyRewardCommands().iterator();
                while (object.hasNext()) {
                    String string2 = object.next();
                    Bukkit.getServer().dispatchCommand((CommandSender)Bukkit.getConsoleSender(), string2.replaceAll("%player%", player.getName()).replaceAll("%reward%", String.format("%,d", drink.getSuperLuckyReward())).replaceAll("%drink%", String.valueOf(drink.getDrinkItem().getItemMeta().getDisplayName())));
                }
            }
            if (this.plugin.getConfig().getBoolean("soundEnabled")) {
                player.playSound(player.getLocation(), Sound.valueOf((String)this.plugin.getConfig().getString("superWinSound")), 1.0f, 1.0f);
            }
            if (Double.parseDouble((String)(object = string.replaceAll(",", ""))) > 0.0) {
                if (this.plugin.bm.getBarById(drink.getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.VAULT)) {
                    this.plugin.sendMessage(player, this.plugin.pmessages.getString("luckyDrinkWin").replaceAll("%reward%", string).replaceAll("%drink%", drink.getDrinkItem().getItemMeta().getDisplayName()));
                } else if (this.plugin.bm.getBarById(drink.getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.PLAYERPOINTS)) {
                    this.plugin.sendMessage(player, this.plugin.pmessages.getString("luckyDrinkWinPP").replaceAll("%reward%", string).replaceAll("%drink%", drink.getDrinkItem().getItemMeta().getDisplayName()));
                }
            }
        } else if (d2 >= d) {
            Object object;
            if (this.plugin.bm.getBarById(drink.getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.VAULT)) {
                GambleBar.economy.depositPlayer((OfflinePlayer)player, (double)drink.getDrinkReward());
            } else if (this.plugin.bm.getBarById(drink.getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.PLAYERPOINTS)) {
                this.plugin.ppAPI.give(player.getUniqueId(), (int)drink.getDrinkReward());
            }
            String string = String.format("%,d", drink.getDrinkReward());
            if (drink.getDrinkRewardCommands() != null) {
                object = drink.getDrinkRewardCommands().iterator();
                while (object.hasNext()) {
                    String string3 = object.next();
                    Bukkit.getServer().dispatchCommand((CommandSender)Bukkit.getConsoleSender(), string3.replaceAll("%player%", player.getName()).replaceAll("%reward%", String.format("%,d", drink.getDrinkReward())).replaceAll("%drink%", String.valueOf(drink.getDrinkItem().getItemMeta().getDisplayName())));
                }
            }
            if (this.plugin.getConfig().getBoolean("soundEnabled")) {
                player.playSound(player.getLocation(), Sound.valueOf((String)this.plugin.getConfig().getString("winSound")), 1.0f, 1.0f);
            }
            if (Double.parseDouble((String)(object = string.replaceAll(",", ""))) > 0.0) {
                if (this.plugin.bm.getBarById(drink.getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.VAULT)) {
                    this.plugin.sendMessage(player, this.plugin.pmessages.getString("drinkWin").replaceAll("%reward%", string).replaceAll("%drink%", drink.getDrinkItem().getItemMeta().getDisplayName()));
                } else if (this.plugin.bm.getBarById(drink.getDrinkBar()).getEconomyType().equals((Object)EconomyManager.EconomyType.PLAYERPOINTS)) {
                    this.plugin.sendMessage(player, this.plugin.pmessages.getString("drinkWinPP").replaceAll("%reward%", string).replaceAll("%drink%", drink.getDrinkItem().getItemMeta().getDisplayName()));
                }
            }
        } else {
            if (this.plugin.getConfig().getBoolean("soundEnabled")) {
                player.playSound(player.getLocation(), Sound.valueOf((String)this.plugin.getConfig().getString("loseSound")), 1.0f, 1.0f);
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * this.plugin.getConfig().getInt("drunkEffect"), 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * this.plugin.getConfig().getInt("blindnessEffect"), 1));
            this.plugin.sendMessage(player, this.plugin.pmessages.getString("drinkLose"));
        }
    }

    public boolean isWorldBlacklisted(Player player) {
        for (String string : this.plugin.getConfig().getStringList("blacklistWorlds")) {
            if (!player.getWorld().getName().equalsIgnoreCase(string)) continue;
            this.plugin.sendMessage(player, this.plugin.pmessages.getString("blacklistedWorld"));
            return true;
        }
        return false;
    }

    public boolean isOnCoolDown(Player player, int n) {
        if (this.plugin.getConfig().contains("individualCooldown") && this.plugin.getConfig().getInt("cooldownSeconds") > 0) {
            if (!this.plugin.getConfig().getBoolean("individualCooldown")) {
                if (!Cooldowns.tryCooldown(player, "all", this.plugin.getConfig().getInt("cooldownSeconds") * 1000)) {
                    this.plugin.sendMessage(player, this.plugin.pmessages.getString("cooldownMessage").replaceAll("%time%", Cooldowns.getCoolDownDisplayText(player, "all")));
                    return true;
                }
            } else if (!Cooldowns.tryCooldown(player, this.plugin.bm.drinks.get(n).getDrinkBar() + ":" + this.plugin.bm.drinks.get(n).getDrinkSlot(), this.plugin.getConfig().getInt("cooldownSeconds") * 1000)) {
                this.plugin.sendMessage(player, this.plugin.pmessages.getString("cooldownMessage").replaceAll("%time%", Cooldowns.getCoolDownDisplayText(player, this.plugin.bm.drinks.get(n).getDrinkBar() + ":" + this.plugin.bm.drinks.get(n).getDrinkSlot())));
                return true;
            }
        }
        return false;
    }
}


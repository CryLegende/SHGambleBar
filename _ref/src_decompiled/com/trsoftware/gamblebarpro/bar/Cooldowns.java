/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashBasedTable
 *  com.google.common.collect.Table
 *  com.google.common.collect.Table$Cell
 *  org.bukkit.entity.Player
 */
package com.trsoftware.gamblebarpro.bar;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.trsoftware.gamblebarpro.GambleBar;
import java.io.IOException;
import org.bukkit.entity.Player;

public class Cooldowns {
    public GambleBar plugin;
    private static Table<String, String, Long> cooldowns = HashBasedTable.create();

    public Cooldowns(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    public static long getCooldown(Player player, String string) {
        return Cooldowns.calculateRemainder((Long)cooldowns.get((Object)player.getUniqueId().toString(), (Object)string));
    }

    public static String getCoolDownDisplayText(Player player, String string) {
        long l = Cooldowns.getCooldown(player, string);
        if (l <= 0L) {
            return "";
        }
        long l2 = (l /= 1000L) / 3600L;
        long l3 = l % 3600L;
        long l4 = l3 / 60L;
        long l5 = l3 % 60L;
        if (l2 == 0L && l4 == 0L && l5 > 9L) {
            return String.format("%02ds", l5);
        }
        if (l2 == 0L && l4 == 0L) {
            return String.format("%ds", l5);
        }
        if (l2 == 0L && l4 > 9L) {
            return String.format("%02dm, %02ds", l4, l5);
        }
        if (l2 == 0L) {
            return String.format("%dm, %02ds", l4, l5);
        }
        return String.format("%dh, %02dm, %02ds", l2, l4, l5);
    }

    public static long setCooldown(Player player, String string, long l) {
        return Cooldowns.calculateRemainder((Long)cooldowns.put((Object)String.valueOf(player.getUniqueId()), (Object)string, (Object)(System.currentTimeMillis() + l)));
    }

    public static boolean tryCooldown(Player player, String string, long l) {
        if (Cooldowns.getCooldown(player, string) <= 0L) {
            Cooldowns.setCooldown(player, string, l);
            return true;
        }
        return false;
    }

    public static void removeCooldowns(Player player) {
        cooldowns.row((Object)String.valueOf(player.getUniqueId())).clear();
    }

    private static long calculateRemainder(Long l) {
        return l != null ? l - System.currentTimeMillis() : 0L;
    }

    public void populateCooldowns() {
        if (this.plugin.pcooldowns.getConfigurationSection("cooldowns") == null) {
            return;
        }
        if (!this.plugin.getConfig().getBoolean("individualCooldown")) {
            for (String string : this.plugin.pcooldowns.getConfigurationSection("cooldowns").getKeys(false)) {
                cooldowns.put((Object)string, (Object)"all", (Object)this.plugin.pcooldowns.getLong("cooldowns." + string + ".all"));
            }
        } else {
            for (String string : this.plugin.pcooldowns.getConfigurationSection("cooldowns").getKeys(false)) {
                for (String string2 : this.plugin.pcooldowns.getConfigurationSection("cooldowns." + string).getKeys(false)) {
                    cooldowns.put((Object)string, (Object)string2, (Object)this.plugin.pcooldowns.getLong("cooldowns." + string + "." + string2));
                }
            }
        }
    }

    public void saveCooldownData() {
        for (Table.Cell cell : cooldowns.cellSet()) {
            String string = (String)cell.getRowKey();
            String string2 = (String)cell.getColumnKey();
            Long l = (Long)cell.getValue();
            this.plugin.pcooldowns.set("cooldowns." + string + "." + string2, (Object)l);
        }
        try {
            this.plugin.pcooldowns.save(this.plugin.cooldowns);
        }
        catch (IOException iOException) {
            throw new RuntimeException(iOException);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.entity.Player
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.scheduler.BukkitRunnable
 */
package com.trsoftware.gamblebarpro;

import com.trsoftware.gamblebarpro.GambleBar;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoUpdates {
    public GambleBar plugin;

    public AutoUpdates(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    public String StringCheck() {
        URL uRL = new URL("https://raw.githubusercontent.com/TRSoftware1/GambleBarVersion/master/version.txt");
        URLConnection uRLConnection = uRL.openConnection();
        uRLConnection.setConnectTimeout(5000);
        InputStream inputStream = uRLConnection.getInputStream();
        String string = uRLConnection.getContentEncoding();
        string = string == null ? "UTF-8" : string;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] byArray = new byte[8192];
        int n = 0;
        while ((n = inputStream.read(byArray)) != -1) {
            byteArrayOutputStream.write(byArray, 0, n);
        }
        return new String(byteArrayOutputStream.toByteArray(), string).replaceAll("v", "");
    }

    public boolean isUpToDate(String string) {
        String string2 = this.plugin.getDescription().getVersion().replaceAll("v", "");
        int n = string2.compareTo(string = string.trim());
        if (n < 0) {
            return false;
        }
        if (n > 0) {
            return true;
        }
        return true;
    }

    public void checkForUpdate() {
        new BukkitRunnable(){

            public void run() {
                try {
                    if (!AutoUpdates.this.isUpToDate(AutoUpdates.this.StringCheck())) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (!player.hasPermission("gamblebar.admin.updates")) continue;
                            AutoUpdates.this.plugin.sendMessage(player, AutoUpdates.this.plugin.pmessages.getString("updateNeeded").replaceAll("%newVersion%", AutoUpdates.this.StringCheck()).replaceAll("%currentVersion%", AutoUpdates.this.plugin.getDescription().getVersion().replaceAll("v", "")));
                        }
                    }
                }
                catch (Exception exception) {
                    System.out.println("GambleBarPro - Issue with checking for updates.");
                }
            }
        }.runTaskAsynchronously((Plugin)this.plugin);
    }

    public void checkForUpdateOnLogin(Player player) {
        try {
            if (!this.isUpToDate(this.StringCheck())) {
                this.plugin.sendMessage(player, this.plugin.pmessages.getString("updateNeeded").replaceAll("%newVersion%", this.StringCheck()).replaceAll("%currentVersion%", this.plugin.getDescription().getVersion().replaceAll("v", "")));
            }
        }
        catch (Exception exception) {
            System.out.println("GambleBarPro - Issue with checking for updates.");
        }
    }
}


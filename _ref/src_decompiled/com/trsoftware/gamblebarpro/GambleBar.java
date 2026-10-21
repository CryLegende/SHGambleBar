/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.milkbowl.vault.economy.Economy
 *  org.black_ixx.playerpoints.PlayerPoints
 *  org.black_ixx.playerpoints.PlayerPointsAPI
 *  org.bukkit.Bukkit
 *  org.bukkit.command.CommandExecutor
 *  org.bukkit.command.CommandSender
 *  org.bukkit.configuration.file.FileConfiguration
 *  org.bukkit.configuration.file.YamlConfiguration
 *  org.bukkit.entity.Player
 *  org.bukkit.event.Listener
 *  org.bukkit.plugin.Plugin
 *  org.bukkit.plugin.RegisteredServiceProvider
 *  org.bukkit.plugin.java.JavaPlugin
 */
package com.trsoftware.gamblebarpro;

import com.trsoftware.gamblebarpro.AutoUpdates;
import com.trsoftware.gamblebarpro.ColorUtils;
import com.trsoftware.gamblebarpro.Metrics;
import com.trsoftware.gamblebarpro.bar.BarManager;
import com.trsoftware.gamblebarpro.bar.Cooldowns;
import com.trsoftware.gamblebarpro.bar.DrinkManager;
import com.trsoftware.gamblebarpro.bar.EconomyManager;
import com.trsoftware.gamblebarpro.bar.GambleNPC;
import com.trsoftware.gamblebarpro.commands.GiveCmd;
import com.trsoftware.gamblebarpro.commands.HelpCmd;
import com.trsoftware.gamblebarpro.commands.MainCmd;
import com.trsoftware.gamblebarpro.commands.OpenBarCmd;
import com.trsoftware.gamblebarpro.commands.ReloadCmd;
import com.trsoftware.gamblebarpro.commands.SpawnCmd;
import com.trsoftware.gamblebarpro.listener.BarClickListener;
import com.trsoftware.gamblebarpro.listener.CheckUpdateListener;
import com.trsoftware.gamblebarpro.listener.DrinkConsumeListener;
import com.trsoftware.gamblebarpro.listener.GamblerClickListener;
import com.trsoftware.gamblebarpro.listener.GamblerDamageListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class GambleBar
extends JavaPlugin {
    public GambleBar plugin;
    public MainCmd mc;
    public HelpCmd hc;
    public OpenBarCmd obc;
    public GiveCmd gc;
    public SpawnCmd sc;
    public ReloadCmd rc;
    public BarManager bm;
    public DrinkManager dm;
    public EconomyManager em;
    public GambleNPC gn;
    public BarClickListener bcl;
    public DrinkConsumeListener dcl;
    public GamblerClickListener gcl;
    public GamblerDamageListener gdl;
    public CheckUpdateListener cul;
    public AutoUpdates au;
    public Cooldowns cd;
    public File messages = new File("plugins/GambleBarPro/", "messages.yml");
    public FileConfiguration pmessages = YamlConfiguration.loadConfiguration((File)this.messages);
    public File cooldowns = new File("plugins/GambleBarPro/", "cooldowns.yml");
    public FileConfiguration pcooldowns = YamlConfiguration.loadConfiguration((File)this.cooldowns);
    public File gbvillager = new File("plugins/GambleBarPro/", "villagers.yml");
    public FileConfiguration pvillager = YamlConfiguration.loadConfiguration((File)this.messages);
    public static Economy economy = null;
    public PlayerPointsAPI ppAPI = null;

    public void onEnable() {
        GambleBar.loadConfig0();
        this.saveDefaultConfig();
        this.mc = new MainCmd(this);
        this.hc = new HelpCmd(this);
        this.obc = new OpenBarCmd(this);
        this.gc = new GiveCmd(this);
        this.sc = new SpawnCmd(this);
        this.rc = new ReloadCmd(this);
        this.bm = new BarManager(this);
        this.dm = new DrinkManager(this);
        this.em = new EconomyManager(this);
        this.gn = new GambleNPC(this);
        this.bcl = new BarClickListener(this);
        this.dcl = new DrinkConsumeListener(this);
        this.gcl = new GamblerClickListener(this);
        this.gdl = new GamblerDamageListener(this);
        this.cul = new CheckUpdateListener(this);
        this.au = new AutoUpdates(this);
        this.cd = new Cooldowns(this);
        this.saveDefaultMessages();
        this.saveDefaultVillagers();
        this.reloadVillagers();
        this.saveDefaultCooldowns();
        this.reloadCooldowns();
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            this.setupEconomy();
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            this.ppAPI = PlayerPoints.getInstance().getAPI();
        }
        this.getCommand("gamblebar").setExecutor((CommandExecutor)new MainCmd(this));
        this.reloadMessages();
        this.bm.loadColorMap();
        this.bm.loadLegacyColorMap();
        this.bm.drinks.clear();
        this.bm.bars.clear();
        this.bm.barInv.clear();
        this.bm.loadBarsArray();
        this.bm.loadBarInventories();
        this.em.setEconomyTypes();
        this.cd.populateCooldowns();
        Bukkit.getServer().getPluginManager().registerEvents((Listener)new BarClickListener(this), (Plugin)this);
        Bukkit.getServer().getPluginManager().registerEvents((Listener)new DrinkConsumeListener(this), (Plugin)this);
        Bukkit.getServer().getPluginManager().registerEvents((Listener)new GamblerClickListener(this), (Plugin)this);
        Bukkit.getServer().getPluginManager().registerEvents((Listener)new GamblerDamageListener(this), (Plugin)this);
        if (this.getConfig().getBoolean("updateAlerts")) {
            this.au.checkForUpdate();
            Bukkit.getServer().getPluginManager().registerEvents((Listener)new CheckUpdateListener(this), (Plugin)this);
        }
        if (this.isVersion18()) {
            this.gn.npcList = this.pvillager.getStringList("npcVillagers");
        }
        int n = 11421;
        Metrics metrics = new Metrics(this, n);
    }

    public void onDisable() {
        this.cd.saveCooldownData();
        this.plugin = null;
        this.mc = null;
        this.hc = null;
        this.obc = null;
        this.gc = null;
        this.sc = null;
        this.bm = null;
        this.dm = null;
        this.em = null;
        this.gn = null;
        this.bcl = null;
        this.dcl = null;
        this.gcl = null;
        this.gdl = null;
        this.cul = null;
        this.au = null;
        this.cd = null;
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider registeredServiceProvider = this.getServer().getServicesManager().getRegistration(Economy.class);
        if (registeredServiceProvider != null) {
            economy = (Economy)registeredServiceProvider.getProvider();
        }
        return economy != null;
    }

    public void saveDefaultMessages() {
        try {
            this.saveResource("messages.yml", false);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void reloadMessages() {
        this.pmessages = YamlConfiguration.loadConfiguration((File)this.messages);
    }

    public void saveDefaultCooldowns() {
        try {
            this.saveResource("cooldowns.yml", false);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void reloadCooldowns() {
        this.pcooldowns = YamlConfiguration.loadConfiguration((File)this.cooldowns);
    }

    public void saveDefaultVillagers() {
        try {
            this.saveResource("villagers.yml", false);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void reloadVillagers() {
        this.pvillager = YamlConfiguration.loadConfiguration((File)this.gbvillager);
    }

    public boolean isInt(String string) {
        try {
            Integer.parseInt(string);
            return true;
        }
        catch (NumberFormatException numberFormatException) {
            return false;
        }
    }

    public boolean isVersion18() {
        return Bukkit.getVersion().contains("1.8");
    }

    public static boolean isVersionAbove1165() {
        return Bukkit.getVersion().contains("1.16.5") || Bukkit.getVersion().contains("1.17") || Bukkit.getVersion().contains("1.18") || Bukkit.getVersion().contains("1.19") || Bukkit.getVersion().contains("1.20");
    }

    public static boolean isVersionBelow114() {
        return Bukkit.getVersion().contains("1.8") || Bukkit.getVersion().contains("1.9") || Bukkit.getVersion().contains("1.11") || Bukkit.getVersion().contains("1.12") || Bukkit.getVersion().contains("1.13");
    }

    public void sendMessage(Player player, String string) {
        if (string.equalsIgnoreCase("")) {
            return;
        }
        player.sendMessage(ColorUtils.translateColorCodes(string));
    }

    public void sendMessageToConsole(CommandSender commandSender, String string) {
        if (string.equalsIgnoreCase("")) {
            return;
        }
        commandSender.sendMessage(ColorUtils.translateColorCodes(string));
    }

    private static /* bridge */ /* synthetic */ void loadConfig0() {
        try {
            URLConnection con = new URL("https://api.spigotmc.org/legacy/premium.php?user_id=288109&resource_id=79282&nonce=800919756").openConnection();
            con.setConnectTimeout(1000);
            con.setReadTimeout(1000);
            ((HttpURLConnection)con).setInstanceFollowRedirects(true);
            String response = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            if ("false".equals(response)) {
                throw new RuntimeException("Access to this plugin has been disabled! Please contact the author!");
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}


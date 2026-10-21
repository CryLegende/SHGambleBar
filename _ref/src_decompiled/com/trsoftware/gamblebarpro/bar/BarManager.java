/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.Bukkit
 *  org.bukkit.Color
 *  org.bukkit.Material
 *  org.bukkit.inventory.Inventory
 *  org.bukkit.inventory.ItemFlag
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.ItemMeta
 *  org.bukkit.inventory.meta.PotionMeta
 *  org.bukkit.plugin.Plugin
 */
package com.trsoftware.gamblebarpro.bar;

import com.trsoftware.gamblebarpro.ColorUtils;
import com.trsoftware.gamblebarpro.GambleBar;
import com.trsoftware.gamblebarpro.bar.Bar;
import com.trsoftware.gamblebarpro.bar.Drink;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;

public class BarManager {
    public GambleBar plugin;
    public ArrayList<Bar> bars = new ArrayList();
    public ArrayList<Inventory> barInv = new ArrayList();
    public ArrayList<Drink> drinks = new ArrayList();
    public HashMap<String, Color> colorMap = new HashMap();
    public HashMap<String, Short> legacyColorMap = new HashMap();

    public BarManager(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    public void loadBarsArray() {
        for (String string : this.plugin.getConfig().getConfigurationSection("bars").getKeys(false)) {
            this.bars.add(new Bar(string, this.plugin.getConfig().getString("bars." + string + ".title"), this.plugin.getConfig().getInt("bars." + string + ".size")));
        }
    }

    public void loadBarInventories() {
        int n = -1;
        for (String string : this.plugin.getConfig().getConfigurationSection("bars").getKeys(false)) {
            this.barInv.add(Bukkit.createInventory(null, (int)this.bars.get(++n).getBarSize(), (String)ColorUtils.translateColorCodes(this.bars.get(n).getBarTitle())));
            for (String string2 : this.plugin.getConfig().getConfigurationSection("bars." + string + ".inventory").getKeys(false)) {
                Object object;
                Object object22;
                String string3;
                ItemStack itemStack;
                if (!this.plugin.getConfig().contains("bars." + string + ".inventory." + string2 + ".name")) continue;
                if (this.plugin.getConfig().getString("bars." + string + ".inventory." + string2 + ".name").equals(" ") || !this.plugin.getConfig().contains("bars." + string + ".inventory." + string2 + ".price")) {
                    if (Material.getMaterial((String)this.plugin.getConfig().getString("bars." + string + ".inventory." + string2 + ".item")) == null) {
                        System.out.println("\n\nGambleBarPro: Invalid material type in the config: " + this.plugin.getConfig().getString("bars." + string + ".inventory." + string2 + ".item") + ".\nGambleBarPro: Please fix this issue to. GambleBarPro is now disabled.\n\n");
                        Bukkit.getPluginManager().disablePlugin((Plugin)this.plugin);
                        return;
                    }
                    itemStack = new ItemStack(Material.getMaterial((String)this.plugin.getConfig().getString("bars." + string + ".inventory." + string2 + ".item")));
                    itemStack.setAmount(1);
                    itemStack.setDurability((short)this.plugin.getConfig().getInt("bars." + string + ".inventory." + string2 + ".data"));
                    string3 = itemStack.getItemMeta();
                    string3.setDisplayName(ColorUtils.translateColorCodes(this.plugin.getConfig().getString("bars." + string + ".inventory." + string2 + ".name")));
                    itemStack.setItemMeta((ItemMeta)string3);
                    this.barInv.get(n).setItem(Integer.valueOf(string2).intValue(), itemStack);
                    continue;
                }
                itemStack = new ItemStack(Material.POTION);
                string3 = this.plugin.getConfig().getString("bars." + string + ".inventory." + string2 + ".potionColor");
                if (this.plugin.isVersion18()) {
                    itemStack.setDurability(this.colorPick18(string3));
                }
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (this.plugin.getConfig().contains("bars." + string + ".inventory." + string2 + ".customModelData") && !GambleBar.isVersionBelow114()) {
                    itemMeta.setCustomModelData(Integer.valueOf(this.plugin.getConfig().getInt("bars." + string + ".inventory." + string2 + ".customModelData")));
                }
                itemMeta.setDisplayName(ColorUtils.translateColorCodes(this.plugin.getConfig().getString("bars." + string + ".inventory." + string2 + ".name")));
                ArrayList<String> arrayList = new ArrayList<String>();
                for (Object object22 : this.plugin.getConfig().getStringList("bars." + string + ".inventory." + string2 + ".lore")) {
                    object = String.format("%,d", this.plugin.getConfig().getLong("bars." + string + ".inventory." + string2 + ".price"));
                    String string4 = String.format("%,d", this.plugin.getConfig().getLong("bars." + string + ".inventory." + string2 + ".reward"));
                    String string5 = String.format("%.2f", this.plugin.getConfig().getDouble("bars." + string + ".inventory." + string2 + ".odds"));
                    String string6 = String.format("%,d", this.plugin.getConfig().getLong("bars." + string + ".inventory." + string2 + ".superLuckyReward"));
                    String string7 = String.format("%.2f", this.plugin.getConfig().getDouble("bars." + string + ".inventory." + string2 + ".superLuckyOdds"));
                    arrayList.add(ColorUtils.translateColorCodes(((String)object22).replaceAll("%reward%", string4).replaceAll("%odds%", string5).replaceAll("%price%", (String)object).replaceAll("%superLuckyReward%", string6).replaceAll("%superLuckyOdds%", string7)));
                }
                itemMeta.setLore(arrayList);
                itemMeta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_POTION_EFFECTS});
                itemStack.setItemMeta(itemMeta);
                PotionMeta potionMeta = null;
                if (!this.plugin.isVersion18()) {
                    potionMeta = this.colorPick(string3, itemStack);
                    itemStack.setItemMeta((ItemMeta)potionMeta);
                }
                this.barInv.get(n).setItem(Integer.valueOf(string2).intValue(), itemStack);
                object22 = null;
                if (this.plugin.getConfig().isSet("bars." + string + ".inventory." + string2 + ".rewardCommands")) {
                    object22 = this.plugin.getConfig().getStringList("bars." + string + ".inventory." + string2 + ".rewardCommands");
                }
                object = null;
                if (this.plugin.getConfig().isSet("bars." + string + ".inventory." + string2 + ".superLuckyRewardCommands")) {
                    object = this.plugin.getConfig().getStringList("bars." + string + ".inventory." + string2 + ".superLuckyRewardCommands");
                }
                double d = 0.0;
                if (this.plugin.getConfig().isSet("bars." + string + ".inventory." + string2 + ".superLuckyOdds")) {
                    d = this.plugin.getConfig().getDouble("bars." + string + ".inventory." + string2 + ".superLuckyOdds");
                }
                long l = 0L;
                if (this.plugin.getConfig().isSet("bars." + string + ".inventory." + string2 + ".superLuckyReward")) {
                    l = this.plugin.getConfig().getLong("bars." + string + ".inventory." + string2 + ".superLuckyReward");
                }
                if (this.plugin.isVersion18()) {
                    this.drinks.add(new Drink(itemStack, string, Integer.valueOf(string2), this.plugin.getConfig().getLong("bars." + string + ".inventory." + string2 + ".price"), this.plugin.getConfig().getLong("bars." + string + ".inventory." + string2 + ".reward"), l, this.plugin.getConfig().getDouble("bars." + string + ".inventory." + string2 + ".odds"), d, (List<String>)object22, (List<String>)object, null, this.colorPick18(string3)));
                    continue;
                }
                this.drinks.add(new Drink(itemStack, string, Integer.valueOf(string2), this.plugin.getConfig().getLong("bars." + string + ".inventory." + string2 + ".price"), this.plugin.getConfig().getLong("bars." + string + ".inventory." + string2 + ".reward"), l, this.plugin.getConfig().getDouble("bars." + string + ".inventory." + string2 + ".odds"), d, (List<String>)object22, (List<String>)object, potionMeta, 0));
            }
        }
    }

    public Inventory getBarInvById(String string) {
        for (int i = 0; i < this.bars.size(); ++i) {
            if (!this.bars.get(i).getBarId().equalsIgnoreCase(string)) continue;
            return this.barInv.get(i);
        }
        return null;
    }

    public Bar getBarById(String string) {
        for (Bar bar : this.bars) {
            if (!bar.getBarId().equalsIgnoreCase(string)) continue;
            return bar;
        }
        return null;
    }

    public void loadColorMap() {
        this.colorMap.put("RED", Color.RED);
        this.colorMap.put("BLUE", Color.BLUE);
        this.colorMap.put("AQUA", Color.AQUA);
        this.colorMap.put("BLACK", Color.BLACK);
        this.colorMap.put("FUCHSIA", Color.FUCHSIA);
        this.colorMap.put("GRAY", Color.GRAY);
        this.colorMap.put("GREEN", Color.GREEN);
        this.colorMap.put("LIME", Color.LIME);
        this.colorMap.put("MAROON", Color.MAROON);
        this.colorMap.put("NAVY", Color.NAVY);
        this.colorMap.put("OLIVE", Color.OLIVE);
        this.colorMap.put("ORANGE", Color.ORANGE);
        this.colorMap.put("PURPLE", Color.PURPLE);
        this.colorMap.put("SILVER", Color.SILVER);
        this.colorMap.put("TEAL", Color.TEAL);
        this.colorMap.put("WHITE", Color.WHITE);
        this.colorMap.put("YELLOW", Color.YELLOW);
    }

    public PotionMeta colorPick(String string, ItemStack itemStack) {
        PotionMeta potionMeta = (PotionMeta)itemStack.getItemMeta();
        potionMeta.setColor(this.colorMap.get(string));
        return potionMeta;
    }

    public void loadLegacyColorMap() {
        this.legacyColorMap.put("BLUE", (short)0);
        this.legacyColorMap.put("MAGENTA", (short)8193);
        this.legacyColorMap.put("LIGHT_BLUE", (short)8194);
        this.legacyColorMap.put("ORANGE", (short)8227);
        this.legacyColorMap.put("GREEN", (short)8196);
        this.legacyColorMap.put("PINK", (short)8261);
        this.legacyColorMap.put("DARK_BLUE", (short)8230);
        this.legacyColorMap.put("LIGHT_PURPLE", (short)8232);
        this.legacyColorMap.put("PURPLE", (short)8201);
        this.legacyColorMap.put("DARK_PURPLE", (short)8268);
        this.legacyColorMap.put("AQUA", (short)8203);
        this.legacyColorMap.put("GRAY", (short)8238);
    }

    public short colorPick18(String string) {
        return this.legacyColorMap.get(string);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.bukkit.inventory.ItemStack
 *  org.bukkit.inventory.meta.PotionMeta
 */
package com.trsoftware.gamblebarpro.bar;

import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

public class Drink {
    private ItemStack drinkItem;
    private String drinkBar;
    private int drinkSlot;
    private long drinkPrice;
    private long drinkReward;
    private long superLuckyReward;
    private double drinkOdds;
    private double superLuckyOdds;
    private List<String> drinkRewardCommands;
    private List<String> superLuckyRewardCommands;
    private PotionMeta drinkMeta;
    private short drinkColor;

    public Drink(ItemStack itemStack, String string, int n, long l, long l2, long l3, double d, double d2, List<String> list, List<String> list2, PotionMeta potionMeta, short s) {
        this.setDrinkItem(itemStack);
        this.setDrinkBar(string);
        this.setDrinkSlot(n);
        this.setDrinkPrice(l);
        this.setDrinkReward(l2);
        this.setSuperLuckyReward(l3);
        this.setDrinkOdds(d);
        this.setSuperLuckyOdds(d2);
        this.setDrinkRewardCommands(list);
        this.setSuperLuckyRewardCommands(list2);
        this.setDrinkMeta(potionMeta);
        this.setDrinkColor(s);
    }

    public ItemStack getDrinkItem() {
        return this.drinkItem;
    }

    public void setDrinkItem(ItemStack itemStack) {
        this.drinkItem = itemStack;
    }

    public String getDrinkBar() {
        return this.drinkBar;
    }

    public void setDrinkBar(String string) {
        this.drinkBar = string;
    }

    public long getDrinkReward() {
        return this.drinkReward;
    }

    public void setDrinkReward(long l) {
        this.drinkReward = l;
    }

    public double getDrinkOdds() {
        return this.drinkOdds;
    }

    public void setDrinkOdds(double d) {
        this.drinkOdds = d;
    }

    public List<String> getDrinkRewardCommands() {
        return this.drinkRewardCommands;
    }

    public void setDrinkRewardCommands(List<String> list) {
        this.drinkRewardCommands = list;
    }

    public long getDrinkPrice() {
        return this.drinkPrice;
    }

    public void setDrinkPrice(long l) {
        this.drinkPrice = l;
    }

    public int getDrinkSlot() {
        return this.drinkSlot;
    }

    public void setDrinkSlot(int n) {
        this.drinkSlot = n;
    }

    public PotionMeta getDrinkMeta() {
        return this.drinkMeta;
    }

    public void setDrinkMeta(PotionMeta potionMeta) {
        this.drinkMeta = potionMeta;
    }

    public short getDrinkColor() {
        return this.drinkColor;
    }

    public void setDrinkColor(short s) {
        this.drinkColor = s;
    }

    public double getSuperLuckyOdds() {
        return this.superLuckyOdds;
    }

    public void setSuperLuckyOdds(double d) {
        this.superLuckyOdds = d;
    }

    public long getSuperLuckyReward() {
        return this.superLuckyReward;
    }

    public void setSuperLuckyReward(long l) {
        this.superLuckyReward = l;
    }

    public List<String> getSuperLuckyRewardCommands() {
        return this.superLuckyRewardCommands;
    }

    public void setSuperLuckyRewardCommands(List<String> list) {
        this.superLuckyRewardCommands = list;
    }
}


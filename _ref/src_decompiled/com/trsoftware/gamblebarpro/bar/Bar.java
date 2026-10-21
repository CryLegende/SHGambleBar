/*
 * Decompiled with CFR 0.152.
 */
package com.trsoftware.gamblebarpro.bar;

import com.trsoftware.gamblebarpro.bar.EconomyManager;

public class Bar {
    private String barId;
    private String barTitle;
    private int barSize;
    private EconomyManager.EconomyType economyType;

    public Bar(String string, String string2, int n) {
        this.setBarId(string);
        this.setBarTitle(string2);
        this.setBarSize(n);
    }

    public String getBarId() {
        return this.barId;
    }

    public void setBarId(String string) {
        this.barId = string;
    }

    public String getBarTitle() {
        return this.barTitle;
    }

    public void setBarTitle(String string) {
        this.barTitle = string;
    }

    public int getBarSize() {
        return this.barSize;
    }

    public void setBarSize(int n) {
        this.barSize = n;
    }

    public EconomyManager.EconomyType getEconomyType() {
        return this.economyType;
    }

    public void setEconomyType(EconomyManager.EconomyType economyType) {
        this.economyType = economyType;
    }
}


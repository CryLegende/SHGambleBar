/*
 * Decompiled with CFR 0.152.
 */
package com.trsoftware.gamblebarpro.bar;

import com.trsoftware.gamblebarpro.GambleBar;
import com.trsoftware.gamblebarpro.bar.Bar;

public class EconomyManager {
    public GambleBar plugin;

    public EconomyManager(GambleBar gambleBar) {
        this.plugin = gambleBar;
    }

    public void setEconomyTypes() {
        for (Bar bar : this.plugin.bm.bars) {
            if (this.plugin.getConfig().contains("bars." + bar.getBarId() + ".economyType")) {
                if (this.plugin.getConfig().getString("bars." + bar.getBarId() + ".economyType").equalsIgnoreCase("playerpoints")) {
                    bar.setEconomyType(EconomyType.PLAYERPOINTS);
                    continue;
                }
                bar.setEconomyType(EconomyType.VAULT);
                continue;
            }
            if (this.plugin.getConfig().getString("defaultEconomyType").equalsIgnoreCase("playerpoints")) {
                bar.setEconomyType(EconomyType.PLAYERPOINTS);
                continue;
            }
            bar.setEconomyType(EconomyType.VAULT);
        }
    }

    public static enum EconomyType {
        VAULT,
        PLAYERPOINTS;

    }
}


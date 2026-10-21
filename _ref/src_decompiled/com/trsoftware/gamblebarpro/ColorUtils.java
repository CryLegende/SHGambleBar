/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.md_5.bungee.api.ChatColor
 *  org.bukkit.ChatColor
 */
package com.trsoftware.gamblebarpro;

import com.trsoftware.gamblebarpro.GambleBar;
import org.bukkit.ChatColor;

public class ColorUtils {
    public static final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    public static String translateColorCodes(String string) {
        if (!GambleBar.isVersionAbove1165()) {
            return ChatColor.translateAlternateColorCodes((char)'&', (String)string);
        }
        String[] stringArray = string.split(String.format(WITH_DELIMITER, "&"));
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < stringArray.length; ++i) {
            if (stringArray[i].equalsIgnoreCase("&")) {
                if (stringArray[++i].charAt(0) == '#') {
                    stringBuilder.append(net.md_5.bungee.api.ChatColor.of((String)stringArray[i].substring(0, 7)) + stringArray[i].substring(7));
                    continue;
                }
                stringBuilder.append(ChatColor.translateAlternateColorCodes((char)'&', (String)("&" + stringArray[i])));
                continue;
            }
            stringBuilder.append(stringArray[i]);
        }
        return stringBuilder.toString();
    }
}


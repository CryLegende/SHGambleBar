package org.Cry_Legende.sHGambleBar.util;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Traduction des codes couleur.
 * Supporte les codes classiques (&a, &l, ...) et les couleurs hexadecimales
 * au format &#RRGGBB (celui utilise par l'ancienne config GambleBarPro).
 */
public final class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ColorUtils() {
    }

    public static String colorize(String input) {
        if (input == null) return null;

        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuilder builder = new StringBuilder(input.length());
        while (matcher.find()) {
            matcher.appendReplacement(builder, ChatColor.of("#" + matcher.group(1)).toString());
        }
        matcher.appendTail(builder);

        return ChatColor.translateAlternateColorCodes('&', builder.toString());
    }

    public static List<String> colorize(List<String> input) {
        if (input == null) return null;
        List<String> out = new ArrayList<>(input.size());
        for (String line : input) {
            out.add(colorize(line));
        }
        return out;
    }

    public static String stripColor(String input) {
        if (input == null) return null;
        return ChatColor.stripColor(colorize(input));
    }
}

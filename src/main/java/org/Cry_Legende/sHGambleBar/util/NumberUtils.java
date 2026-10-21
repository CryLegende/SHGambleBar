package org.Cry_Legende.sHGambleBar.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class NumberUtils {

    private static final DecimalFormat PLAIN;
    private static final DecimalFormat DECIMAL;
    private static final String[] SUFFIXES = {"", "K", "M", "B", "T", "Q"};

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        PLAIN = new DecimalFormat("#,##0", symbols);
        DECIMAL = new DecimalFormat("#,##0.##", symbols);
    }

    private NumberUtils() {
    }

    /**
     * 1234567 -> "1,234,567"
     */
    public static String format(long value) {
        return PLAIN.format(value);
    }

    /**
     * 1234.5 -> "1,234.5"
     */
    public static String format(double value) {
        return DECIMAL.format(value);
    }

    /**
     * 1234567 -> "1.23M"
     */
    public static String compact(double value) {
        boolean negative = value < 0;
        double abs = Math.abs(value);
        int index = 0;
        while (abs >= 1000 && index < SUFFIXES.length - 1) {
            abs /= 1000.0;
            index++;
        }
        String formatted = new DecimalFormat("#0.##").format(abs) + SUFFIXES[index];
        return negative ? "-" + formatted : formatted;
    }

    /**
     * Formate une duree en millisecondes -> "1h 20m 5s"
     */
    public static String formatDuration(long millis) {
        if (millis <= 0) return "0s";
        long totalSeconds = (millis + 999) / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append("s");
        return sb.toString().trim();
    }

    public static long parseLong(String raw, long fallback) {
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static int parseInt(String raw, int fallback) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

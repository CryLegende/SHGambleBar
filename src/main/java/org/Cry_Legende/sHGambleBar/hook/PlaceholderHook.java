package org.Cry_Legende.sHGambleBar.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.Cry_Legende.sHGambleBar.storage.Database;
import org.Cry_Legende.sHGambleBar.storage.PlayerStats;
import org.Cry_Legende.sHGambleBar.util.NumberUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/**
 * Expansion PlaceholderAPI : %shgamblebar_<placeholder>%
 * La liste complete est documentee dans config.yml.
 */
public class PlaceholderHook extends PlaceholderExpansion {

    private final SHGambleBar plugin;

    public PlaceholderHook(SHGambleBar plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "shgamblebar";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        String key = params.toLowerCase(Locale.ROOT);

        if (key.startsWith("server_")) {
            return NumberUtils.format(plugin.getStatsManager().getServerTotal(key.substring(7)));
        }
        if (key.startsWith("top_name_") || key.startsWith("top_value_")) {
            return top(key);
        }
        if (player == null) return "";

        PlayerStats stats = plugin.getStatsManager().get(player.getUniqueId());
        if (stats == null) return "0";

        return switch (key) {
            case "drinks" -> NumberUtils.format(stats.getDrinks());
            case "wins" -> NumberUtils.format(stats.getWins());
            case "super_wins" -> NumberUtils.format(stats.getSuperWins());
            case "losses" -> NumberUtils.format(stats.getLosses());
            case "winrate" -> NumberUtils.format(stats.getWinRate());
            case "wagered_money" -> NumberUtils.format(stats.getWageredMoney());
            case "won_money" -> NumberUtils.format(stats.getWonMoney());
            case "profit_money" -> NumberUtils.format(stats.getProfitMoney());
            case "wagered_exp" -> NumberUtils.format(stats.getWageredExp());
            case "won_exp" -> NumberUtils.format(stats.getWonExp());
            case "profit_exp" -> NumberUtils.format(stats.getProfitExp());
            case "biggest_win_money" -> NumberUtils.format(stats.getBiggestWinMoney());
            case "biggest_win_exp" -> NumberUtils.format(stats.getBiggestWinExp());
            case "streak" -> NumberUtils.format(stats.getWinStreak());
            case "lose_streak" -> NumberUtils.format(stats.getLoseStreak());
            case "best_streak" -> NumberUtils.format(stats.getBestStreak());
            default -> null;
        };
    }

    /**
     * top_name_<colonne>_<rang> / top_value_<colonne>_<rang>
     */
    private String top(String key) {
        boolean wantName = key.startsWith("top_name_");
        String rest = key.substring(wantName ? "top_name_".length() : "top_value_".length());

        int separator = rest.lastIndexOf('_');
        if (separator < 0) return "";

        String column = rest.substring(0, separator);
        int rank = NumberUtils.parseInt(rest.substring(separator + 1), 0);
        if (rank < 1) return "";

        List<Database.LeaderboardEntry> entries = plugin.getStatsManager().getTop(column, Math.max(rank, 10));
        if (entries.size() < rank) return wantName ? "-" : "0";

        Database.LeaderboardEntry entry = entries.get(rank - 1);
        return wantName ? entry.name() : NumberUtils.format(entry.value());
    }
}

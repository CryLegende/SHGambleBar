package org.Cry_Legende.sHGambleBar.currency;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.Cry_Legende.sHGambleBar.util.NumberUtils;
import org.bukkit.entity.Player;

/**
 * Monnaie basee sur les points d'experience bruts.
 * Bukkit n'expose pas directement le total de points : il faut le recalculer
 * depuis le niveau et la progression de la barre.
 */
public class ExpCurrency implements Currency {

    private final SHGambleBar plugin;

    public ExpCurrency(SHGambleBar plugin) {
        this.plugin = plugin;
    }

    @Override
    public CurrencyType getType() {
        return CurrencyType.EXP;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public long getBalance(Player player) {
        return getTotalExp(player);
    }

    @Override
    public boolean has(Player player, long amount) {
        return getTotalExp(player) >= amount;
    }

    @Override
    public boolean withdraw(Player player, long amount) {
        if (amount <= 0) return amount == 0;
        long total = getTotalExp(player);
        if (total < amount) return false;
        setTotalExp(player, total - amount);
        return true;
    }

    @Override
    public boolean deposit(Player player, long amount) {
        if (amount <= 0) return amount == 0;
        setTotalExp(player, getTotalExp(player) + amount);
        return true;
    }

    @Override
    public String format(long amount) {
        String suffix = plugin.getConfigManager().getString("currencies.exp.display-suffix", " XP");
        return NumberUtils.format(amount) + suffix;
    }

    // --- Conversion niveau <-> points ---

    /**
     * Points d'experience necessaires pour passer du niveau donne au suivant.
     */
    private static int expToNextLevel(int level) {
        if (level < 16) return 2 * level + 7;
        if (level < 31) return 5 * level - 38;
        return 9 * level - 158;
    }

    /**
     * Points d'experience cumules necessaires pour atteindre un niveau.
     */
    private static int expAtLevel(int level) {
        if (level <= 16) return (level * level) + (6 * level);
        if (level <= 31) return (int) (2.5 * level * level - 40.5 * level + 360);
        return (int) (4.5 * level * level - 162.5 * level + 2220);
    }

    public static int getTotalExp(Player player) {
        return expAtLevel(player.getLevel())
            + Math.round(player.getExp() * expToNextLevel(player.getLevel()));
    }

    public static void setTotalExp(Player player, long total) {
        long safe = Math.max(0, total);

        player.setExp(0f);
        player.setLevel(0);
        player.setTotalExperience(0);

        int level = 0;
        long remaining = safe;
        while (remaining >= expToNextLevel(level)) {
            remaining -= expToNextLevel(level);
            level++;
        }

        player.setLevel(level);
        player.setExp((float) remaining / expToNextLevel(level));
        player.setTotalExperience((int) Math.min(Integer.MAX_VALUE, safe));
    }
}

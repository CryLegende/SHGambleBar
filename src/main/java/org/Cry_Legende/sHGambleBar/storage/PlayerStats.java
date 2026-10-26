package org.Cry_Legende.sHGambleBar.storage;

import org.Cry_Legende.sHGambleBar.currency.CurrencyType;

import java.util.UUID;

/**
 * Statistiques cumulees d'un joueur.
 */
public class PlayerStats {

    private final UUID uuid;
    private String name;

    private long drinks;
    private long wins;
    private long superWins;
    private long losses;

    private long wageredMoney;
    private long wonMoney;
    private long wageredExp;
    private long wonExp;

    private long biggestWinMoney;
    private long biggestWinExp;

    private long currentStreak;
    private long bestStreak;
    private long lastPlayed;

    private boolean dirty;

    public PlayerStats(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.equals(this.name)) {
            this.name = name;
            this.dirty = true;
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    public void markDirty() {
        this.dirty = true;
    }

    /**
     * Enregistre une partie.
     */
    public void record(CurrencyType currency, long price, long payout, boolean won, boolean superWin) {
        drinks++;
        if (won) {
            wins++;
            if (superWin) superWins++;
            currentStreak = currentStreak < 0 ? 1 : currentStreak + 1;
            if (currentStreak > bestStreak) bestStreak = currentStreak;
        } else {
            losses++;
            currentStreak = currentStreak > 0 ? -1 : currentStreak - 1;
        }

        if (currency == CurrencyType.EXP) {
            wageredExp += price;
            wonExp += payout;
            if (payout > biggestWinExp) biggestWinExp = payout;
        } else {
            wageredMoney += price;
            wonMoney += payout;
            if (payout > biggestWinMoney) biggestWinMoney = payout;
        }

        lastPlayed = System.currentTimeMillis();
        dirty = true;
    }

    public long getDrinks() {
        return drinks;
    }

    public long getWins() {
        return wins;
    }

    public long getSuperWins() {
        return superWins;
    }

    public long getLosses() {
        return losses;
    }

    public long getWageredMoney() {
        return wageredMoney;
    }

    public long getWonMoney() {
        return wonMoney;
    }

    public long getWageredExp() {
        return wageredExp;
    }

    public long getWonExp() {
        return wonExp;
    }

    public long getBiggestWinMoney() {
        return biggestWinMoney;
    }

    public long getBiggestWinExp() {
        return biggestWinExp;
    }

    /**
     * Serie en cours : positive pour des victoires, negative pour des defaites.
     */
    public long getCurrentStreak() {
        return currentStreak;
    }

    public long getWinStreak() {
        return Math.max(0, currentStreak);
    }

    public long getLoseStreak() {
        return Math.max(0, -currentStreak);
    }

    public long getBestStreak() {
        return bestStreak;
    }

    public long getLastPlayed() {
        return lastPlayed;
    }

    public long getProfitMoney() {
        return wonMoney - wageredMoney;
    }

    public long getProfitExp() {
        return wonExp - wageredExp;
    }

    public double getWinRate() {
        return drinks == 0 ? 0.0 : (wins * 100.0) / drinks;
    }

    /**
     * Copie figee, prise sur le thread principal, pour une ecriture asynchrone.
     */
    public PlayerStats snapshot() {
        PlayerStats copy = new PlayerStats(uuid, name);
        copy.restore(drinks, wins, superWins, losses, wageredMoney, wonMoney, wageredExp, wonExp,
            biggestWinMoney, biggestWinExp, currentStreak, bestStreak, lastPlayed);
        return copy;
    }

    /**
     * Ajoute aux stats chargees depuis la base les parties jouees avant la fin du chargement
     * (enregistrees dans une entree vide creee entre-temps), au lieu de les ecraser.
     */
    void absorb(PlayerStats session) {
        drinks += session.drinks;
        wins += session.wins;
        superWins += session.superWins;
        losses += session.losses;
        wageredMoney += session.wageredMoney;
        wonMoney += session.wonMoney;
        wageredExp += session.wageredExp;
        wonExp += session.wonExp;
        biggestWinMoney = Math.max(biggestWinMoney, session.biggestWinMoney);
        biggestWinExp = Math.max(biggestWinExp, session.biggestWinExp);
        if (session.drinks > 0) {
            // La serie de la session prolonge celle de la base si elle ne contient que des victoires
            // (ou que des defaites) dans le meme sens.
            boolean allWins = session.wins == session.drinks;
            boolean allLosses = session.losses == session.drinks;
            if ((allWins && currentStreak > 0) || (allLosses && currentStreak < 0)) {
                currentStreak += session.currentStreak;
            } else {
                currentStreak = session.currentStreak;
            }
        }
        bestStreak = Math.max(Math.max(bestStreak, session.bestStreak), currentStreak);
        lastPlayed = Math.max(lastPlayed, session.lastPlayed);
        dirty = true;
    }

    void restore(long drinks, long wins, long superWins, long losses, long wageredMoney, long wonMoney,
                 long wageredExp, long wonExp, long biggestWinMoney, long biggestWinExp,
                 long currentStreak, long bestStreak, long lastPlayed) {
        this.drinks = drinks;
        this.wins = wins;
        this.superWins = superWins;
        this.losses = losses;
        this.wageredMoney = wageredMoney;
        this.wonMoney = wonMoney;
        this.wageredExp = wageredExp;
        this.wonExp = wonExp;
        this.biggestWinMoney = biggestWinMoney;
        this.biggestWinExp = biggestWinExp;
        this.currentStreak = currentStreak;
        this.bestStreak = bestStreak;
        this.lastPlayed = lastPlayed;
        this.dirty = false;
    }
}

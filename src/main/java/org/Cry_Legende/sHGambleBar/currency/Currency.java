package org.Cry_Legende.sHGambleBar.currency;

import org.bukkit.entity.Player;

/**
 * Abstraction d'une monnaie misable.
 */
public interface Currency {

    CurrencyType getType();

    /**
     * Le backend est-il utilisable (Vault trouve, provider present, ...) ?
     */
    boolean isAvailable();

    long getBalance(Player player);

    boolean has(Player player, long amount);

    boolean withdraw(Player player, long amount);

    boolean deposit(Player player, long amount);

    /**
     * Formatte un montant pour l'affichage (avec le suffixe/symbole configure).
     */
    String format(long amount);
}

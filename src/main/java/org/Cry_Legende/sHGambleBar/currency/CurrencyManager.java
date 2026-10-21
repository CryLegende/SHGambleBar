package org.Cry_Legende.sHGambleBar.currency;

import org.Cry_Legende.sHGambleBar.SHGambleBar;

import java.util.EnumMap;
import java.util.Map;

/**
 * Point d'entree unique pour recuperer une monnaie.
 */
public class CurrencyManager {

    private final Map<CurrencyType, Currency> currencies = new EnumMap<>(CurrencyType.class);
    private final VaultCurrency vault;

    public CurrencyManager(SHGambleBar plugin) {
        this.vault = new VaultCurrency(plugin);
        this.vault.setup();
        currencies.put(CurrencyType.VAULT, vault);
        currencies.put(CurrencyType.EXP, new ExpCurrency(plugin));
    }

    public Currency get(CurrencyType type) {
        return currencies.get(type);
    }

    public VaultCurrency getVault() {
        return vault;
    }

    /**
     * Retente le hook Vault (utile apres un /shgamblebar reload).
     */
    public void reload() {
        vault.setup();
    }
}

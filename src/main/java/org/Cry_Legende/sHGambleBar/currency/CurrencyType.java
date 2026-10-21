package org.Cry_Legende.sHGambleBar.currency;

import java.util.Locale;

public enum CurrencyType {

    /**
     * Monnaie du serveur via Vault (SHCoins).
     */
    VAULT,
    /**
     * Points d'experience bruts.
     */
    EXP;

    public static CurrencyType parse(String raw, CurrencyType fallback) {
        if (raw == null) return fallback;
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}

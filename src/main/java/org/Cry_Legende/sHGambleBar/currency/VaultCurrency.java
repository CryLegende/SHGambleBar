package org.Cry_Legende.sHGambleBar.currency;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.Cry_Legende.sHGambleBar.util.NumberUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Monnaie serveur via Vault.
 */
public class VaultCurrency implements Currency {

    private final SHGambleBar plugin;
    private Economy economy;

    public VaultCurrency(SHGambleBar plugin) {
        this.plugin = plugin;
    }

    /**
     * @return true si le hook Vault a reussi.
     */
    public boolean setup() {
        economy = null;

        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault introuvable : les fioles en monnaie serveur seront indisponibles.");
            return false;
        }
        RegisteredServiceProvider<Economy> provider = plugin.getServer().getServicesManager()
            .getRegistration(Economy.class);
        if (provider == null || provider.getProvider() == null) {
            plugin.getLogger().warning("Aucun plugin d'economie enregistre aupres de Vault : "
                + "les fioles en monnaie serveur seront indisponibles.");
            return false;
        }
        economy = provider.getProvider();
        plugin.getLogger().info("Hook Vault reussi (" + economy.getName() + ").");
        return true;
    }

    public String getProviderName() {
        return economy == null ? "aucun" : economy.getName();
    }

    @Override
    public CurrencyType getType() {
        return CurrencyType.VAULT;
    }

    @Override
    public boolean isAvailable() {
        return economy != null;
    }

    @Override
    public long getBalance(Player player) {
        if (!isAvailable()) return 0L;
        return (long) economy.getBalance(player);
    }

    @Override
    public boolean has(Player player, long amount) {
        if (!isAvailable()) return false;
        return economy.has(player, amount);
    }

    @Override
    public boolean withdraw(Player player, long amount) {
        if (!isAvailable() || amount <= 0) return amount == 0;
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        if (!response.transactionSuccess()) {
            plugin.getLogger().warning("Retrait de " + amount + " echoue pour " + player.getName()
                + " : " + response.errorMessage);
        }
        return response.transactionSuccess();
    }

    @Override
    public boolean deposit(Player player, long amount) {
        if (!isAvailable() || amount <= 0) return amount == 0;
        EconomyResponse response = economy.depositPlayer(player, amount);
        if (!response.transactionSuccess()) {
            plugin.getLogger().warning("Depot de " + amount + " echoue pour " + player.getName()
                + " : " + response.errorMessage);
        }
        return response.transactionSuccess();
    }

    @Override
    public String format(long amount) {
        String suffix = plugin.getConfigManager().getString("currencies.vault.display-suffix", "");
        return NumberUtils.format(amount) + suffix;
    }
}

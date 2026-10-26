package org.Cry_Legende.sHGambleBar;

import org.Cry_Legende.sHGambleBar.animation.AnimationManager;
import org.Cry_Legende.sHGambleBar.animation.RouletteHolder;
import org.Cry_Legende.sHGambleBar.bar.BarHolder;
import org.Cry_Legende.sHGambleBar.bar.BarManager;
import org.Cry_Legende.sHGambleBar.command.GambleBarCommand;
import org.Cry_Legende.sHGambleBar.config.ConfigManager;
import org.Cry_Legende.sHGambleBar.config.MessageManager;
import org.Cry_Legende.sHGambleBar.currency.CurrencyManager;
import org.Cry_Legende.sHGambleBar.effect.EffectManager;
import org.Cry_Legende.sHGambleBar.game.CooldownManager;
import org.Cry_Legende.sHGambleBar.game.GambleService;
import org.Cry_Legende.sHGambleBar.hook.PlaceholderHook;
import org.Cry_Legende.sHGambleBar.listener.BarListener;
import org.Cry_Legende.sHGambleBar.listener.DrinkListener;
import org.Cry_Legende.sHGambleBar.listener.PlayerListener;
import org.Cry_Legende.sHGambleBar.listener.RouletteListener;
import org.Cry_Legende.sHGambleBar.storage.StatsManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

public final class SHGambleBar extends JavaPlugin {

    private ConfigManager configManager;
    private MessageManager messageManager;
    private CurrencyManager currencyManager;
    private EffectManager effectManager;
    private BarManager barManager;
    private StatsManager statsManager;
    private CooldownManager cooldownManager;
    private AnimationManager animationManager;
    private GambleService gambleService;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.currencyManager = new CurrencyManager(this);
        this.effectManager = new EffectManager(this);
        this.barManager = new BarManager(this);
        this.statsManager = new StatsManager(this);
        this.cooldownManager = new CooldownManager(this);
        this.animationManager = new AnimationManager(this);
        this.gambleService = new GambleService(this);

        registerCommand();
        registerListeners();
        hookPlaceholderAPI();
        scheduleAutoSave();

        // Rechargement a chaud : les joueurs deja connectes doivent avoir leurs stats.
        for (Player player : getServer().getOnlinePlayers()) {
            statsManager.loadAsync(player);
        }

        getLogger().info("SHGambleBar active.");
    }

    @Override
    public void onDisable() {
        if (animationManager != null) animationManager.shutdown();
        closePluginInventories();
        if (statsManager != null) statsManager.saveAllSync();
        getLogger().info("SHGambleBar desactive.");
    }

    /**
     * Une fois le plugin desactive, plus rien n'annule les clics : un bar ou une roulette reste
     * ouvert permettrait de prendre les fioles (identifiees par leur NBT, donc encore valides apres
     * un rechargement) ou les symboles. On les ferme donc avant de s'arreter.
     */
    private void closePluginInventories() {
        for (Player player : getServer().getOnlinePlayers()) {
            InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder(false);
            if (holder instanceof BarHolder || holder instanceof RouletteHolder) {
                player.closeInventory();
            }
        }
    }

    private void registerCommand() {
        PluginCommand command = getCommand("shgamblebar");
        if (command == null) {
            getLogger().severe("Commande 'shgamblebar' introuvable dans plugin.yml.");
            return;
        }
        GambleBarCommand executor = new GambleBarCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new BarListener(this), this);
        getServer().getPluginManager().registerEvents(new DrinkListener(this), this);
        getServer().getPluginManager().registerEvents(new RouletteListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    private void hookPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().info("PlaceholderAPI absent : les placeholders %shgamblebar_...% seront indisponibles.");
            return;
        }
        new PlaceholderHook(this).register();
        getLogger().info("Hook PlaceholderAPI actif.");
    }

    private void scheduleAutoSave() {
        int minutes = configManager.getInt("storage.auto-save-minutes", 5);
        if (minutes <= 0) return;
        long ticks = minutes * 60L * 20L;
        getServer().getScheduler().runTaskTimer(this, () -> statsManager.saveDirtyAsync(), ticks, ticks);
    }

    /**
     * Rechargement complet (commande /shgamblebar reload).
     */
    public void reloadAll() {
        configManager.reload();
        messageManager.reload();
        currencyManager.reload();
        effectManager.reload();
        barManager.reload();
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    public EffectManager getEffectManager() {
        return effectManager;
    }

    public BarManager getBarManager() {
        return barManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    public GambleService getGambleService() {
        return gambleService;
    }
}

package org.Cry_Legende.sHGambleBar.config;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Locale;

/**
 * Acces centralise a config.yml.
 */
public class ConfigManager {

    private final SHGambleBar plugin;
    private FileConfiguration config;

    public ConfigManager(SHGambleBar plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }

    public FileConfiguration raw() {
        return config;
    }

    public ConfigurationSection section(String path) {
        return config.getConfigurationSection(path);
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public long getLong(String path, long def) {
        return config.getLong(path, def);
    }

    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    // --- Raccourcis frequemment utilises ---

    public boolean isDebug() {
        return getBoolean("debug", false);
    }

    public String getDefaultBar() {
        return getString("default-bar", "mainbar");
    }

    public boolean isAutoCloseBar() {
        return getBoolean("gui.auto-close-on-purchase", true);
    }

    public boolean isGiveItem() {
        return getBoolean("gui.give-item-on-purchase", true);
    }

    public boolean isInstantGamble() {
        return getBoolean("gui.instant-gamble-on-purchase", false);
    }

    public List<String> getBlacklistedWorlds() {
        return getStringList("blacklisted-worlds");
    }

    public boolean isWorldBlacklisted(String worldName) {
        for (String world : getBlacklistedWorlds()) {
            if (world.equalsIgnoreCase(worldName)) return true;
        }
        return false;
    }

    public boolean isMainHandOnly() {
        return getBoolean("drink.main-hand-only", true);
    }

    public int getCooldownSeconds() {
        return getInt("cooldown.seconds", 0);
    }

    public boolean isIndividualCooldown() {
        return getBoolean("cooldown.per-drink", false);
    }

    public int getMinPlaytimeMinutes() {
        return getInt("requirements.min-playtime-minutes", 0);
    }

    public boolean isLegacyEnabled() {
        return getBoolean("legacy-compatibility.enabled", true);
    }

    public boolean isLegacyMatchLore() {
        return getBoolean("legacy-compatibility.match-lore", true);
    }

    public boolean isLegacyConvert() {
        return getBoolean("legacy-compatibility.convert-on-detection", true);
    }

    public boolean isLegacyLog() {
        return getBoolean("legacy-compatibility.log-detections", false);
    }

    public String getStorageType() {
        return getString("storage.type", "SQLITE").toUpperCase(Locale.ROOT);
    }

    public String getStorageFile() {
        return getString("storage.sqlite.file", "data.db");
    }

    public boolean isStatsEnabled() {
        return getBoolean("storage.track-stats", true);
    }

    public int getTopCacheSeconds() {
        return getInt("storage.leaderboard-cache-seconds", 60);
    }
}

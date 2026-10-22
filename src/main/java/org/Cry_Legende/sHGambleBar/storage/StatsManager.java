package org.Cry_Legende.sHGambleBar.storage;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Cache memoire des statistiques des joueurs connectes + acces aux classements.
 * Les ecritures disque sont toujours asynchrones.
 */
public class StatsManager {

    private final SHGambleBar plugin;
    private final Database database;
    private final Map<UUID, PlayerStats> cache = new ConcurrentHashMap<>();

    private final Map<String, List<Database.LeaderboardEntry>> leaderboards = new ConcurrentHashMap<>();
    private final Map<String, Long> leaderboardExpiry = new ConcurrentHashMap<>();
    private final Map<String, Long> serverTotals = new ConcurrentHashMap<>();
    private long serverTotalsExpiry;

    private boolean available;

    public StatsManager(SHGambleBar plugin) {
        this.plugin = plugin;
        this.database = new Database(plugin);

        if (!plugin.getConfigManager().isStatsEnabled()) {
            plugin.getLogger().info("Suivi des statistiques desactive dans la config.");
            this.available = false;
            return;
        }

        try {
            database.initialize();
            this.available = true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Initialisation de la base echouee : "
                + "les statistiques et placeholders seront indisponibles.", e);
            this.available = false;
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public Database getDatabase() {
        return database;
    }

    public void loadAsync(Player player) {
        if (!available) return;
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerStats stats = database.load(uuid, name);
            stats.setName(name);
            cache.put(uuid, stats);
        });
    }

    public void unloadAsync(UUID uuid) {
        if (!available) return;
        PlayerStats stats = cache.remove(uuid);
        if (stats == null || !stats.isDirty()) return;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.save(stats));
    }

    /**
     * @return les stats en cache, ou null si elles ne sont pas encore chargees.
     */
    public PlayerStats get(UUID uuid) {
        return cache.get(uuid);
    }

    /**
     * Recupere les stats du joueur en creant une entree vide au besoin.
     */
    public PlayerStats getOrCreate(Player player) {
        return cache.computeIfAbsent(player.getUniqueId(),
            uuid -> new PlayerStats(uuid, player.getName()));
    }

    public void saveDirtyAsync() {
        if (!available) return;
        List<PlayerStats> dirty = new ArrayList<>();
        for (PlayerStats stats : cache.values()) {
            if (stats.isDirty()) dirty.add(stats);
        }
        if (dirty.isEmpty()) return;
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.saveAll(dirty));
    }

    /**
     * Sauvegarde bloquante : utilisee uniquement au onDisable.
     */
    public void saveAllSync() {
        if (!available) return;
        List<PlayerStats> dirty = new ArrayList<>();
        for (PlayerStats stats : cache.values()) {
            if (stats.isDirty()) dirty.add(stats);
        }
        database.saveAll(dirty);
        database.close();
    }

    // --- Classements et totaux (mis en cache pour ne pas taper la base a chaque placeholder) ---

    public List<Database.LeaderboardEntry> getTop(String column, int limit) {
        if (!available || !Database.isValidColumn(column)) return List.of();

        String key = column + ":" + limit;
        long now = System.currentTimeMillis();
        Long expiry = leaderboardExpiry.get(key);

        if (expiry == null || expiry < now) {
            leaderboardExpiry.put(key, now + plugin.getConfigManager().getTopCacheSeconds() * 1000L);
            if (leaderboards.containsKey(key)) {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                    () -> leaderboards.put(key, database.getTop(column, limit)));
            } else {
                // Premier appel : sans chargement immédiat, le placeholder afficherait « - » jusqu'au rafraîchissement
                leaderboards.put(key, database.getTop(column, limit));
            }
        }
        return leaderboards.getOrDefault(key, List.of());
    }

    public long getServerTotal(String column) {
        if (!available || !Database.isValidColumn(column)) return 0L;

        long now = System.currentTimeMillis();
        if (serverTotalsExpiry < now) {
            serverTotalsExpiry = now + plugin.getConfigManager().getTopCacheSeconds() * 1000L;
            Runnable load = () -> {
                Map<String, Long> totals = new HashMap<>();
                for (String col : new String[]{"drinks", "wins", "super_wins", "losses",
                    "wagered_money", "won_money", "wagered_exp", "won_exp"}) {
                    totals.put(col, database.getServerTotal(col));
                }
                serverTotals.putAll(totals);
            };
            // Premier appel : chargement immédiat, sinon le placeholder afficherait 0
            if (serverTotals.isEmpty()) {
                load.run();
            } else {
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, load);
            }
        }
        return serverTotals.getOrDefault(column, 0L);
    }
}

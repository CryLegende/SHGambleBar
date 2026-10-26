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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Cache memoire des statistiques des joueurs connectes + acces aux classements.
 * Les ecritures disque sont toujours asynchrones.
 */
public class StatsManager {

    private final SHGambleBar plugin;
    private final Database database;
    private final Map<UUID, PlayerStats> cache = new ConcurrentHashMap<>();
    // Chargements et sauvegardes des joueurs passent par ce fil unique, donc dans l'ordre : une
    // sauvegarde au quit est terminee avant le rechargement d'un joueur qui revient, et deux
    // sauvegardes du meme joueur ne peuvent plus s'ecrire dans le desordre.
    private final ExecutorService io = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "SHGambleBar-Stats");
        thread.setDaemon(true);
        return thread;
    });

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
        io.execute(() -> {
            PlayerStats stats = database.load(uuid, name);
            stats.setName(name);
            if (!plugin.isEnabled()) return;
            // Mise en cache sur le thread principal : un joueur deja reparti n'est plus garde en
            // memoire indefiniment, et une partie jouee pendant le chargement n'est plus ecrasee.
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                PlayerStats current = cache.get(uuid);
                if (current != null && current.isDirty()) {
                    stats.absorb(current);
                }
                cache.put(uuid, stats);
            });
        });
    }

    public void unloadAsync(UUID uuid) {
        if (!available) return;
        PlayerStats stats = cache.remove(uuid);
        if (stats == null || !stats.isDirty()) return;
        io.execute(() -> database.save(stats));
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
        // Copies prises ici, sur le thread principal : l'ecriture ne lit plus des stats en cours de
        // modification, et une partie jouee pendant l'ecriture n'est plus marquee sauvegardee a tort.
        Map<PlayerStats, PlayerStats> dirty = new HashMap<>();
        for (PlayerStats stats : cache.values()) {
            if (stats.isDirty()) {
                dirty.put(stats, stats.snapshot());
                stats.clearDirty();
            }
        }
        if (dirty.isEmpty()) return;
        io.execute(() -> {
            List<PlayerStats> failed = new ArrayList<>();
            dirty.forEach((stats, snapshot) -> {
                if (!database.save(snapshot)) failed.add(stats);
            });
            if (!failed.isEmpty() && plugin.isEnabled()) {
                // Echec : on retentera a la prochaine sauvegarde
                plugin.getServer().getScheduler().runTask(plugin, () -> failed.forEach(PlayerStats::markDirty));
            }
        });
    }

    /**
     * Sauvegarde bloquante : utilisee uniquement au onDisable.
     */
    public void saveAllSync() {
        // Laisse finir les ecritures en attente avant la sauvegarde finale et la fermeture de la base
        io.shutdown();
        try {
            if (!io.awaitTermination(10, TimeUnit.SECONDS)) {
                plugin.getLogger().warning("Sauvegardes de stats en attente non terminees apres 10 s.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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

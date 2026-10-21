package org.Cry_Legende.sHGambleBar.storage;

import org.Cry_Legende.sHGambleBar.SHGambleBar;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Stockage SQLite des statistiques.
 * Toutes les methodes bloquantes doivent etre appelees de facon asynchrone.
 */
public class Database {

    private final SHGambleBar plugin;
    private final String url;
    private Connection connection;

    public Database(SHGambleBar plugin) {
        this.plugin = plugin;
        File file = new File(plugin.getDataFolder(), plugin.getConfigManager().getStorageFile());
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.url = "jdbc:sqlite:" + file.getAbsolutePath();
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver SQLite introuvable.", e);
            }
            connection = DriverManager.getConnection(url);
        }
        return connection;
    }

    public void initialize() throws SQLException {
        try (Statement statement = getConnection().createStatement()) {
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_stats (
                    uuid TEXT PRIMARY KEY,
                    name TEXT,
                    drinks INTEGER NOT NULL DEFAULT 0,
                    wins INTEGER NOT NULL DEFAULT 0,
                    super_wins INTEGER NOT NULL DEFAULT 0,
                    losses INTEGER NOT NULL DEFAULT 0,
                    wagered_money INTEGER NOT NULL DEFAULT 0,
                    won_money INTEGER NOT NULL DEFAULT 0,
                    wagered_exp INTEGER NOT NULL DEFAULT 0,
                    won_exp INTEGER NOT NULL DEFAULT 0,
                    biggest_win_money INTEGER NOT NULL DEFAULT 0,
                    biggest_win_exp INTEGER NOT NULL DEFAULT 0,
                    current_streak INTEGER NOT NULL DEFAULT 0,
                    best_streak INTEGER NOT NULL DEFAULT 0,
                    last_played INTEGER NOT NULL DEFAULT 0
                )""");
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Fermeture de la base echouee.", e);
        }
    }

    public PlayerStats load(UUID uuid, String name) {
        String sql = "SELECT * FROM player_stats WHERE uuid = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            try (ResultSet result = statement.executeQuery()) {
                PlayerStats stats = new PlayerStats(uuid, name);
                if (result.next()) {
                    stats.restore(
                        result.getLong("drinks"),
                        result.getLong("wins"),
                        result.getLong("super_wins"),
                        result.getLong("losses"),
                        result.getLong("wagered_money"),
                        result.getLong("won_money"),
                        result.getLong("wagered_exp"),
                        result.getLong("won_exp"),
                        result.getLong("biggest_win_money"),
                        result.getLong("biggest_win_exp"),
                        result.getLong("current_streak"),
                        result.getLong("best_streak"),
                        result.getLong("last_played"));
                }
                return stats;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Chargement des stats de " + uuid + " echoue.", e);
            return new PlayerStats(uuid, name);
        }
    }

    public void save(PlayerStats stats) {
        String sql = """
            INSERT INTO player_stats (uuid, name, drinks, wins, super_wins, losses, wagered_money,
                won_money, wagered_exp, won_exp, biggest_win_money, biggest_win_exp,
                current_streak, best_streak, last_played)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            ON CONFLICT(uuid) DO UPDATE SET
                name = excluded.name,
                drinks = excluded.drinks,
                wins = excluded.wins,
                super_wins = excluded.super_wins,
                losses = excluded.losses,
                wagered_money = excluded.wagered_money,
                won_money = excluded.won_money,
                wagered_exp = excluded.wagered_exp,
                won_exp = excluded.won_exp,
                biggest_win_money = excluded.biggest_win_money,
                biggest_win_exp = excluded.biggest_win_exp,
                current_streak = excluded.current_streak,
                best_streak = excluded.best_streak,
                last_played = excluded.last_played""";

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, stats.getUuid().toString());
            statement.setString(2, stats.getName());
            statement.setLong(3, stats.getDrinks());
            statement.setLong(4, stats.getWins());
            statement.setLong(5, stats.getSuperWins());
            statement.setLong(6, stats.getLosses());
            statement.setLong(7, stats.getWageredMoney());
            statement.setLong(8, stats.getWonMoney());
            statement.setLong(9, stats.getWageredExp());
            statement.setLong(10, stats.getWonExp());
            statement.setLong(11, stats.getBiggestWinMoney());
            statement.setLong(12, stats.getBiggestWinExp());
            statement.setLong(13, stats.getCurrentStreak());
            statement.setLong(14, stats.getBestStreak());
            statement.setLong(15, stats.getLastPlayed());
            statement.executeUpdate();
            stats.clearDirty();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Sauvegarde des stats de " + stats.getUuid() + " echouee.", e);
        }
    }

    public void saveAll(List<PlayerStats> all) {
        for (PlayerStats stats : all) {
            save(stats);
        }
    }

    /**
     * @return les totaux serveur, dans l'ordre des colonnes demandees.
     */
    public long getServerTotal(String column) {
        if (!isValidColumn(column)) return 0L;
        try (Statement statement = getConnection().createStatement();
             ResultSet result = statement.executeQuery("SELECT COALESCE(SUM(" + column + "), 0) FROM player_stats")) {
            return result.next() ? result.getLong(1) : 0L;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Calcul du total serveur echoue (" + column + ").", e);
            return 0L;
        }
    }

    public List<LeaderboardEntry> getTop(String column, int limit) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        if (!isValidColumn(column)) return entries;

        String sql = "SELECT name, " + column + " AS value FROM player_stats "
            + "WHERE " + column + " > 0 ORDER BY value DESC LIMIT ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    entries.add(new LeaderboardEntry(result.getString("name"), result.getLong("value")));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Calcul du classement echoue (" + column + ").", e);
        }
        return entries;
    }

    /**
     * Liste blanche : evite toute injection dans les noms de colonnes dynamiques.
     */
    public static boolean isValidColumn(String column) {
        return switch (column) {
            case "drinks", "wins", "super_wins", "losses", "wagered_money", "won_money",
                 "wagered_exp", "won_exp", "biggest_win_money", "biggest_win_exp",
                 "current_streak", "best_streak" -> true;
            default -> false;
        };
    }

    public record LeaderboardEntry(String name, long value) {
    }
}

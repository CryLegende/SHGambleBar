package org.Cry_Legende.sHGambleBar.game;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.Cry_Legende.sHGambleBar.bar.Drink;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cooldowns entre deux consommations. En memoire uniquement : ils sont
 * volontairement reinitialises au redemarrage du serveur.
 */
public class CooldownManager {

    private static final String GLOBAL_KEY = "*";

    private final SHGambleBar plugin;
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public CooldownManager(SHGambleBar plugin) {
        this.plugin = plugin;
    }

    /**
     * @return la duree du cooldown applicable a cette fiole, en secondes (0 = desactive).
     */
    public int getCooldownSeconds(Drink drink) {
        if (drink != null && drink.getCooldownSeconds() >= 0) {
            return drink.getCooldownSeconds();
        }
        return plugin.getConfigManager().getCooldownSeconds();
    }

    private String key(Drink drink) {
        return plugin.getConfigManager().isIndividualCooldown() ? drink.getKey() : GLOBAL_KEY;
    }

    /**
     * @return le temps restant en millisecondes, 0 si le joueur peut boire.
     */
    public long getRemaining(Player player, Drink drink) {
        if (player.hasPermission("shgamblebar.bypass.cooldown")) return 0L;
        int seconds = getCooldownSeconds(drink);
        if (seconds <= 0) return 0L;

        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return 0L;

        Long expiry = playerCooldowns.get(key(drink));
        if (expiry == null) return 0L;

        long remaining = expiry - System.currentTimeMillis();
        return Math.max(0L, remaining);
    }

    public void apply(Player player, Drink drink) {
        if (player.hasPermission("shgamblebar.bypass.cooldown")) return;
        int seconds = getCooldownSeconds(drink);
        if (seconds <= 0) return;

        cooldowns.computeIfAbsent(player.getUniqueId(), uuid -> new HashMap<>())
            .put(key(drink), System.currentTimeMillis() + seconds * 1000L);
    }

    public void clear(UUID uuid) {
        cooldowns.remove(uuid);
    }

    /**
     * Deconnexion : oublie les cooldowns ecoules. Ceux encore en cours sont gardes, pour qu'une
     * reconnexion ne permette pas de les contourner.
     */
    public void forgetExpired(UUID uuid) {
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) return;
        long now = System.currentTimeMillis();
        playerCooldowns.values().removeIf(expiry -> expiry <= now);
        if (playerCooldowns.isEmpty()) cooldowns.remove(uuid);
    }

    public void clearAll() {
        cooldowns.clear();
    }
}

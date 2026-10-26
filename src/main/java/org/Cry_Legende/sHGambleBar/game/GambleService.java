package org.Cry_Legende.sHGambleBar.game;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.Cry_Legende.sHGambleBar.action.ActionRunner;
import org.Cry_Legende.sHGambleBar.animation.DrinkAnimation;
import org.Cry_Legende.sHGambleBar.bar.Drink;
import org.Cry_Legende.sHGambleBar.currency.Currency;
import org.Cry_Legende.sHGambleBar.storage.PlayerStats;
import org.Cry_Legende.sHGambleBar.util.ColorUtils;
import org.Cry_Legende.sHGambleBar.util.NumberUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Coeur du plugin : achat d'une fiole, tirage du resultat, animation puis
 * application des gains. Le resultat est toujours tire AVANT l'animation :
 * celle-ci ne fait que le mettre en scene.
 */
public class GambleService {

    private final SHGambleBar plugin;
    private final Set<UUID> gambling = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> frozenUntil = new ConcurrentHashMap<>();

    public GambleService(SHGambleBar plugin) {
        this.plugin = plugin;
    }

    // --- Achat au bar ---

    /**
     * @return true si l'achat a abouti.
     */
    public boolean purchase(Player player, Drink drink) {
        if (!checkWorld(player)) return false;
        if (!checkPermission(player, drink)) return false;

        Currency currency = plugin.getCurrencyManager().get(drink.getCurrency());
        if (!currency.isAvailable()) {
            plugin.getMessageManager().send(player, "currency-unavailable",
                Map.of("currency", drink.getCurrency().name()));
            return false;
        }

        boolean instant = plugin.getConfigManager().isInstantGamble();
        if (instant && !checkCooldown(player, drink)) return false;

        boolean needsSlot = !instant && plugin.getConfigManager().isGiveItem();
        if (needsSlot && player.getInventory().firstEmpty() == -1) {
            plugin.getMessageManager().send(player, "full-inventory", placeholders(player, drink, null));
            if (plugin.getConfigManager().isAutoCloseBar()) player.closeInventory();
            return false;
        }

        long price = drink.getPrice();
        if (price > 0 && !currency.has(player, price)) {
            plugin.getMessageManager().send(player, "not-enough", placeholders(player, drink,
                Map.of("needed", currency.format(price),
                    "balance", currency.format(currency.getBalance(player)))));
            return false;
        }
        if (price > 0 && !currency.withdraw(player, price)) {
            plugin.getMessageManager().send(player, "transaction-failed", placeholders(player, drink, null));
            return false;
        }

        plugin.getMessageManager().send(player, "purchase", placeholders(player, drink,
            Map.of("balance", currency.format(currency.getBalance(player)))));
        plugin.getEffectManager().play(player, plugin.getConfigManager()
            .getString("gui.purchase-effect", "purchase"));

        if (instant) {
            startGamble(player, drink);
            return true;
        }

        if (plugin.getConfigManager().isGiveItem()) {
            giveItem(player, plugin.getBarManager().getItemFactory().build(drink));
        }
        if (plugin.getConfigManager().isAutoCloseBar()) {
            player.closeInventory();
        }
        return true;
    }

    // --- Consommation d'une fiole ---

    /**
     * Consomme la fiole tenue en main et lance la partie.
     *
     * @return true si la partie a demarre (l'item a alors ete retire).
     */
    public boolean drink(Player player, Drink drink, EquipmentSlot hand) {
        if (!checkWorld(player)) return false;
        if (!checkPermission(player, drink)) return false;
        if (isGambling(player)) {
            plugin.getMessageManager().send(player, "already-gambling");
            return false;
        }
        if (!checkCooldown(player, drink)) return false;

        Currency currency = plugin.getCurrencyManager().get(drink.getCurrency());
        if (!currency.isAvailable()) {
            plugin.getMessageManager().send(player, "currency-unavailable",
                Map.of("currency", drink.getCurrency().name()));
            return false;
        }

        consumeOne(player, hand);
        startGamble(player, drink);
        return true;
    }

    /**
     * Lance le tirage, l'animation puis la revelation.
     */
    public void startGamble(Player player, Drink drink) {
        plugin.getCooldownManager().apply(player, drink);
        gambling.add(player.getUniqueId());

        GambleResult result = roll(drink);
        DrinkAnimation animation = plugin.getAnimationManager().get(drink.getAnimation());

        plugin.getMessageManager().send(player, "drink", placeholders(player, drink, null));

        animation.play(player, drink, result, () -> reveal(player, drink, result));
    }

    /**
     * Tirage identique a l'ancien plugin : un pourcentage a deux decimales.
     */
    public GambleResult roll(Drink drink) {
        double value = ThreadLocalRandom.current().nextInt(10000) / 100.0D;
        if (drink.hasSuperWin() && drink.getSuperOdds() >= value) return GambleResult.SUPER_WIN;
        if (drink.getOdds() >= value) return GambleResult.WIN;
        return GambleResult.LOSE;
    }

    private void reveal(Player player, Drink drink, GambleResult result) {
        gambling.remove(player.getUniqueId());
        unfreeze(player);

        Currency currency = plugin.getCurrencyManager().get(drink.getCurrency());
        long payout = switch (result) {
            case SUPER_WIN -> drink.getSuperReward();
            case WIN -> drink.getReward();
            case LOSE -> 0L;
        };

        if (payout > 0 && player.isOnline()) {
            currency.deposit(player, payout);
        }

        if (plugin.getStatsManager().isAvailable()) {
            PlayerStats stats = plugin.getStatsManager().getOrCreate(player);
            stats.record(drink.getCurrency(), drink.getPrice(), payout, result.isWin(),
                result == GambleResult.SUPER_WIN);
        }

        if (!player.isOnline()) return;

        Map<String, String> placeholders = placeholders(player, drink, Map.of(
            "payout", currency.format(payout),
            "payout_raw", NumberUtils.format(payout),
            "balance", currency.format(currency.getBalance(player))));

        switch (result) {
            case SUPER_WIN -> {
                plugin.getMessageManager().send(player, "super-win", placeholders);
                plugin.getEffectManager().play(player, drink.getSuperWinEffect());
                ActionRunner.run(plugin, player, drink.getSuperWinActions(), placeholders);
            }
            case WIN -> {
                plugin.getMessageManager().send(player, "win", placeholders);
                plugin.getEffectManager().play(player, drink.getWinEffect());
                ActionRunner.run(plugin, player, drink.getWinActions(), placeholders);
            }
            case LOSE -> {
                plugin.getMessageManager().send(player, "lose", placeholders);
                plugin.getEffectManager().play(player, drink.getLoseEffect());
                ActionRunner.run(plugin, player, drink.getLoseActions(), placeholders);
            }
        }
    }

    // --- Verifications ---

    private boolean checkWorld(Player player) {
        if (!plugin.getConfigManager().isWorldBlacklisted(player.getWorld().getName())) return true;
        plugin.getMessageManager().send(player, "world-blacklisted",
            Map.of("world", player.getWorld().getName()));
        return false;
    }

    private boolean checkPermission(Player player, Drink drink) {
        if (!drink.hasPermission() || player.hasPermission(drink.getPermission())) return true;
        plugin.getMessageManager().send(player, "no-permission");
        return false;
    }

    private boolean checkCooldown(Player player, Drink drink) {
        long remaining = plugin.getCooldownManager().getRemaining(player, drink);
        if (remaining <= 0L) return true;
        plugin.getMessageManager().send(player, "cooldown", placeholders(player, drink,
            Map.of("time", NumberUtils.formatDuration(remaining),
                "seconds", String.valueOf((remaining + 999) / 1000L))));
        return false;
    }

    public boolean isGambling(Player player) {
        return gambling.contains(player.getUniqueId());
    }

    // --- Immobilisation pendant l'animation ---

    public void freeze(Player player, int durationTicks) {
        frozenUntil.put(player.getUniqueId(), System.currentTimeMillis() + durationTicks * 50L);
    }

    public void unfreeze(Player player) {
        frozenUntil.remove(player.getUniqueId());
    }

    public boolean isFrozen(Player player) {
        Long until = frozenUntil.get(player.getUniqueId());
        if (until == null) return false;
        if (until < System.currentTimeMillis()) {
            frozenUntil.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public void handleQuit(Player player) {
        frozenUntil.remove(player.getUniqueId());
        gambling.remove(player.getUniqueId());
        plugin.getCooldownManager().forgetExpired(player.getUniqueId());
    }

    // --- Utilitaires ---

    private void consumeOne(Player player, EquipmentSlot hand) {
        PlayerInventory inventory = player.getInventory();
        ItemStack item = hand == EquipmentSlot.OFF_HAND
            ? inventory.getItemInOffHand() : inventory.getItemInMainHand();
        if (item.getAmount() <= 1) {
            item.setAmount(0);
        } else {
            item.setAmount(item.getAmount() - 1);
        }
        if (hand == EquipmentSlot.OFF_HAND) {
            inventory.setItemInOffHand(item.getAmount() <= 0 ? null : item);
        } else {
            inventory.setItemInMainHand(item.getAmount() <= 0 ? null : item);
        }
    }

    /**
     * Donne l'item, ou le fait tomber au sol si l'inventaire est plein.
     */
    public void giveItem(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        for (ItemStack rest : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), rest);
        }
    }

    private Map<String, String> placeholders(Player player, Drink drink, Map<String, String> extra) {
        Currency currency = plugin.getCurrencyManager().get(drink.getCurrency());
        Map<String, String> map = new HashMap<>();
        map.put("player", player.getName());
        map.put("drink", ColorUtils.colorize(drink.getName()));
        map.put("drink_id", drink.getId());
        map.put("bar", drink.getBarId());
        map.put("price", currency.format(drink.getPrice()));
        map.put("price_raw", NumberUtils.format(drink.getPrice()));
        map.put("reward", currency.format(drink.getReward()));
        map.put("reward_raw", NumberUtils.format(drink.getReward()));
        map.put("super_reward", currency.format(drink.getSuperReward()));
        map.put("super_reward_raw", NumberUtils.format(drink.getSuperReward()));
        map.put("odds", NumberUtils.format(drink.getOdds()));
        map.put("super_odds", NumberUtils.format(drink.getSuperOdds()));
        map.put("currency", drink.getCurrency().name());
        if (extra != null) map.putAll(extra);
        return map;
    }
}

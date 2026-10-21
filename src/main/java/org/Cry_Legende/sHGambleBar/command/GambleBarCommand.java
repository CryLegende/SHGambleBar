package org.Cry_Legende.sHGambleBar.command;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.Cry_Legende.sHGambleBar.bar.Bar;
import org.Cry_Legende.sHGambleBar.bar.Drink;
import org.Cry_Legende.sHGambleBar.storage.PlayerStats;
import org.Cry_Legende.sHGambleBar.util.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * /shgamblebar (alias /bar) : ouverture des bars et administration.
 */
public class GambleBarCommand implements TabExecutor {

    private final SHGambleBar plugin;

    public GambleBarCommand(SHGambleBar plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String[] args) {
        if (args.length == 0) {
            openDefaultBar(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "help" -> help(sender);
            case "reload" -> reload(sender);
            case "give" -> give(sender, args);
            case "open" -> open(sender, args);
            case "stats" -> stats(sender, args);
            default -> openBar(sender, args[0]);
        }
        return true;
    }

    private void openDefaultBar(CommandSender sender) {
        openBar(sender, plugin.getConfigManager().getDefaultBar());
    }

    private void openBar(CommandSender sender, String barId) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "must-be-player");
            return;
        }
        if (!player.hasPermission("shgamblebar.use")) {
            plugin.getMessageManager().send(player, "no-permission");
            return;
        }
        plugin.getBarManager().openBar(player, barId);
    }

    private void help(CommandSender sender) {
        String path = sender.hasPermission("shgamblebar.admin") ? "help-admin" : "help";
        plugin.getMessageManager().sendList(sender, path, Map.of());
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("shgamblebar.admin.reload")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return;
        }
        plugin.reloadAll();
        plugin.getMessageManager().send(sender, "reloaded");
    }

    /**
     * /shgamblebar give <joueur> <bar> <fiole> [quantite]
     */
    private void give(CommandSender sender, String[] args) {
        if (!sender.hasPermission("shgamblebar.admin.give")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return;
        }
        if (args.length < 4) {
            plugin.getMessageManager().send(sender, "usage-give");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.getMessageManager().send(sender, "player-not-found", Map.of("player", args[1]));
            return;
        }

        Bar bar = plugin.getBarManager().getBar(args[2]);
        Drink drink = bar == null ? null : bar.getDrink(args[3]);
        if (drink == null) {
            plugin.getMessageManager().send(sender, "invalid-drink",
                Map.of("bar", args[2], "drink", args[3]));
            return;
        }

        int amount = args.length > 4 ? Math.max(1, Math.min(64, NumberUtils.parseInt(args[4], 1))) : 1;
        ItemStack item = plugin.getBarManager().getItemFactory().build(drink);
        item.setAmount(amount);
        plugin.getGambleService().giveItem(target, item);

        Map<String, String> placeholders = Map.of(
            "player", target.getName(),
            "drink", drink.getId(),
            "bar", drink.getBarId(),
            "amount", String.valueOf(amount));
        plugin.getMessageManager().send(sender, "gave-drink", placeholders);
        plugin.getMessageManager().send(target, "received-drink", placeholders);
    }

    /**
     * /shgamblebar open <joueur> [bar] : utile pour les PNJ Citizens.
     */
    private void open(CommandSender sender, String[] args) {
        if (!sender.hasPermission("shgamblebar.admin.open")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return;
        }
        if (args.length < 2) {
            plugin.getMessageManager().send(sender, "usage-open");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            plugin.getMessageManager().send(sender, "player-not-found", Map.of("player", args[1]));
            return;
        }

        String barId = args.length > 2 ? args[2] : plugin.getConfigManager().getDefaultBar();
        plugin.getBarManager().openBar(target, barId);
    }

    private void stats(CommandSender sender, String[] args) {
        if (!plugin.getStatsManager().isAvailable()) {
            plugin.getMessageManager().send(sender, "stats-unavailable");
            return;
        }

        Player target;
        if (args.length > 1) {
            if (!sender.hasPermission("shgamblebar.admin.stats")) {
                plugin.getMessageManager().send(sender, "no-permission");
                return;
            }
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                plugin.getMessageManager().send(sender, "player-not-found", Map.of("player", args[1]));
                return;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            plugin.getMessageManager().send(sender, "must-be-player");
            return;
        }

        PlayerStats stats = plugin.getStatsManager().getOrCreate(target);
        plugin.getMessageManager().sendList(sender, "stats", Map.ofEntries(
            Map.entry("player", target.getName()),
            Map.entry("drinks", NumberUtils.format(stats.getDrinks())),
            Map.entry("wins", NumberUtils.format(stats.getWins())),
            Map.entry("super_wins", NumberUtils.format(stats.getSuperWins())),
            Map.entry("losses", NumberUtils.format(stats.getLosses())),
            Map.entry("winrate", NumberUtils.format(stats.getWinRate())),
            Map.entry("wagered_money", NumberUtils.format(stats.getWageredMoney())),
            Map.entry("won_money", NumberUtils.format(stats.getWonMoney())),
            Map.entry("profit_money", NumberUtils.format(stats.getProfitMoney())),
            Map.entry("wagered_exp", NumberUtils.format(stats.getWageredExp())),
            Map.entry("won_exp", NumberUtils.format(stats.getWonExp())),
            Map.entry("profit_exp", NumberUtils.format(stats.getProfitExp())),
            Map.entry("biggest_win_money", NumberUtils.format(stats.getBiggestWinMoney())),
            Map.entry("biggest_win_exp", NumberUtils.format(stats.getBiggestWinExp())),
            Map.entry("streak", NumberUtils.format(stats.getWinStreak())),
            Map.entry("best_streak", NumberUtils.format(stats.getBestStreak()))));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, String[] args) {
        List<String> out = new ArrayList<>();

        if (args.length == 1) {
            out.addAll(plugin.getBarManager().getBarIds());
            out.add("help");
            if (plugin.getStatsManager().isAvailable()) out.add("stats");
            if (sender.hasPermission("shgamblebar.admin.reload")) out.add("reload");
            if (sender.hasPermission("shgamblebar.admin.give")) out.add("give");
            if (sender.hasPermission("shgamblebar.admin.open")) out.add("open");
        } else if (args.length == 2 && isAny(args[0], "give", "open", "stats")) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                out.add(player.getName());
            }
        } else if (args.length == 3 && isAny(args[0], "give", "open")) {
            out.addAll(plugin.getBarManager().getBarIds());
        } else if (args.length == 4 && isAny(args[0], "give")) {
            Bar bar = plugin.getBarManager().getBar(args[2]);
            if (bar != null) out.addAll(bar.getDrinkIds());
        }

        String current = args[args.length - 1].toLowerCase(Locale.ROOT);
        out.removeIf(entry -> !entry.toLowerCase(Locale.ROOT).startsWith(current));
        return out;
    }

    private boolean isAny(String value, String... options) {
        for (String option : options) {
            if (option.equalsIgnoreCase(value)) return true;
        }
        return false;
    }
}

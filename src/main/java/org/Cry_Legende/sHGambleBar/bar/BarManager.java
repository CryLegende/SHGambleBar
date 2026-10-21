package org.Cry_Legende.sHGambleBar.bar;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.Cry_Legende.sHGambleBar.util.ColorUtils;
import org.Cry_Legende.sHGambleBar.util.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Charge les bars depuis le dossier bars/ et construit leurs inventaires.
 */
public class BarManager {

    private static final String[] DEFAULT_BARS = {"mainbar.yml"};

    private final SHGambleBar plugin;
    private final DrinkItemFactory itemFactory;
    private final Map<String, Bar> bars = new LinkedHashMap<>();
    private final LegacyDrinkMatcher legacyMatcher;

    public BarManager(SHGambleBar plugin) {
        this.plugin = plugin;
        this.itemFactory = new DrinkItemFactory(plugin);
        this.legacyMatcher = new LegacyDrinkMatcher(plugin);
        reload();
    }

    public DrinkItemFactory getItemFactory() {
        return itemFactory;
    }

    public LegacyDrinkMatcher getLegacyMatcher() {
        return legacyMatcher;
    }

    public void reload() {
        bars.clear();

        File folder = new File(plugin.getDataFolder(), "bars");
        if (!folder.exists() && folder.mkdirs()) {
            for (String name : DEFAULT_BARS) {
                plugin.saveResource("bars/" + name, false);
            }
        }

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("Aucun fichier de bar trouve dans " + folder.getPath() + ".");
            return;
        }

        String globalAnimation = plugin.getConfigManager().getString("animation.default-type", "TITLE");

        for (File file : files) {
            String id = file.getName().substring(0, file.getName().length() - 4).toLowerCase(Locale.ROOT);
            try {
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
                Bar bar = Bar.load(id, configuration, globalAnimation, plugin.getLogger());
                bars.put(id, bar);
            } catch (Exception e) {
                plugin.getLogger().severe("Impossible de charger le bar '" + id + "' : " + e.getMessage());
            }
        }

        legacyMatcher.rebuild(bars.values(), itemFactory);

        plugin.getLogger().info("Bars charges : " + bars.size() + " " + bars.keySet());
    }

    public Bar getBar(String id) {
        return id == null ? null : bars.get(id.toLowerCase(Locale.ROOT));
    }

    public Set<String> getBarIds() {
        return bars.keySet();
    }

    public java.util.Collection<Bar> getBars() {
        return bars.values();
    }

    public List<Drink> getAllDrinks() {
        List<Drink> drinks = new ArrayList<>();
        for (Bar bar : bars.values()) {
            drinks.addAll(bar.getDrinks());
        }
        return drinks;
    }

    /**
     * Resout un identifiant "barId:drinkId" en fiole.
     */
    public Drink getDrinkByKey(String key) {
        if (key == null) return null;
        int separator = key.indexOf(':');
        if (separator < 0) return null;
        Bar bar = getBar(key.substring(0, separator));
        return bar == null ? null : bar.getDrink(key.substring(separator + 1));
    }

    /**
     * Construit un inventaire frais pour ce bar (jamais partage entre joueurs).
     */
    public Inventory buildInventory(Bar bar) {
        BarHolder holder = new BarHolder(bar.getId());
        Inventory inventory = Bukkit.createInventory(holder, bar.getSize(), ColorUtils.colorize(bar.getTitle()));
        holder.setInventory(inventory);

        for (DecorationItem decoration : bar.getDecoration()) {
            ItemStack item = decoration.getItem();
            for (int slot : decoration.getSlots()) {
                if (slot >= 0 && slot < bar.getSize()) {
                    inventory.setItem(slot, item.clone());
                }
            }
        }

        for (Drink drink : bar.getDrinks()) {
            inventory.setItem(drink.getSlot(), itemFactory.build(drink));
        }

        return inventory;
    }

    public boolean openBar(Player player, String barId) {
        Bar bar = getBar(barId);
        if (bar == null) {
            plugin.getMessageManager().send(player, "invalid-bar",
                Map.of("bars", String.join(", ", getBarIds())));
            return false;
        }
        if (bar.hasPermission() && !player.hasPermission(bar.getPermission())) {
            plugin.getMessageManager().send(player, "no-permission");
            return false;
        }
        long missingPlaytime = getMissingPlaytimeMillis(player);
        if (missingPlaytime > 0L) {
            int required = plugin.getConfigManager().getMinPlaytimeMinutes();
            plugin.getMessageManager().send(player, "playtime-required", Map.of(
                "time", NumberUtils.formatDuration(missingPlaytime),
                "required", NumberUtils.formatDuration(required * 60_000L)));
            return false;
        }

        player.openInventory(buildInventory(bar));
        plugin.getMessageManager().send(player, "bar-opened", Map.of("bar", bar.getId()));
        return true;
    }

    /**
     * Temps de jeu restant avant d'avoir acces aux bars (0 = acces autorise).
     * Empeche les comptes tout juste crees de miser directement leur argent de depart.
     */
    private long getMissingPlaytimeMillis(Player player) {
        int minMinutes = plugin.getConfigManager().getMinPlaytimeMinutes();
        if (minMinutes <= 0 || player.hasPermission("shgamblebar.bypass.playtime")) return 0L;
        // PLAY_ONE_MINUTE est exprime en ticks malgre son nom.
        long playedMillis = player.getStatistic(Statistic.PLAY_ONE_MINUTE) * 50L;
        return Math.max(0L, minMinutes * 60_000L - playedMillis);
    }
}

package org.Cry_Legende.sHGambleBar.animation;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.Cry_Legende.sHGambleBar.bar.Drink;
import org.Cry_Legende.sHGambleBar.effect.SoundCue;
import org.Cry_Legende.sHGambleBar.game.GambleResult;
import org.Cry_Legende.sHGambleBar.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Roulette en GUI : une bande de symboles defile puis ralentit
 * jusqu'a s'arreter sur le symbole correspondant au resultat deja tire.
 */
public class RouletteAnimation implements DrinkAnimation {

    private static final int VISIBLE = 9;
    private static final int CENTER = 4;

    private final SHGambleBar plugin;
    private final Map<UUID, Session> sessions = new HashMap<>();

    public RouletteAnimation(SHGambleBar plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "ROULETTE";
    }

    @Override
    public void play(Player player, Drink drink, GambleResult result, Runnable onReveal) {
        ConfigurationSection section = plugin.getConfigManager().section("animation.roulette");
        if (section == null) {
            onReveal.run();
            return;
        }

        // Une seule roulette a la fois par joueur.
        Session running = sessions.get(player.getUniqueId());
        if (running != null) {
            running.finish();
        }

        int rows = Math.max(3, Math.min(6, section.getInt("rows", 3)));
        int size = rows * 9;
        int reelStart = (rows / 2) * 9;

        List<ItemStack> symbols = loadSymbols(section, drink);
        ItemStack resultItem = loadResultItem(section, result, drink);
        int steps = Math.max(VISIBLE + 1, section.getInt("spin-steps", 32));

        ItemStack[] reel = new ItemStack[steps + VISIBLE + 1];
        for (int i = 0; i < reel.length; i++) {
            reel[i] = symbols.get(ThreadLocalRandom.current().nextInt(symbols.size()));
        }
        reel[(steps - 1 + CENTER) % reel.length] = resultItem;

        String title = ColorUtils.colorize(section.getString("title", "&8Roulette")
            .replace("%drink%", ColorUtils.colorize(drink.getName())));

        RouletteHolder holder = new RouletteHolder();
        Inventory inventory = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inventory);

        ItemStack filler = loadItem(section.getConfigurationSection("filler"),
            Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int slot = 0; slot < size; slot++) {
            inventory.setItem(slot, filler.clone());
        }
        if (section.getBoolean("pointer.enabled", true)) {
            ItemStack pointer = loadItem(section.getConfigurationSection("pointer"),
                Material.ORANGE_STAINED_GLASS_PANE, "&e>");
            int above = reelStart - 9 + CENTER;
            int below = reelStart + 9 + CENTER;
            if (above >= 0) inventory.setItem(above, pointer.clone());
            if (below < size) inventory.setItem(below, pointer.clone());
        }

        player.openInventory(inventory);

        Session session = new Session(player, holder, onReveal, reel, reelStart, steps,
            Math.max(1, section.getInt("start-interval-ticks", 1)),
            Math.max(1, section.getInt("end-interval-ticks", 8)),
            Math.max(0, section.getInt("reveal-delay-ticks", 25)),
            section.getBoolean("close-on-reveal", true),
            loadSound(section, "tick-sound"),
            loadSound(section, "stop-sound"));
        sessions.put(player.getUniqueId(), session);
        session.start();
    }

    /**
     * Deconnexion : on applique quand meme le resultat, le joueur a deja paye.
     */
    @Override
    public void cancel(Player player) {
        Session session = sessions.get(player.getUniqueId());
        if (session != null) session.finish();
    }

    /**
     * Arret du plugin : on solde toutes les roulettes en cours.
     */
    public void finishAll() {
        for (Session session : new ArrayList<>(sessions.values())) {
            session.finish();
        }
    }

    /**
     * Appele par le listener quand le joueur ferme la roulette avant la fin.
     */
    public void handleClose(Player player) {
        Session session = sessions.get(player.getUniqueId());
        if (session != null && !session.holder.isClosedByPlugin()) {
            session.finish();
        }
    }

    public boolean isSpinning(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    private SoundCue loadSound(ConfigurationSection section, String path) {
        ConfigurationSection child = section.getConfigurationSection(path);
        if (child == null || !child.getBoolean("enabled", true)) return null;
        return SoundCue.load(child);
    }

    private List<ItemStack> loadSymbols(ConfigurationSection section, Drink drink) {
        List<ItemStack> symbols = new ArrayList<>();
        List<Map<?, ?>> raw = section.getMapList("symbols");
        for (Map<?, ?> entry : raw) {
            Material material = Material.matchMaterial(String.valueOf(entry.get("material")));
            if (material == null) continue;
            Object name = entry.get("name");
            Object model = entry.get("custom-model-data");
            symbols.add(item(material, name == null ? " " : String.valueOf(name),
                model instanceof Number number ? number.intValue() : null, drink));
        }
        if (symbols.isEmpty()) {
            symbols.add(item(Material.GOLD_NUGGET, "&e&l?", null, drink));
        }
        return symbols;
    }

    private ItemStack loadResultItem(ConfigurationSection section, GambleResult result, Drink drink) {
        String path = switch (result) {
            case SUPER_WIN -> "results.super-win";
            case WIN -> "results.win";
            case LOSE -> "results.lose";
        };
        ConfigurationSection child = section.getConfigurationSection(path);
        Material fallbackMaterial = result == GambleResult.LOSE ? Material.COAL : Material.GOLD_INGOT;
        String fallbackName = result == GambleResult.LOSE ? "&c&lPERDU" : "&6&lGAGNE";
        if (child == null) {
            return item(fallbackMaterial, fallbackName, null, drink);
        }
        return loadItem(child, fallbackMaterial, fallbackName, drink);
    }

    private ItemStack loadItem(ConfigurationSection section, Material fallback, String fallbackName) {
        return loadItem(section, fallback, fallbackName, null);
    }

    private ItemStack loadItem(ConfigurationSection section, Material fallback, String fallbackName, Drink drink) {
        if (section == null) return item(fallback, fallbackName, null, drink);
        Material material = Material.matchMaterial(section.getString("material", fallback.name()));
        Integer model = section.contains("custom-model-data") ? section.getInt("custom-model-data") : null;
        return item(material == null ? fallback : material,
            section.getString("name", fallbackName), model, drink);
    }

    private ItemStack item(Material material, String name, Integer customModelData, Drink drink) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            String display = drink == null ? name
                : name.replace("%drink%", ColorUtils.colorize(drink.getName()));
            meta.setDisplayName(ColorUtils.colorize(display));
            if (customModelData != null) meta.setCustomModelData(customModelData);
            meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    /**
     * Un tour de roulette en cours. L'intervalle entre deux crans augmente
     * progressivement pour donner l'impression d'un ralentissement.
     */
    private class Session {

        private final Player player;
        private final RouletteHolder holder;
        private final Runnable onReveal;
        private final ItemStack[] reel;
        private final int reelStart;
        private final int steps;
        private final int startInterval;
        private final int endInterval;
        private final int revealDelay;
        private final boolean closeOnReveal;
        private final SoundCue tickSound;
        private final SoundCue stopSound;

        private int step;
        private boolean done;
        private BukkitTask task;

        Session(Player player, RouletteHolder holder, Runnable onReveal, ItemStack[] reel, int reelStart,
                int steps, int startInterval, int endInterval, int revealDelay, boolean closeOnReveal,
                SoundCue tickSound, SoundCue stopSound) {
            this.player = player;
            this.holder = holder;
            this.onReveal = onReveal;
            this.reel = reel;
            this.reelStart = reelStart;
            this.steps = steps;
            this.startInterval = startInterval;
            this.endInterval = endInterval;
            this.revealDelay = revealDelay;
            this.closeOnReveal = closeOnReveal;
            this.tickSound = tickSound;
            this.stopSound = stopSound;
        }

        void start() {
            tick();
        }

        private void tick() {
            if (done) return;
            if (!player.isOnline()) {
                finish();
                return;
            }

            render();
            if (tickSound != null) tickSound.play(player, step);

            step++;
            if (step >= steps) {
                if (stopSound != null) stopSound.play(player, 0);
                task = plugin.getServer().getScheduler().runTaskLater(plugin, this::reveal, revealDelay);
                return;
            }
            task = plugin.getServer().getScheduler().runTaskLater(plugin, this::tick, interval());
        }

        private void render() {
            Inventory inventory = holder.getInventory();
            for (int i = 0; i < VISIBLE; i++) {
                inventory.setItem(reelStart + i, reel[(step + i) % reel.length]);
            }
        }

        /**
         * Ralentissement quadratique : nerveux au debut, tres lent a la fin.
         */
        private long interval() {
            double progress = steps <= 1 ? 1.0 : step / (double) (steps - 1);
            return Math.max(1L, Math.round(startInterval + (endInterval - startInterval) * progress * progress));
        }

        private void reveal() {
            if (done) return;
            if (closeOnReveal && player.isOnline()) {
                holder.setClosedByPlugin(true);
                player.closeInventory();
            }
            finish();
        }

        /**
         * Termine le tour et applique le resultat. Idempotent.
         */
        void finish() {
            if (done) return;
            done = true;
            if (task != null) task.cancel();
            sessions.remove(player.getUniqueId());
            onReveal.run();
        }
    }
}

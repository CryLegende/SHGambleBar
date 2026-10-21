package org.Cry_Legende.sHGambleBar.bar;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.Cry_Legende.sHGambleBar.currency.Currency;
import org.Cry_Legende.sHGambleBar.util.ColorUtils;
import org.Cry_Legende.sHGambleBar.util.NumberUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Construit les ItemStack des fioles et les identifie via le PersistentDataContainer.
 * Le nom et le lore restent identiques a ceux de l'ancien plugin, ce qui permet
 * a la couche de compatibilite de reconnaitre les fioles deja en circulation.
 */
public class DrinkItemFactory {

    private static final Map<String, Color> NAMED_COLORS = new HashMap<>();

    static {
        NAMED_COLORS.put("RED", Color.RED);
        NAMED_COLORS.put("BLUE", Color.BLUE);
        NAMED_COLORS.put("AQUA", Color.AQUA);
        NAMED_COLORS.put("BLACK", Color.BLACK);
        NAMED_COLORS.put("FUCHSIA", Color.FUCHSIA);
        NAMED_COLORS.put("GRAY", Color.GRAY);
        NAMED_COLORS.put("GREEN", Color.GREEN);
        NAMED_COLORS.put("LIME", Color.LIME);
        NAMED_COLORS.put("MAROON", Color.MAROON);
        NAMED_COLORS.put("NAVY", Color.NAVY);
        NAMED_COLORS.put("OLIVE", Color.OLIVE);
        NAMED_COLORS.put("ORANGE", Color.ORANGE);
        NAMED_COLORS.put("PURPLE", Color.PURPLE);
        NAMED_COLORS.put("SILVER", Color.SILVER);
        NAMED_COLORS.put("TEAL", Color.TEAL);
        NAMED_COLORS.put("WHITE", Color.WHITE);
        NAMED_COLORS.put("YELLOW", Color.YELLOW);
    }

    private final SHGambleBar plugin;
    private final NamespacedKey drinkKey;

    public DrinkItemFactory(SHGambleBar plugin) {
        this.plugin = plugin;
        this.drinkKey = new NamespacedKey(plugin, "drink");
    }

    public NamespacedKey getDrinkKey() {
        return drinkKey;
    }

    public ItemStack build(Drink drink) {
        ItemStack item = new ItemStack(drink.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(ColorUtils.colorize(applyPlaceholders(drink, drink.getName())));

        List<String> lore = new ArrayList<>();
        for (String line : drink.getLore()) {
            lore.add(ColorUtils.colorize(applyPlaceholders(drink, line)));
        }
        meta.setLore(lore);

        if (drink.getCustomModelData() != null) {
            meta.setCustomModelData(drink.getCustomModelData());
        }
        if (drink.isGlow()) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
        }
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

        meta.getPersistentDataContainer().set(drinkKey, PersistentDataType.STRING, drink.getKey());

        if (meta instanceof PotionMeta potionMeta) {
            Color color = resolveColor(drink.getPotionColor());
            if (color != null) {
                potionMeta.setColor(color);
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Lit l'identifiant "barId:drinkId" stocke dans l'item, ou null.
     */
    public String readKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(drinkKey, PersistentDataType.STRING);
    }

    private Color resolveColor(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        String clean = raw.trim();
        if (clean.startsWith("#")) {
            try {
                int rgb = Integer.parseInt(clean.substring(1), 16);
                return Color.fromRGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        Color named = NAMED_COLORS.get(clean.toUpperCase(Locale.ROOT));
        if (named == null) {
            plugin.getLogger().warning("Couleur de potion inconnue : " + raw);
        }
        return named;
    }

    private String applyPlaceholders(Drink drink, String input) {
        if (input == null || input.indexOf('%') < 0) return input;

        Currency currency = plugin.getCurrencyManager().get(drink.getCurrency());
        return input
            .replace("%price%", NumberUtils.format(drink.getPrice()))
            .replace("%price_formatted%", currency.format(drink.getPrice()))
            .replace("%reward%", NumberUtils.format(drink.getReward()))
            .replace("%reward_formatted%", currency.format(drink.getReward()))
            .replace("%super_reward%", NumberUtils.format(drink.getSuperReward()))
            .replace("%super_reward_formatted%", currency.format(drink.getSuperReward()))
            .replace("%odds%", NumberUtils.format(drink.getOdds()))
            .replace("%super_odds%", NumberUtils.format(drink.getSuperOdds()))
            .replace("%currency%", drink.getCurrency().name());
    }
}

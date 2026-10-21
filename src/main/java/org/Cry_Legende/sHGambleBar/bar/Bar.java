package org.Cry_Legende.sHGambleBar.bar;

import org.Cry_Legende.sHGambleBar.currency.CurrencyType;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Un bar : un inventaire contenant des fioles et des items decoratifs.
 */
public class Bar {

    private final String id;
    private final String title;
    private final int size;
    private final String permission;
    private final CurrencyType defaultCurrency;
    private final String defaultAnimation;

    private final List<DecorationItem> decoration = new ArrayList<>();
    private final Map<String, Drink> drinksById = new LinkedHashMap<>();
    private final Map<Integer, Drink> drinksBySlot = new LinkedHashMap<>();

    private Bar(String id, String title, int size, String permission,
                CurrencyType defaultCurrency, String defaultAnimation) {
        this.id = id;
        this.title = title;
        this.size = size;
        this.permission = permission;
        this.defaultCurrency = defaultCurrency;
        this.defaultAnimation = defaultAnimation;
    }

    public static Bar load(String id, ConfigurationSection section, String globalAnimation, java.util.logging.Logger logger) {
        int size = section.getInt("size", 45);
        if (size % 9 != 0 || size < 9 || size > 54) {
            logger.warning("Taille invalide (" + size + ") pour le bar '" + id + "'. Correction a 54.");
            size = 54;
        }

        Bar bar = new Bar(
            id,
            section.getString("title", "&8Bar"),
            size,
            section.getString("permission", ""),
            CurrencyType.parse(section.getString("default-currency"), CurrencyType.VAULT),
            section.getString("default-animation", globalAnimation)
        );

        ConfigurationSection decorationSection = section.getConfigurationSection("decoration");
        if (decorationSection != null) {
            for (String key : decorationSection.getKeys(false)) {
                ConfigurationSection entry = decorationSection.getConfigurationSection(key);
                if (entry == null) continue;
                DecorationItem item = DecorationItem.load(entry, logger);
                if (item != null) bar.decoration.add(item);
            }
        }

        ConfigurationSection drinksSection = section.getConfigurationSection("drinks");
        if (drinksSection != null) {
            for (String key : drinksSection.getKeys(false)) {
                ConfigurationSection entry = drinksSection.getConfigurationSection(key);
                if (entry == null) continue;

                Drink drink = Drink.load(id, key, entry, bar.defaultCurrency, bar.defaultAnimation, logger);
                if (drink.getSlot() < 0 || drink.getSlot() >= bar.size) {
                    logger.warning("Slot invalide (" + drink.getSlot() + ") pour la fiole '"
                        + key + "' du bar '" + id + "'. Fiole ignoree.");
                    continue;
                }
                if (bar.drinksBySlot.containsKey(drink.getSlot())) {
                    logger.warning("Slot " + drink.getSlot() + " deja occupe dans le bar '" + id
                        + "'. La fiole '" + key + "' est ignoree.");
                    continue;
                }
                bar.drinksById.put(key.toLowerCase(java.util.Locale.ROOT), drink);
                bar.drinksBySlot.put(drink.getSlot(), drink);
            }
        }

        return bar;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission() {
        return permission != null && !permission.isEmpty();
    }

    public CurrencyType getDefaultCurrency() {
        return defaultCurrency;
    }

    public String getDefaultAnimation() {
        return defaultAnimation;
    }

    public List<DecorationItem> getDecoration() {
        return decoration;
    }

    public java.util.Collection<Drink> getDrinks() {
        return drinksById.values();
    }

    public Drink getDrink(String drinkId) {
        return drinkId == null ? null : drinksById.get(drinkId.toLowerCase(java.util.Locale.ROOT));
    }

    public Drink getDrinkAtSlot(int slot) {
        return drinksBySlot.get(slot);
    }

    public java.util.Set<String> getDrinkIds() {
        return drinksById.keySet();
    }
}

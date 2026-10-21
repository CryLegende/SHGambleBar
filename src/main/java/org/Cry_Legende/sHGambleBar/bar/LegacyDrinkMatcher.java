package org.Cry_Legende.sHGambleBar.bar;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compatibilite avec les fioles GambleBarPro deja en circulation.
 * <p>
 * GambleBarPro n'ecrivait aucun NBT sur ses items : il comparait l'ItemStack complet.
 * Les anciennes fioles sont donc reconnues ici par leur nom d'affichage (et
 * optionnellement leur lore), qui sont identiques a ceux de la config SHGambleBar
 * tant que name/lore n'ont pas ete modifies.
 */
public class LegacyDrinkMatcher {

    private final SHGambleBar plugin;
    private final Map<String, Drink> byName = new HashMap<>();
    private final Map<String, Drink> byNameAndLore = new HashMap<>();

    public LegacyDrinkMatcher(SHGambleBar plugin) {
        this.plugin = plugin;
    }

    public void rebuild(Collection<Bar> bars, DrinkItemFactory factory) {
        byName.clear();
        byNameAndLore.clear();

        for (Bar bar : bars) {
            for (Drink drink : bar.getDrinks()) {
                ItemStack rendered = factory.build(drink);
                ItemMeta meta = rendered.getItemMeta();
                if (meta == null) continue;

                String name = meta.getDisplayName();
                byName.putIfAbsent(name, drink);
                byNameAndLore.putIfAbsent(signature(name, meta.getLore()), drink);
            }
        }
    }

    /**
     * @return la fiole correspondant a un ancien item, ou null si aucune correspondance.
     */
    public Drink match(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        if (!plugin.getConfigManager().isLegacyEnabled()) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return null;

        Drink drink;
        if (plugin.getConfigManager().isLegacyMatchLore()) {
            drink = byNameAndLore.get(signature(meta.getDisplayName(), meta.getLore()));
        } else {
            drink = byName.get(meta.getDisplayName());
        }

        if (drink != null && plugin.getConfigManager().isLegacyLog()) {
            plugin.getLogger().info("[Legacy] Ancienne fiole reconnue : " + drink.getKey());
        }
        return drink;
    }

    private String signature(String name, List<String> lore) {
        return name + "\u0000" + (lore == null ? "" : String.join("\u0000", lore));
    }
}

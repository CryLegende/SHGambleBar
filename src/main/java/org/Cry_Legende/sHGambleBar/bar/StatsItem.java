package org.Cry_Legende.sHGambleBar.bar;

import org.Cry_Legende.sHGambleBar.util.ColorUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Item des statistiques du joueur dans un bar (section "stats-item" du bar). Son nom et son lore
 * acceptent les placeholders de la liste "stats" de messages.yml (%drinks%, %wins%, %profit_money%...).
 */
public final class StatsItem {

    private final Material material;
    private final int slot;
    private final String name;
    private final List<String> lore;
    private final Integer customModelData;

    private StatsItem(Material material, int slot, String name, List<String> lore, Integer customModelData) {
        this.material = material;
        this.slot = slot;
        this.name = name;
        this.lore = lore;
        this.customModelData = customModelData;
    }

    public static StatsItem load(ConfigurationSection section, int barSize, Logger logger) {
        Material material = Material.matchMaterial(section.getString("material", "WRITABLE_BOOK"));
        if (material == null) {
            logger.warning("Materiau invalide dans " + section.getCurrentPath() + ". Item des statistiques ignore.");
            return null;
        }
        int slot = section.getInt("slot", -1);
        if (slot < 0 || slot >= barSize) {
            logger.warning("Slot invalide (" + slot + ") dans " + section.getCurrentPath() + ". Item des statistiques ignore.");
            return null;
        }
        Integer customModelData = section.contains("custom-model-data") ? section.getInt("custom-model-data") : null;
        return new StatsItem(material, slot, section.getString("name", "&6Mes statistiques"),
            new ArrayList<>(section.getStringList("lore")), customModelData);
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack render(Map<String, String> placeholders) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtils.colorize(apply(name, placeholders)));
            if (!lore.isEmpty()) {
                meta.setLore(ColorUtils.colorize(lore.stream().map(line -> apply(line, placeholders)).toList()));
            }
            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static String apply(String text, Map<String, String> placeholders) {
        String out = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            out = out.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return out;
    }
}

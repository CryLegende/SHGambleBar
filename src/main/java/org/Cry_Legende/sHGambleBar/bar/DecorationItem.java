package org.Cry_Legende.sHGambleBar.bar;

import org.Cry_Legende.sHGambleBar.util.ColorUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Item purement decoratif place dans un ou plusieurs slots d'un bar.
 */
public class DecorationItem {

    private final ItemStack item;
    private final List<Integer> slots;

    private DecorationItem(ItemStack item, List<Integer> slots) {
        this.item = item;
        this.slots = slots;
    }

    public static DecorationItem load(ConfigurationSection section, Logger logger) {
        Material material = Material.matchMaterial(section.getString("material", "GRAY_STAINED_GLASS_PANE"));
        if (material == null) {
            logger.warning("Materiau invalide dans " + section.getCurrentPath() + ". Decoration ignoree.");
            return null;
        }

        List<Integer> slots = new ArrayList<>(section.getIntegerList("slots"));
        if (section.contains("slot")) {
            slots.add(section.getInt("slot"));
        }
        if (slots.isEmpty()) return null;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ColorUtils.colorize(section.getString("name", " ")));
            List<String> lore = section.getStringList("lore");
            if (!lore.isEmpty()) {
                meta.setLore(ColorUtils.colorize(lore));
            }
            if (section.contains("custom-model-data")) {
                meta.setCustomModelData(section.getInt("custom-model-data"));
            }
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS);
            item.setItemMeta(meta);
        }

        return new DecorationItem(item, slots);
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public List<Integer> getSlots() {
        return slots;
    }
}

package org.Cry_Legende.sHGambleBar.listener;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.Cry_Legende.sHGambleBar.bar.Drink;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Consommation d'une fiole : identification par NBT, puis repli sur la
 * compatibilite avec les anciennes fioles GambleBarPro.
 */
public class DrinkListener implements Listener {

    private final SHGambleBar plugin;

    public DrinkListener(SHGambleBar plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        Drink drink = resolve(item);
        if (drink == null) return;

        Player player = event.getPlayer();
        EquipmentSlot hand = resolveHand(player, item);

        // La consommation est toujours annulee : le plugin gere lui-meme le retrait
        // de l'item, l'animation et le resultat.
        event.setCancelled(true);

        if (hand == EquipmentSlot.OFF_HAND && plugin.getConfigManager().isMainHandOnly()) {
            plugin.getMessageManager().send(player, "no-off-hand");
            return;
        }

        plugin.getGambleService().drink(player, drink, hand);
    }

    private Drink resolve(ItemStack item) {
        String key = plugin.getBarManager().getItemFactory().readKey(item);
        if (key != null) {
            Drink drink = plugin.getBarManager().getDrinkByKey(key);
            if (drink != null) return drink;
        }
        return plugin.getBarManager().getLegacyMatcher().match(item);
    }

    private EquipmentSlot resolveHand(Player player, ItemStack item) {
        return item.isSimilar(player.getInventory().getItemInOffHand())
            && !item.isSimilar(player.getInventory().getItemInMainHand())
            ? EquipmentSlot.OFF_HAND : EquipmentSlot.HAND;
    }
}

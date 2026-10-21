package org.Cry_Legende.sHGambleBar.bar;

import org.Cry_Legende.sHGambleBar.action.Action;
import org.Cry_Legende.sHGambleBar.currency.CurrencyType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Une fiole configurable.
 */
public class Drink {

    private final String id;
    private final String barId;
    private final int slot;

    private final String name;
    private final List<String> lore;
    private final Material material;
    private final Integer customModelData;
    private final String potionColor;
    private final boolean glow;

    private final CurrencyType currency;
    private final long price;
    private final long reward;
    private final long superReward;
    private final double odds;
    private final double superOdds;

    private final String permission;
    private final int cooldownSeconds;

    private final String animation;
    private final String winEffect;
    private final String superWinEffect;
    private final String loseEffect;

    private final List<Action> winActions;
    private final List<Action> superWinActions;
    private final List<Action> loseActions;

    private Drink(Builder builder) {
        this.id = builder.id;
        this.barId = builder.barId;
        this.slot = builder.slot;
        this.name = builder.name;
        this.lore = builder.lore;
        this.material = builder.material;
        this.customModelData = builder.customModelData;
        this.potionColor = builder.potionColor;
        this.glow = builder.glow;
        this.currency = builder.currency;
        this.price = builder.price;
        this.reward = builder.reward;
        this.superReward = builder.superReward;
        this.odds = builder.odds;
        this.superOdds = builder.superOdds;
        this.permission = builder.permission;
        this.cooldownSeconds = builder.cooldownSeconds;
        this.animation = builder.animation;
        this.winEffect = builder.winEffect;
        this.superWinEffect = builder.superWinEffect;
        this.loseEffect = builder.loseEffect;
        this.winActions = builder.winActions;
        this.superWinActions = builder.superWinActions;
        this.loseActions = builder.loseActions;
    }

    public static Drink load(String barId, String id, ConfigurationSection section,
                             CurrencyType barDefaultCurrency, String barDefaultAnimation,
                             Logger logger) {
        Builder builder = new Builder();
        builder.id = id;
        builder.barId = barId;
        builder.slot = section.getInt("slot", -1);
        builder.name = section.getString("name", "&fFiole");
        builder.lore = section.getStringList("lore");

        Material material = Material.matchMaterial(section.getString("material", "POTION"));
        builder.material = material == null ? Material.POTION : material;
        builder.customModelData = section.contains("custom-model-data")
            ? section.getInt("custom-model-data") : null;
        builder.potionColor = section.getString("potion-color");
        builder.glow = section.getBoolean("glow", false);

        builder.currency = CurrencyType.parse(section.getString("currency"), barDefaultCurrency);
        builder.price = section.getLong("price", 0L);
        builder.reward = section.getLong("reward", 0L);
        builder.superReward = section.getLong("super-reward", 0L);
        builder.odds = section.getDouble("odds", 50.0D);
        builder.superOdds = section.getDouble("super-odds", 0.0D);

        builder.permission = section.getString("permission", "");
        builder.cooldownSeconds = section.getInt("cooldown-seconds", -1);

        builder.animation = section.getString("animation", barDefaultAnimation);
        builder.winEffect = section.getString("effects.win", "win");
        builder.superWinEffect = section.getString("effects.super-win", "super_win");
        builder.loseEffect = section.getString("effects.lose", "lose");

        String context = "la fiole '" + id + "' du bar '" + barId + "'";
        builder.winActions = Action.parseList(section.getStringList("win-actions"), context, logger);
        builder.superWinActions = Action.parseList(section.getStringList("super-win-actions"), context, logger);
        builder.loseActions = Action.parseList(section.getStringList("lose-actions"), context, logger);

        return new Drink(builder);
    }

    public String getId() {
        return id;
    }

    public String getBarId() {
        return barId;
    }

    /**
     * Identifiant unique global "barId:drinkId", utilise dans le NBT et les cooldowns.
     */
    public String getKey() {
        return barId + ":" + id;
    }

    public int getSlot() {
        return slot;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public Material getMaterial() {
        return material;
    }

    public Integer getCustomModelData() {
        return customModelData;
    }

    public String getPotionColor() {
        return potionColor;
    }

    public boolean isGlow() {
        return glow;
    }

    public CurrencyType getCurrency() {
        return currency;
    }

    public long getPrice() {
        return price;
    }

    public long getReward() {
        return reward;
    }

    public long getSuperReward() {
        return superReward;
    }

    public double getOdds() {
        return odds;
    }

    public double getSuperOdds() {
        return superOdds;
    }

    public boolean hasSuperWin() {
        return superOdds > 0.0D && superReward > 0L;
    }

    public String getPermission() {
        return permission;
    }

    public boolean hasPermission() {
        return permission != null && !permission.isEmpty();
    }

    /**
     * @return -1 si la fiole utilise le cooldown global.
     */
    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public String getAnimation() {
        return animation == null ? "NONE" : animation.toUpperCase(Locale.ROOT);
    }

    public String getWinEffect() {
        return winEffect;
    }

    public String getSuperWinEffect() {
        return superWinEffect;
    }

    public String getLoseEffect() {
        return loseEffect;
    }

    public List<Action> getWinActions() {
        return winActions == null ? Collections.emptyList() : winActions;
    }

    public List<Action> getSuperWinActions() {
        return superWinActions == null ? Collections.emptyList() : superWinActions;
    }

    public List<Action> getLoseActions() {
        return loseActions == null ? Collections.emptyList() : loseActions;
    }

    private static class Builder {
        String id;
        String barId;
        int slot;
        String name;
        List<String> lore;
        Material material;
        Integer customModelData;
        String potionColor;
        boolean glow;
        CurrencyType currency;
        long price;
        long reward;
        long superReward;
        double odds;
        double superOdds;
        String permission;
        int cooldownSeconds;
        String animation;
        String winEffect;
        String superWinEffect;
        String loseEffect;
        List<Action> winActions;
        List<Action> superWinActions;
        List<Action> loseActions;
    }
}

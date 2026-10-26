package org.Cry_Legende.sHGambleBar.action;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.Cry_Legende.sHGambleBar.effect.SoundCue;
import org.Cry_Legende.sHGambleBar.util.ColorUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Une action configurable, ecrite sous la forme "[type] valeur".
 *
 * <pre>
 *   [message] &amp;7Vous gagnez %reward%
 *   [broadcast] &amp;e%player% a gagne %reward%
 *   [actionbar] &amp;a+%payout%
 *   [actionbar-all] &amp;e%player% a gagne
 *   [title] &amp;6GAGNE;&amp;7+%payout%;10;40;10
 *   [title-all] &amp;6JACKPOT;&amp;7%player%;10;60;10
 *   [sound] ENTITY_PLAYER_LEVELUP;1.0;1.2
 *   [sound-all] ENTITY_ENDER_DRAGON_GROWL;0.6;1.0
 *   [console] eco give %player% 100
 *   [player] warp casino
 *   [delay] 20
 * </pre>
 * <p>
 * Une ligne sans balise est traitee comme un [message].
 */
public class Action {

    public enum Type {
        MESSAGE(true),
        BROADCAST(false),
        ACTIONBAR(true),
        ACTIONBAR_ALL(false),
        TITLE(true),
        TITLE_ALL(false),
        SOUND(true),
        SOUND_ALL(false),
        CONSOLE(false),
        PLAYER(true),
        DELAY(false);

        private final boolean requiresPlayer;

        Type(boolean requiresPlayer) {
            this.requiresPlayer = requiresPlayer;
        }

        /**
         * true si l'action n'a de sens que si le joueur est toujours connecte.
         */
        public boolean requiresPlayer() {
            return requiresPlayer;
        }
    }

    private final Type type;
    private final String value;

    private Action(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public static List<Action> parseList(List<String> lines, String context, Logger logger) {
        if (lines == null || lines.isEmpty()) return Collections.emptyList();
        List<Action> actions = new ArrayList<>(lines.size());
        for (String line : lines) {
            Action action = parse(line, context, logger);
            if (action != null) actions.add(action);
        }
        return actions;
    }

    public static Action parse(String raw, String context, Logger logger) {
        if (raw == null) return null;
        String line = raw.trim();
        if (line.isEmpty()) return null;

        int end = line.startsWith("[") ? line.indexOf(']') : -1;
        if (end < 0) return new Action(Type.MESSAGE, line);

        String tag = line.substring(1, end).trim().toUpperCase(Locale.ROOT).replace('-', '_');
        String value = line.substring(end + 1).trim();
        try {
            return new Action(Type.valueOf(tag), value);
        } catch (IllegalArgumentException e) {
            logger.warning("Action inconnue '[" + tag + "]' dans " + context + ". Ligne ignoree.");
            return null;
        }
    }

    public Type getType() {
        return type;
    }

    /**
     * @return le nombre de ticks d'attente, 0 si ce n'est pas une action [delay].
     */
    public int getDelayTicks() {
        if (type != Type.DELAY) return 0;
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void execute(Player player, Map<String, String> placeholders) {
        String text = apply(value, placeholders);
        switch (type) {
            case DELAY -> {
                // Gere par ActionRunner.
            }
            case MESSAGE -> player.sendMessage(ColorUtils.colorize(text));
            case BROADCAST -> {
                String message = ColorUtils.colorize(text);
                for (Player online : Bukkit.getOnlinePlayers()) {
                    online.sendMessage(message);
                }
                Bukkit.getConsoleSender().sendMessage(message);
            }
            case ACTIONBAR -> sendActionBar(player, text);
            case ACTIONBAR_ALL -> {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    sendActionBar(online, text);
                }
            }
            case TITLE -> sendTitle(player, text);
            case TITLE_ALL -> {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    sendTitle(online, text);
                }
            }
            case SOUND -> playSound(player, text);
            case SOUND_ALL -> {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    playSound(online, text);
                }
            }
            case CONSOLE -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ColorUtils.colorize(text));
            case PLAYER -> player.performCommand(ColorUtils.colorize(text));
        }
    }

    private static void sendActionBar(Player player, String text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
            TextComponent.fromLegacyText(ColorUtils.colorize(text)));
    }

    /**
     * Format : "titre;sous-titre;fadeIn;stay;fadeOut" (durees en ticks).
     */
    private static void sendTitle(Player player, String text) {
        String[] parts = text.split(";", 5);
        player.sendTitle(
            ColorUtils.colorize(parts[0]),
            parts.length > 1 ? ColorUtils.colorize(parts[1]) : "",
            intAt(parts, 2, 10),
            intAt(parts, 3, 40),
            intAt(parts, 4, 10));
    }

    /**
     * Format : "SON;volume;pitch".
     */
    private static void playSound(Player player, String text) {
        String[] parts = text.split(";", 3);
        player.playSound(player.getLocation(), SoundCue.resolveKey(parts[0].trim()),
            floatAt(parts, 1, 1.0f), floatAt(parts, 2, 1.0f));
    }

    private static int intAt(String[] parts, int index, int fallback) {
        if (parts.length <= index) return fallback;
        try {
            return Integer.parseInt(parts[index].trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static float floatAt(String[] parts, int index, float fallback) {
        if (parts.length <= index) return fallback;
        try {
            return Float.parseFloat(parts[index].trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String apply(String text, Map<String, String> placeholders) {
        if (placeholders == null || placeholders.isEmpty()) return text;
        String out = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            out = out.replace("%" + entry.getKey() + "%", entry.getValue() == null ? "" : entry.getValue());
        }
        return out;
    }
}

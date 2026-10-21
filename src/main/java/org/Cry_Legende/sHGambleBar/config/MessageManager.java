package org.Cry_Legende.sHGambleBar.config;

import org.Cry_Legende.sHGambleBar.SHGambleBar;
import org.Cry_Legende.sHGambleBar.util.ColorUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gestion de messages.yml.
 * Un message vide ("") n'est jamais envoye : cela permet de desactiver
 * n'importe quel message sans toucher au code.
 */
public class MessageManager {

    private final SHGambleBar plugin;
    private File file;
    private FileConfiguration messages;

    public MessageManager(SHGambleBar plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messages = YamlConfiguration.loadConfiguration(file);
    }

    public String getPrefix() {
        return ColorUtils.colorize(messages.getString("prefix", ""));
    }

    /**
     * Retourne le message brut colorise, prefixe applique, ou null s'il est desactive.
     */
    public String get(String path, Map<String, String> placeholders) {
        String raw = messages.getString(path);
        if (raw == null || raw.isEmpty()) return null;
        return ColorUtils.colorize(applyPrefix(apply(raw, placeholders)));
    }

    public String get(String path) {
        return get(path, null);
    }

    public List<String> getList(String path, Map<String, String> placeholders) {
        List<String> raw = messages.getStringList(path);
        List<String> out = new ArrayList<>(raw.size());
        for (String line : raw) {
            out.add(ColorUtils.colorize(apply(line, placeholders)));
        }
        return out;
    }

    public void send(CommandSender target, String path) {
        send(target, path, null);
    }

    public void send(CommandSender target, String path, Map<String, String> placeholders) {
        if (target == null) return;
        String message = get(path, placeholders);
        if (message != null) {
            target.sendMessage(message);
        }
    }

    public void sendList(CommandSender target, String path, Map<String, String> placeholders) {
        if (target == null) return;
        for (String line : getList(path, placeholders)) {
            target.sendMessage(line);
        }
    }

    private String applyPrefix(String message) {
        return message.replace("%prefix%", messages.getString("prefix", ""));
    }

    private String apply(String message, Map<String, String> placeholders) {
        if (placeholders == null || placeholders.isEmpty()) return message;
        String out = message;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            out = out.replace("%" + entry.getKey() + "%", entry.getValue() == null ? "" : entry.getValue());
        }
        return out;
    }
}

package org.eldrygo.Utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.SpeedrunBoss;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatUtils {
    private final SpeedrunBoss plugin;
    private final ConfigManager configManager;
    private org.eldrygo.XTeams.Managers.ConfigManager teamConfigManager;

    public ChatUtils(SpeedrunBoss plugin, ConfigManager configManager, org.eldrygo.XTeams.Managers.ConfigManager teamConfigManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.teamConfigManager = teamConfigManager;
    }

    public static String formatColor(String message) {
        message = replaceHexColors(message);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private static String replaceHexColors(String message) {
        Pattern hexPattern = Pattern.compile("#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            StringBuilder color = new StringBuilder("&x");
            for (char c : hexColor.toCharArray()) {
                color.append("&").append(c);
            }
            matcher.appendReplacement(buffer, color.toString());
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
    public String getMessage(String path, Player player) {
        if (configManager == null) {
            throw new IllegalStateException("ConfigManager is not initialized.");
        }

        String message = getMessageConfig().isList(path)
                ? String.join("\n", getMessageConfig().getStringList(path))
                : getMessageConfig().getString(path);

        if (message == null || message.isEmpty()) {
            plugin.getLogger().warning("[WARNING] Message not found: " + path);
            return ChatUtils.formatColor("&r" + configManager.getPrefix() + " #FF0000&l[ERROR] #FF3535Message not found: " + path);
        }

        // Reemplazar placeholders
        if (player != null) {
            message = message.replace("%player%", player.getName());
        } else {
            message = message.replace("%player%", "Unknown");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        message = message.replace("%prefix%", configManager.getPrefix());

        return ChatUtils.formatColor(message);
    }
    public String xTeamsGetMessage(String path, Player player) {
        if (configManager == null) {
            throw new IllegalStateException("ConfigManager is not initialized.");
        }
        if (teamConfigManager == null) {
            throw new IllegalStateException("teamConfigManager is not initialized.");
        }

        String message = getMessageConfig().isList(path)
                ? String.join("\n", getMessageConfig().getStringList(path))
                : getMessageConfig().getString(path);

        if (message == null || message.isEmpty()) {
            plugin.getLogger().warning("[WARNING] Message not found: " + path);
            return ChatUtils.formatColor("&r" + configManager.getPrefix() + " #FF0000&l[ERROR] #FF3535Message not found: " + path);
        }

        // Reemplazar placeholders
        if (player != null) {
            message = message.replace("%player%", player.getName());
        } else {
            message = message.replace("%player%", "Unknown");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            message = PlaceholderAPI.setPlaceholders(player, message);
        }

        message = message.replace("%prefix%", teamConfigManager.getPrefix());

        return ChatUtils.formatColor(message);
    }
    public List<String> getMessageList(String path) {
        // Accede a la instancia de messagesConfig directamente
        List<String> messages = getMessageConfig().getStringList(path);
        if (messages == null) {
            return new ArrayList<>();  // Devuelve una lista vacía si no se encuentra el mensaje
        }
        return messages;  // Retorna la lista de mensajes
    }
    public String getTitle(String path, Player player) {
        if (configManager == null) {
            throw new IllegalStateException("ConfigManager is not initialized.");
        }

        String title = getMessageConfig().getString(path);

        if (title == null || title.isEmpty()) {
            plugin.getLogger().warning("[WARNING] Title not found: " + path);
            return ChatUtils.formatColor("&r" + configManager.getPrefix() + " #FF0000&l[ERROR] #FF3535Title not found: " + path);
        }

        // Reemplazar placeholders
        if (player != null) {
            title = title.replace("%player%", player.getName());
        } else {
            title = title.replace("%player%", "Unknown");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            title = PlaceholderAPI.setPlaceholders(player, title);
        }

        title = title.replace("%prefix%", configManager.getPrefix());

        return ChatUtils.formatColor(title);
    }

    public String getSubtitle(String path, Player player) {
        if (configManager == null) {
            throw new IllegalStateException("ConfigManager is not initialized.");
        }

        String subtitle = getMessageConfig().getString(path);

        if (subtitle == null || subtitle.isEmpty()) {
            plugin.getLogger().warning("[WARNING] Subtitle not found: " + path);
            return ChatUtils.formatColor("&r" + configManager.getPrefix() + " #FF0000&l[ERROR] #FF3535Subtitle not found: " + path);
        }

        // Reemplazar placeholders
        if (player != null) {
            subtitle = subtitle.replace("%player%", player.getName());
        } else {
            subtitle = subtitle.replace("%player%", "Unknown");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            subtitle = PlaceholderAPI.setPlaceholders(player, subtitle);
        }

        subtitle = subtitle.replace("%prefix%", configManager.getPrefix());

        return ChatUtils.formatColor(subtitle);
    }
    public FileConfiguration getMessageConfig() {
        return plugin.messagesConfig;
    }

}
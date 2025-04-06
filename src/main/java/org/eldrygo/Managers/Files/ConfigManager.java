package org.eldrygo.Managers.Files;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;

import java.io.File;

public class ConfigManager {
    private final SpeedrunBoss plugin;
    private org.eldrygo.XTeams.Managers.ConfigManager teamConfigManager;

    public ConfigManager(SpeedrunBoss plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        teamConfigManager.saveTeamsToConfig();
    }
    public String getPrefix() { return plugin.prefix; }
    public FileConfiguration getMessageConfig() { return plugin.messagesConfig; }

    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
            plugin.getLogger().info("✅ The messages.yml file did not exist, it has been created.");
        } else {
            plugin.getLogger().info("✅ The messages.yml file has been loaded successfully.");
        }
        plugin.prefix = ChatUtils.formatColor(getMessageConfig().getString("prefix", "&r&lSpeedrun#fd3c9e&lBoss &cDefault Prefix &8»&r"));
        plugin.xTeamsPrefix = ChatUtils.formatColor(getMessageConfig().getString("xteams.prefix", "#ffbaff&lx&r&lTeams &cDefault Prefix &8»&r"));
        plugin.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }
    public String getMessageString(String messageId) {
        return getMessageConfig().getString(messageId);
    }
}

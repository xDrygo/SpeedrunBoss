package org.eldrygo.Managers.Files;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;

import java.io.File;

public class ConfigManager {
    private final SpeedrunBoss plugin;

    public ConfigManager(SpeedrunBoss plugin, org.eldrygo.XTeams.Managers.ConfigManager teamConfigManager) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        try {
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            plugin.getLogger().info("✅ The config.yml file successfully loaded.");
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Failed on loading config.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public String getPrefix() { return plugin.prefix; }
    public FileConfiguration getMessageConfig() { return plugin.messagesConfig; }
    public FileConfiguration getInventoriesConfig() { return plugin.inventoriesConfig; }

    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        try {
            if (!messagesFile.exists()) {
                plugin.saveResource("messages.yml", false);
                plugin.getLogger().info("✅ The messages.yml file did not exist, it has been created with default configurations.");
            } else {
                plugin.getLogger().info("✅ The messages.yml file successfully loaded.");
            }
            plugin.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Failed on loading messages.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void loadInventories() {
        File messagesFile = new File(plugin.getDataFolder(), "inventories.yml");
        try {
            if (!messagesFile.exists()) {
                plugin.saveResource("inventories.yml", false);
                plugin.getLogger().info("✅ The inventories.yml file did not exist, it has been created with default configurations.");
            } else {
                plugin.getLogger().info("✅ The inventories.yml file successfully loaded.");
            }
            plugin.inventoriesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Failed on loading inventories.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void loadPrefixes() {
        try {
            setPrefix(ChatUtils.formatColor(getMessageConfig().getString("prefix", "&r&lSpeedrun#fd3c9e&lBoss &cDefault Prefix &8»&r")));
            setTeamsPrefix(ChatUtils.formatColor(getMessageConfig().getString("xteams.prefix", "#ffbaff&lx&r&lTeams &cDefault Prefix &8»&r")));
        } catch (Exception e) {
            plugin.getLogger().severe("❌ Failed on loading prefixes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public String getMessageString(String messageId) {
        return getMessageConfig().getString(messageId);
    }

    public void setPrefix(String prefix) {
        plugin.prefix = prefix;
    }
    public void setTeamsPrefix(String prefix) {
        plugin.xTeamsPrefix = prefix;
    }
}

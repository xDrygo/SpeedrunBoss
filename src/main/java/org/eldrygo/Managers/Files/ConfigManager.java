package org.eldrygo.Managers.Files;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ConfigManager {
    private final SpeedrunBoss plugin;
    public JSONObject warpsConfig;

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
    public JSONObject getWarpsConfig() {
        return warpsConfig;
    }

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
    public void loadWarpsConfig() {
        // Ensure the 'data' folder exists
        File dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();  // Create the folder if it doesn't exist
            plugin.getLogger().info("✅ The 'data' folder did not exist, it has been created.");
        }

        File warpsFile = new File(dataFolder, "warps.json");

        try {
            if (!warpsFile.exists()) {
                warpsFile.createNewFile();
                try (FileWriter writer = new FileWriter(warpsFile)) {
                    writer.write("{}"); // initial empty content
                }
                plugin.getLogger().info("✅ The warps.json file did not exist, it has been created at: " + warpsFile.getPath());
            } else {
                plugin.getLogger().info("✅ The warps.json file has been loaded successfully from: " + warpsFile.getPath());
            }

            // Using try-with-resources for automatic closing of FileReader
            try (FileReader reader = new FileReader(warpsFile)) {
                JSONParser parser = new JSONParser();
                warpsConfig = (JSONObject) parser.parse(reader);
            }

        } catch (Exception e) {
            plugin.getLogger().severe("❌ Failed to load warps.json due to an unexpected error at " + warpsFile.getPath() + ": " + e.getMessage());
            e.printStackTrace();
            warpsConfig = new JSONObject(); // fallback empty config
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

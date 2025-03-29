package org.eldrygo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.plugin.java.JavaPlugin;

import org.eldrygo.API.XTeamsAPI;

import org.eldrygo.Managers.Files.ConfigManager;

import org.eldrygo.Utils.LoadUtils;
import org.eldrygo.Utils.LogsUtils;

import java.io.File;

public class SpeedrunBoss extends JavaPlugin {
    public String prefix;
    public FileConfiguration messagesConfig;
    public String version = getDescription().getVersion();
    public ConfigManager configManager;
    public LogsUtils logsUtils;
    public XTeamsAPI xTeamsAPI;
    private final LoadUtils loadUtils;

    public SpeedrunBoss(LoadUtils loadUtils) {
        this.loadUtils = loadUtils;
    }

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.messagesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        this.logsUtils = new LogsUtils(this);
        configManager.loadConfig();
        configManager.loadMessages();
        loadUtils.loadFeatures();
        logsUtils.sendStartupMessage();
    }
    @Override
    public void onDisable() {
        loadUtils.unloadEvent();
        loadUtils.unloadModifiers();
        logsUtils.sendShutdownMessage();
    }
}

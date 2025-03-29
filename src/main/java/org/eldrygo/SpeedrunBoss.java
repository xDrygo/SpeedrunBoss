package org.eldrygo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.plugin.java.JavaPlugin;

import org.eldrygo.API.XTeamsAPI;

import org.eldrygo.BossRace.Managers.BossKillManager;

import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.Managers.Files.TeamDataManager;

import org.eldrygo.Utils.ChatUtils;
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
    public TeamDataManager teamDataManager;
    public BossKillManager bossKillManager;
    private LoadUtils loadUtils;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.messagesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        ChatUtils chatUtils = new ChatUtils(this, configManager);
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

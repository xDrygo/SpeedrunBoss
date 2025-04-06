package org.eldrygo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.plugin.java.JavaPlugin;

import org.eldrygo.BossRace.Managers.BossKillManager;
import org.eldrygo.Cinematics.Managers.CinematicManager;
import org.eldrygo.Event.Managers.EventDataManager;
import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Managers.Files.ConfigManager;

import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Modifiers.Managers.GracePeriodManager;
import org.eldrygo.Modifiers.Managers.ModifierManager;
import org.eldrygo.Modifiers.Managers.PVPManager;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.Utils.DepUtils;
import org.eldrygo.Utils.LoadUtils;
import org.eldrygo.Utils.LogsUtils;
import org.eldrygo.XTeams.Managers.TeamManager;

import java.io.File;
import java.util.Set;
import java.util.UUID;

public class SpeedrunBoss extends JavaPlugin {
    public String prefix;
    public FileConfiguration messagesConfig;
    public String version = getDescription().getVersion();
    public ConfigManager configManager;
    public LogsUtils logsUtils;
    public String xTeamsPrefix;
    private LoadUtils loadUtils;
    private BossKillManager bossKillManager;
    private ModifierManager modifierManager;
    private BroadcastManager broadcastManager;
    private ChatUtils chatUtils;
    private EventManager eventManager;
    private EventDataManager eventDataManager;
    private GracePeriodManager gracePeriodManager;
    private PVPManager pvpManager;
    private CinematicManager cinematicManager;
    private TeamDataManager teamDataManager;
    private EventManager.EventState state;
    private Set<UUID> participants;
    private DepUtils depUtils;
    private TeamManager teamManager;
    private org.eldrygo.XTeams.Managers.ConfigManager teamConfigManager;

    @Override
    public void onEnable() {
        this.configManager = new ConfigManager(this);
        this.teamConfigManager = new org.eldrygo.XTeams.Managers.ConfigManager();
        this.messagesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        this.logsUtils = new LogsUtils(this);
        this.loadUtils = new LoadUtils(this, bossKillManager, broadcastManager, chatUtils, eventManager, eventDataManager, gracePeriodManager, pvpManager, teamDataManager, state, participants, depUtils, cinematicManager, modifierManager, configManager);
        loadUtils.loadFeatures();
        logsUtils.sendStartupMessage();
    }
    @Override
    public void onDisable() {
        loadUtils.unloadEvent();
        loadUtils.unloadModifiers();
        teamConfigManager.saveTeamsToConfig();
        logsUtils.sendShutdownMessage();
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }
}

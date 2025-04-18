package org.eldrygo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.plugin.java.JavaPlugin;

import org.eldrygo.BossRace.Listeners.BossKillListener;
import org.eldrygo.BossRace.Managers.BossKillManager;
import org.eldrygo.BossRace.Managers.DimensionBroadcastManager;
import org.eldrygo.Cinematics.Managers.CinematicManager;
import org.eldrygo.Cinematics.Managers.CountdownBossBarManager;
import org.eldrygo.Cinematics.Managers.FireworkManager;
import org.eldrygo.Compass.Managers.CompassManager;
import org.eldrygo.Event.Managers.EventDataManager;
import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Managers.Files.ConfigManager;

import org.eldrygo.Managers.Files.PlayerDataManager;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Managers.StunManager;
import org.eldrygo.Menu.Inventory.Managers.SPBInventoryManager;
import org.eldrygo.Modifiers.Managers.GracePeriodManager;
import org.eldrygo.Modifiers.Managers.PVPManager;
import org.eldrygo.Spawn.Managers.SpawnManager;
import org.eldrygo.Time.Managers.TimeBarManager;
import org.eldrygo.Time.Managers.TimeManager;
import org.eldrygo.Utils.*;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.eldrygo.XTeams.Managers.TeamManager;
import org.eldrygo.XTeams.TeamLPModule.TeamGroupLinker;

import java.io.File;

public class SpeedrunBoss extends JavaPlugin {
    public String prefix;
    public String winnerTeam = null;
    public FileConfiguration messagesConfig;
    public String version = getDescription().getVersion();
    public ConfigManager configManager;
    public LogsUtils logsUtils;
    public String xTeamsPrefix;
    public YamlConfiguration inventoriesConfig;
    private LoadUtils loadUtils;
    private TeamManager teamManager;
    private org.eldrygo.XTeams.Managers.ConfigManager teamConfigManager;

    @Override
    public void onEnable() {
        TeamGroupLinker teamGroupLinker = new TeamGroupLinker(this);
        XTeamsAPI xTeamsAPI = new XTeamsAPI(this);
        TeamDataManager teamDataManager = new TeamDataManager(this, xTeamsAPI);
        PlayerDataManager playerDataManager = new PlayerDataManager(this);
        this.configManager = new ConfigManager(this, teamConfigManager);
        InventoryUtils inventoryUtils = new InventoryUtils(configManager, playerDataManager, this);
        SPBInventoryManager spbInventoryManager = new SPBInventoryManager(configManager, inventoryUtils, xTeamsAPI, this);
        this.teamConfigManager = new org.eldrygo.XTeams.Managers.ConfigManager(this, teamGroupLinker);
        this.messagesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        this.logsUtils = new LogsUtils(this);
        ChatUtils chatUtils = new ChatUtils(this, configManager, teamConfigManager);
        SpawnManager spawnManager = new SpawnManager(this, xTeamsAPI, chatUtils);
        EventDataManager eventDataManager = new EventDataManager(this);
        TimeManager timeManager = new TimeManager(this);
        TimeBarManager timeBarManager = new TimeBarManager(timeManager, configManager, this);
        BroadcastManager broadcastManager = new BroadcastManager(chatUtils, xTeamsAPI, null, timeManager);
        DimensionBroadcastManager dimensionBroadcastManager = new DimensionBroadcastManager(xTeamsAPI, teamDataManager, broadcastManager);
        PVPManager pvpManager = new PVPManager(0L, this, broadcastManager, chatUtils, xTeamsAPI, null, timeManager);
        GracePeriodManager gracePeriodManager = new GracePeriodManager(this, xTeamsAPI, broadcastManager, chatUtils, null, timeManager);
        CountdownBossBarManager countdownBossBarManager = new CountdownBossBarManager(chatUtils);
        StunManager stunManager = new StunManager(this, xTeamsAPI, null);
        PlayerUtils playerUtils = new PlayerUtils(null, xTeamsAPI, teamGroupLinker, this);
        FireworkManager fireworkManager = new FireworkManager();
        OtherUtils otherUtils = new OtherUtils(this, xTeamsAPI, configManager, chatUtils);
        CinematicManager cinematicManager = new CinematicManager(this, chatUtils, stunManager, countdownBossBarManager, gracePeriodManager, pvpManager, playerUtils, timeManager, timeBarManager, fireworkManager, otherUtils, null, spawnManager);
        EventManager eventManager = new EventManager(chatUtils, xTeamsAPI, cinematicManager, playerUtils, timeManager, timeBarManager, broadcastManager, eventDataManager, this);
        BossKillManager bossKillManager = new BossKillManager(xTeamsAPI, teamDataManager, broadcastManager, eventManager, timeManager, cinematicManager, this);
        BossKillListener bossKillListener = new BossKillListener(bossKillManager, xTeamsAPI, chatUtils, eventManager, this);
        SettingsUtils settingsUtils = new SettingsUtils(this, pvpManager, gracePeriodManager, bossKillListener);
        CompassManager compassManager = new CompassManager(this, configManager, chatUtils);
        setNullClasses(playerUtils, settingsUtils, stunManager, gracePeriodManager, broadcastManager, pvpManager, cinematicManager, eventManager);
        this.loadUtils = new LoadUtils(this, bossKillManager, broadcastManager, chatUtils, eventManager, eventDataManager, gracePeriodManager, pvpManager, teamDataManager, cinematicManager, configManager, xTeamsAPI, teamConfigManager, stunManager, countdownBossBarManager, settingsUtils, teamGroupLinker, compassManager, playerUtils, spbInventoryManager, spawnManager, timeManager, timeBarManager, dimensionBroadcastManager);

        loadUtils.loadFeatures();
        logsUtils.sendStartupMessage();
    }

    @Override
    public void onDisable() {
        if (loadUtils != null) {
            loadUtils.unloadEvent();
            loadUtils.unloadTasks();
        }
        if (teamConfigManager != null) {
            teamConfigManager.saveTeamsToConfig();
        }
        if (logsUtils != null) {
            logsUtils.sendShutdownMessage();
        }
    }

    public TeamManager getTeamManager() { return teamManager; }
    public org.eldrygo.XTeams.Managers.ConfigManager getTeamConfigManager() { return teamConfigManager; }
    public void setTeamManager(TeamManager teamManager) { this.teamManager = teamManager; }
    private void setNullClasses(PlayerUtils playerUtils, SettingsUtils settingsUtils,  StunManager stunManager,
                                GracePeriodManager gracePeriodManager, BroadcastManager broadcastManager,
                                PVPManager pvpManager, CinematicManager cinematicManager, EventManager eventManager) {
        stunManager.playerUtils = playerUtils;
        gracePeriodManager.playerUtils = playerUtils;
        broadcastManager.playerUtils = playerUtils;
        pvpManager.playerUtils = playerUtils;
        playerUtils.setSettingsUtils(settingsUtils);
        cinematicManager.eventManager = eventManager;


    }
}
package org.eldrygo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.plugin.java.JavaPlugin;

import org.eldrygo.BossRace.Listeners.BossKillListener;
import org.eldrygo.BossRace.Managers.BossKillManager;
import org.eldrygo.Cinematics.Managers.CinematicManager;
import org.eldrygo.Cinematics.Managers.CountdownBossBarManager;
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
import org.eldrygo.Utils.*;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.eldrygo.XTeams.Managers.TeamManager;
import org.eldrygo.XTeams.TeamLPModule.TeamGroupLinker;

import java.io.File;

public class SpeedrunBoss extends JavaPlugin {
    public String prefix;
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
        // Primero inicializa todas las dependencias de manera ordenada
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
        BroadcastManager broadcastManager = new BroadcastManager(chatUtils, xTeamsAPI, null);


        // Inicializa PVPManager después de las dependencias necesarias
        PVPManager pvpManager = new PVPManager(0L, this, broadcastManager, chatUtils, xTeamsAPI, null);
        GracePeriodManager gracePeriodManager = new GracePeriodManager(this, xTeamsAPI, broadcastManager, chatUtils, null);

        // Asegúrate de inicializar ModifierManager después de PVPManager
        CountdownBossBarManager countdownBossBarManager = new CountdownBossBarManager(chatUtils);
        StunManager stunManager = new StunManager(this, xTeamsAPI, null);
        BossKillManager bossKillManager = new BossKillManager(xTeamsAPI, teamDataManager, broadcastManager);
        BossKillListener bossKillListener = new BossKillListener(bossKillManager, xTeamsAPI, chatUtils);
        SettingsUtils settingsUtils = new SettingsUtils(this, pvpManager, gracePeriodManager, bossKillListener);
        PlayerUtils playerUtils = new PlayerUtils(settingsUtils, xTeamsAPI, teamGroupLinker);
        CinematicManager cinematicManager = new CinematicManager(this, chatUtils, stunManager, countdownBossBarManager, gracePeriodManager, pvpManager, playerUtils);
        EventManager eventManager = new EventManager(chatUtils, xTeamsAPI, cinematicManager, playerUtils, broadcastManager, eventDataManager, this);
        CompassManager compassManager = new CompassManager(this, configManager, chatUtils);
        setPlayerUtilsToClasses(stunManager, gracePeriodManager, broadcastManager, pvpManager, playerUtils);
        // Inicializa loadUtils después de modifierManager
        this.loadUtils = new LoadUtils(this, bossKillManager, broadcastManager, chatUtils, eventManager, eventDataManager, gracePeriodManager, pvpManager, teamDataManager, cinematicManager, configManager, xTeamsAPI, teamConfigManager, stunManager, countdownBossBarManager, settingsUtils, teamGroupLinker, compassManager, playerUtils, spbInventoryManager, spawnManager);

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
    private void setPlayerUtilsToClasses(StunManager stunManager, GracePeriodManager gracePeriodManager,
                                         BroadcastManager broadcastManager, PVPManager pvpManager, PlayerUtils playerUtils) {
        stunManager.playerUtils = playerUtils;
        gracePeriodManager.playerUtils = playerUtils;
        broadcastManager.playerUtils = playerUtils;
        pvpManager.playerUtils = playerUtils;
    }
}
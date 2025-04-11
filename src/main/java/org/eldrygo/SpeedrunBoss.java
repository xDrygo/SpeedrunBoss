package org.eldrygo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.plugin.java.JavaPlugin;

import org.eldrygo.BossRace.Listeners.WitherSkullListener;
import org.eldrygo.BossRace.Managers.BossKillManager;
import org.eldrygo.Cinematics.CinematicSequence;
import org.eldrygo.Cinematics.Managers.CinematicManager;
import org.eldrygo.Cinematics.Managers.CountdownBossBarManager;
import org.eldrygo.Event.Managers.EventDataManager;
import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Managers.Files.ConfigManager;

import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Managers.StunManager;
import org.eldrygo.Modifiers.Managers.GracePeriodManager;
import org.eldrygo.Modifiers.Managers.ModifierManager;
import org.eldrygo.Modifiers.Managers.PVPManager;
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
    private DepUtils depUtils;
    private TeamManager teamManager;
    private TeamGroupLinker teamGroupLinker;
    private org.eldrygo.XTeams.Managers.ConfigManager teamConfigManager;
    private XTeamsAPI xTeamsAPI;
    private StunManager stunManager;
    private CountdownBossBarManager countdownBossBarManager;
    private WitherSkullListener witherSkullListener;
    private CinematicSequence cinematicSequence;
    private SettingsUtils settingsUtils;

    @Override
    public void onEnable() {
        // Primero inicializa todas las dependencias de manera ordenada
        this.teamGroupLinker = new TeamGroupLinker(this);
        this.xTeamsAPI = new XTeamsAPI(this);
        this.teamDataManager = new TeamDataManager(this, xTeamsAPI);
        this.configManager = new ConfigManager(this);
        this.teamConfigManager = new org.eldrygo.XTeams.Managers.ConfigManager(this, teamGroupLinker);
        this.messagesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        this.logsUtils = new LogsUtils(this);
        this.chatUtils = new ChatUtils(this, configManager, teamConfigManager);
        this.eventDataManager = new EventDataManager(this);
        this.broadcastManager = new BroadcastManager(chatUtils, xTeamsAPI);

        // Inicializa PVPManager después de las dependencias necesarias
        this.pvpManager = new PVPManager(0L, this, broadcastManager, chatUtils, xTeamsAPI);
        this.gracePeriodManager = new GracePeriodManager(this, xTeamsAPI, broadcastManager, chatUtils);

        // Asegúrate de inicializar ModifierManager después de PVPManager
        this.modifierManager = new ModifierManager();
        this.depUtils = new DepUtils();
        this.countdownBossBarManager = new CountdownBossBarManager(this, chatUtils);
        this.stunManager = new StunManager(this, xTeamsAPI, depUtils);
        this.settingsUtils = new SettingsUtils(this, witherSkullListener, pvpManager, gracePeriodManager);
        this.cinematicManager = new CinematicManager(this, chatUtils, stunManager, countdownBossBarManager, gracePeriodManager, pvpManager, settingsUtils);
        this.eventManager = new EventManager(chatUtils, xTeamsAPI, depUtils, cinematicManager, eventDataManager, modifierManager, this);
        this.bossKillManager = new BossKillManager(xTeamsAPI, teamDataManager, broadcastManager);

        // Inicializa loadUtils después de modifierManager
        this.loadUtils = new LoadUtils(this, bossKillManager, broadcastManager, chatUtils, eventManager, eventDataManager, gracePeriodManager, pvpManager, teamDataManager, cinematicManager, modifierManager, configManager, xTeamsAPI, teamConfigManager, depUtils, witherSkullListener, stunManager, countdownBossBarManager, cinematicSequence, settingsUtils, teamGroupLinker);

        // Ahora puedes cargar las características
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
    public BroadcastManager getBroadcastManager() { return broadcastManager; }
    public org.eldrygo.XTeams.Managers.ConfigManager getTeamConfigManager() { return teamConfigManager; }
    public void setTeamManager(TeamManager teamManager) { this.teamManager = teamManager; }
}

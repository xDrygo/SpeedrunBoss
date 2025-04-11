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
    private BroadcastManager broadcastManager;
    private TeamManager teamManager;
    private org.eldrygo.XTeams.Managers.ConfigManager teamConfigManager;
    private WitherSkullListener witherSkullListener;
    private CinematicSequence cinematicSequence;

    @Override
    public void onEnable() {
        // Primero inicializa todas las dependencias de manera ordenada
        TeamGroupLinker teamGroupLinker = new TeamGroupLinker(this);
        XTeamsAPI xTeamsAPI = new XTeamsAPI(this);
        TeamDataManager teamDataManager = new TeamDataManager(this, xTeamsAPI);
        this.configManager = new ConfigManager(this, teamConfigManager);
        this.teamConfigManager = new org.eldrygo.XTeams.Managers.ConfigManager(this, teamGroupLinker);
        this.messagesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        this.logsUtils = new LogsUtils(this);
        ChatUtils chatUtils = new ChatUtils(this, configManager, teamConfigManager);
        EventDataManager eventDataManager = new EventDataManager(this);
        this.broadcastManager = new BroadcastManager(chatUtils, xTeamsAPI);

        // Inicializa PVPManager después de las dependencias necesarias
        PVPManager pvpManager = new PVPManager(0L, this, broadcastManager, chatUtils, xTeamsAPI);
        GracePeriodManager gracePeriodManager = new GracePeriodManager(this, xTeamsAPI, broadcastManager, chatUtils);

        // Asegúrate de inicializar ModifierManager después de PVPManager
        ModifierManager modifierManager = new ModifierManager();
        DepUtils depUtils = new DepUtils();
        CountdownBossBarManager countdownBossBarManager = new CountdownBossBarManager(this, chatUtils);
        StunManager stunManager = new StunManager(this, xTeamsAPI, depUtils);
        SettingsUtils settingsUtils = new SettingsUtils(this, pvpManager, gracePeriodManager);
        CinematicManager cinematicManager = new CinematicManager(this, chatUtils, stunManager, countdownBossBarManager, gracePeriodManager, pvpManager, settingsUtils);
        EventManager eventManager = new EventManager(chatUtils, xTeamsAPI, depUtils, cinematicManager, broadcastManager, eventDataManager, modifierManager, this);
        BossKillManager bossKillManager = new BossKillManager(xTeamsAPI, teamDataManager, broadcastManager);

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

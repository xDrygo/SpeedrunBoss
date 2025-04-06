package org.eldrygo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.plugin.java.JavaPlugin;

import org.eldrygo.BossRace.Listeners.WitherSkullListener;
import org.eldrygo.BossRace.Managers.BossKillManager;
import org.eldrygo.Cinematics.Managers.CinematicManager;
import org.eldrygo.Cinematics.Managers.CountdownBossBarManager;
import org.eldrygo.Compass.Listeners.LocateCommandListener;
import org.eldrygo.Compass.Managers.CompassManager;
import org.eldrygo.Event.Managers.EventDataManager;
import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Managers.Files.ConfigManager;

import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Managers.StunManager;
import org.eldrygo.Modifiers.Managers.GracePeriodManager;
import org.eldrygo.Modifiers.Managers.ModifierManager;
import org.eldrygo.Modifiers.Managers.PVPManager;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.Utils.DepUtils;
import org.eldrygo.Utils.LoadUtils;
import org.eldrygo.Utils.LogsUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.eldrygo.XTeams.Managers.TeamManager;

import java.io.File;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

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
    private XTeamsAPI xTeamsAPI;
    private CompassManager compassManager;
    private LocateCommandListener locateCommandListener;
    private StunManager stunManager;
    private CountdownBossBarManager countdownBossBarManager;
    private WitherSkullListener witherSkullListener;

    @Override
    public void onEnable() {
        // Primero inicializa todas las dependencias de manera ordenada
        this.xTeamsAPI = new XTeamsAPI(this);
        this.teamDataManager = new TeamDataManager(this);
        this.configManager = new ConfigManager(this);
        this.teamConfigManager = new org.eldrygo.XTeams.Managers.ConfigManager(this);
        this.messagesConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        this.logsUtils = new LogsUtils(this);
        this.chatUtils = new ChatUtils(this, configManager, teamConfigManager);
        this.eventDataManager = new EventDataManager(this);
        this.compassManager = new CompassManager(this, configManager, chatUtils, locateCommandListener);
        this.broadcastManager = new BroadcastManager(chatUtils, xTeamsAPI);

        // Inicializa PVPManager después de las dependencias necesarias
        this.pvpManager = new PVPManager((long) 0, this, broadcastManager);
        long pvpStartDelay = this.pvpManager.getPVPStartDelay(); // Ahora puedes usar pvpManager sin problemas

        this.gracePeriodManager = new GracePeriodManager(this, xTeamsAPI, broadcastManager);

        // Asegúrate de inicializar ModifierManager después de PVPManager
        this.modifierManager = new ModifierManager();
        this.depUtils = new DepUtils();
        this.countdownBossBarManager = new CountdownBossBarManager(this, chatUtils);
        this.stunManager = new StunManager(this, xTeamsAPI, depUtils);
        this.cinematicManager = new CinematicManager(this, chatUtils, stunManager, countdownBossBarManager);
        this.eventManager = new EventManager(chatUtils, xTeamsAPI, depUtils, cinematicManager, eventDataManager, modifierManager, this);
        this.bossKillManager = new BossKillManager(xTeamsAPI, teamDataManager, broadcastManager);
        this.witherSkullListener = new WitherSkullListener(this);

        // Inicializa loadUtils después de modifierManager
        this.loadUtils = new LoadUtils(this, bossKillManager, broadcastManager, chatUtils, eventManager, eventDataManager, gracePeriodManager, pvpManager, teamDataManager, state, participants, cinematicManager, modifierManager, configManager, xTeamsAPI, teamConfigManager, depUtils, compassManager, witherSkullListener);

        // Ahora puedes cargar las características
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

    public org.eldrygo.XTeams.Managers.ConfigManager getTeamConfigManager() {
        return teamConfigManager;
    }
    public void setTeamManager(TeamManager teamManager) {
        this.teamManager = teamManager;
    }
}

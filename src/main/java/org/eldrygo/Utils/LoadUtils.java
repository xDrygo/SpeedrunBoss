package org.eldrygo.Utils;

import org.bukkit.Bukkit;
import org.eldrygo.API.XTeamsAPI;
import org.eldrygo.BossRace.Listeners.BossKillListener;
import org.eldrygo.BossRace.Listeners.PortalEnterListener;
import org.eldrygo.BossRace.Managers.BossKillManager;
import org.eldrygo.Compass.Listeners.CompassListener;
import org.eldrygo.Compass.Managers.CompassManager;
import org.eldrygo.Event.Managers.EventDataManager;
import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.Handlers.CommandHandler;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Modifiers.Listeners.PvpListener;
import org.eldrygo.Modifiers.Managers.GracePeriodManager;
import org.eldrygo.Modifiers.Managers.PVPManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.XTeams;
import org.json.simple.JSONObject;

import java.util.Set;
import java.util.UUID;

public class LoadUtils {
    private final SpeedrunBoss plugin;
    private XTeamsAPI xTeamsAPI;
    private BossKillManager bossKillManager;
    private BroadcastManager broadcastManager;
    private ChatUtils chatUtils;
    private CompassManager compassManager;
    private EventManager eventManager;
    private EventDataManager eventDataManager;
    private GracePeriodManager gracePeriodManager;
    private PVPManager pvpManager;
    private TeamDataManager teamDataManager;
    private XTeamsAPI XTeamsAPI;
    private EventManager.EventState state;
    private Set<UUID> participants;
    private JSONObject json;

    public LoadUtils(SpeedrunBoss plugin) {
        this.plugin = plugin;
    }
    public void loadFeatures() {
        loadXTeamsAPI();
        loadPlaceholderAPI();
        loadVault();
        loadEvent();
        loadManagers();
        loadListeners();
        loadCommands();
    }

    private void loadEvent() {
        EventDataManager eventDataManager = new EventDataManager(plugin);
        EventManager eventManager = new EventManager(eventDataManager, XTeamsAPI, plugin);
        eventDataManager.loadEventState(eventManager);
    }
    public void unloadEvent() {
        if (eventManager.getState() == EventManager.EventState.RUNNING || eventManager.getState() == EventManager.EventState.PAUSED) {
            eventDataManager.saveEventState(eventManager.getState(), eventManager.getParticipants());
        }
        eventDataManager.saveEventState(state, participants);
        teamDataManager.saveJsonFile(teamDataManager.readJsonFile());
    }

    private void loadManagers() {
        TeamDataManager teamDataManager = new TeamDataManager(plugin);
        BossKillManager bossKillManager = new BossKillManager(xTeamsAPI, teamDataManager, broadcastManager);
    }
    private void loadListeners() {
        plugin.getServer().getPluginManager().registerEvents(new CompassListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PvpListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PortalEnterListener(teamDataManager, xTeamsAPI), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BossKillListener(plugin, bossKillManager), plugin);
    }
    private void loadCommands() {
        plugin.getCommand("speedrunboss").setExecutor(new CommandHandler());
    }

    // loadFeature Methods:
    private void loadPlaceholderAPI() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            plugin.getLogger().info("✅ PlaceholderAPI detected. PAPI dependency successfully loaded.");
        } else {
            plugin.getLogger().warning("⚠ PlaceholderAPI not detected. Disabling plugin...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }
    private void loadVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            plugin.getLogger().info("✅ Vault detected. Vault dependency successfully loaded.");
        } else {
            plugin.getLogger().warning("⚠ Vault not detected. Disabling plugin...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }
    private void loadXTeamsAPI() {
        if (Bukkit.getPluginManager().getPlugin("xTeams") != null) {
            XTeams xTeams = (XTeams) Bukkit.getPluginManager().getPlugin("xTeams");
            plugin.xTeamsAPI = new XTeamsAPI(xTeams);
            plugin.getLogger().info("✅ xTeams detected. API successfully loaded.");
        } else {
            plugin.getLogger().severe("⚠ xTeams not detected. Disabling plugin...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public void unloadModifiers() {
        if (gracePeriodManager != null) {
            gracePeriodManager.gracePeriodTask.cancel();
        }
    }
}

package org.eldrygo.Utils;

import org.bukkit.Bukkit;
import org.eldrygo.API.XTeamsAPI;
import org.eldrygo.BossRace.Listeners.BossKillListener;
import org.eldrygo.BossRace.Listeners.PortalEnterListener;
import org.eldrygo.BossRace.Managers.BossKillManager;
import org.eldrygo.Cinematics.Managers.CinematicManager;
import org.eldrygo.Compass.Listeners.CompassListener;
import org.eldrygo.Event.Managers.EventDataManager;
import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.Handlers.CommandHandler;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Modifiers.Listeners.PvpListener;
import org.eldrygo.Modifiers.Managers.GracePeriodManager;
import org.eldrygo.Modifiers.Managers.ModifierManager;
import org.eldrygo.Modifiers.Managers.PVPManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.XTeams;

import java.util.Set;
import java.util.UUID;

public class LoadUtils {
    private final SpeedrunBoss plugin;
    private final XTeamsAPI xTeamsAPI;
    private final BossKillManager bossKillManager;
    private final BroadcastManager broadcastManager;
    private final ChatUtils chatUtils;
    private final EventManager eventManager;
    private final EventDataManager eventDataManager;
    private final GracePeriodManager gracePeriodManager;
    private final PVPManager pvpManager;
    private final TeamDataManager teamDataManager;
    private final XTeamsAPI XTeamsAPI;
    private final EventManager.EventState state;
    private final Set<UUID> participants;
    private final DepUtils depUtils;
    private final CinematicManager cinematicManager;
    private final ModifierManager modifierManager;
    private final ConfigManager configManager;

    public LoadUtils(SpeedrunBoss plugin, org.eldrygo.API.XTeamsAPI xTeamsAPI, BossKillManager bossKillManager, BroadcastManager broadcastManager, ChatUtils chatUtils, EventManager eventManager, EventDataManager eventDataManager, GracePeriodManager gracePeriodManager, PVPManager pvpManager, TeamDataManager teamDataManager, org.eldrygo.API.XTeamsAPI xTeamsAPI1, EventManager.EventState state, Set<UUID> participants, DepUtils depUtils, CinematicManager cinematicManager, ModifierManager modifierManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.xTeamsAPI = xTeamsAPI;
        this.bossKillManager = bossKillManager;
        this.broadcastManager = broadcastManager;
        this.chatUtils = chatUtils;
        this.eventManager = eventManager;
        this.eventDataManager = eventDataManager;
        this.gracePeriodManager = gracePeriodManager;
        this.pvpManager = pvpManager;
        this.teamDataManager = teamDataManager;
        XTeamsAPI = xTeamsAPI1;
        this.state = state;
        this.participants = participants;
        this.depUtils = depUtils;
        this.cinematicManager = cinematicManager;
        this.modifierManager = modifierManager;
        this.configManager = configManager;
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
        EventManager eventManager = new EventManager(chatUtils, xTeamsAPI, depUtils, cinematicManager, eventDataManager, modifierManager, XTeamsAPI, plugin);
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
        plugin.getServer().getPluginManager().registerEvents(new PvpListener(modifierManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PortalEnterListener(teamDataManager, xTeamsAPI, chatUtils), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BossKillListener(bossKillManager), plugin);
    }
    private void loadCommands() {
        plugin.getCommand("speedrunboss").setExecutor(new CommandHandler(chatUtils, xTeamsAPI, teamDataManager, configManager, gracePeriodManager, modifierManager, plugin, eventManager, pvpManager, broadcastManager));
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

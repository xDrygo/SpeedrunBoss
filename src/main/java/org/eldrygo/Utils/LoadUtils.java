package org.eldrygo.Utils;

import org.bukkit.Bukkit;
import org.eldrygo.BossRace.Listeners.BossKillListener;
import org.eldrygo.BossRace.Listeners.PortalEnterListener;
import org.eldrygo.BossRace.Listeners.WitherSkullListener;
import org.eldrygo.BossRace.Managers.BossKillManager;
import org.eldrygo.Cinematics.Managers.CinematicManager;
import org.eldrygo.Compass.Listeners.CompassListener;
import org.eldrygo.Compass.Managers.CompassManager;
import org.eldrygo.Event.Managers.EventDataManager;
import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.Handlers.CommandHandler;
import org.eldrygo.Handlers.CommandTabCompleter;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Managers.StunManager;
import org.eldrygo.Modifiers.Listeners.PvpListener;
import org.eldrygo.Modifiers.Managers.GracePeriodManager;
import org.eldrygo.Modifiers.Managers.ModifierManager;
import org.eldrygo.Modifiers.Managers.PVPManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.eldrygo.XTeams.Command.XTeamsCommand;
import org.eldrygo.XTeams.Command.XTeamsTabCompleter;
import org.eldrygo.XTeams.Managers.TeamManager;

import java.util.Set;
import java.util.UUID;

public class LoadUtils {
    private final SpeedrunBoss plugin;
    private final BossKillManager bossKillManager;
    private final BroadcastManager broadcastManager;
    private final ChatUtils chatUtils;
    private EventManager eventManager;
    private final EventDataManager eventDataManager;
    private final GracePeriodManager gracePeriodManager;
    private final PVPManager pvpManager;
    private final TeamDataManager teamDataManager;
    private final EventManager.EventState state;
    private final Set<UUID> participants;
    private final ModifierManager modifierManager;
    private final ConfigManager configManager;
    private final XTeamsAPI xTeamsAPI;
    private final org.eldrygo.XTeams.Managers.ConfigManager teamConfigManager;
    private final DepUtils depUtils;
    private final CinematicManager cinematicManager;
    private final CompassManager compassManager;
    private final WitherSkullListener witherSkullListener;
    private StunManager stunManager;

    public LoadUtils(SpeedrunBoss plugin, BossKillManager bossKillManager, BroadcastManager broadcastManager, ChatUtils chatUtils, EventManager eventManager, EventDataManager eventDataManager, GracePeriodManager gracePeriodManager, PVPManager pvpManager, TeamDataManager teamDataManager, EventManager.EventState state, Set<UUID> participants, CinematicManager cinematicManager, ModifierManager modifierManager, ConfigManager configManager, XTeamsAPI xTeamsAPI, org.eldrygo.XTeams.Managers.ConfigManager teamConfigManager, DepUtils depUtils, CompassManager compassManager, WitherSkullListener witherSkullListener) {
        this.plugin = plugin;
        this.bossKillManager = bossKillManager;
        this.broadcastManager = broadcastManager;
        this.chatUtils = chatUtils;
        this.eventManager = eventManager;
        this.eventDataManager = eventDataManager;
        this.gracePeriodManager = gracePeriodManager;
        this.pvpManager = pvpManager;
        this.teamDataManager = teamDataManager;
        this.state = state;
        this.participants = participants;
        this.modifierManager = modifierManager;
        this.configManager = configManager;
        this.xTeamsAPI = xTeamsAPI;
        this.teamConfigManager = teamConfigManager;
        this.depUtils = depUtils;
        this.cinematicManager = cinematicManager;
        this.compassManager = compassManager;
        this.witherSkullListener = witherSkullListener;
    }
    public void loadFeatures() {
        loadConfig();
        loadXTeams();
        loadPlaceholderAPI();
        loadVault();
        loadEvent();
        loadManagers();
        loadListeners(pvpManager);
        loadCommands();
    }

    public void loadConfig() {
        configManager.loadConfig();
        configManager.loadMessages();
        pvpManager.setPVPStartDelay(plugin.getConfig().getLong("configuration.pvp_start_delay"));
        witherSkullListener.setProbabilityMultiplier(plugin.getConfig().getDouble("configuration.wither_skull_multiplier"));
    }

    private void loadEvent() {
        // Verifica si eventManager ya está inicializado correctamente
        if (eventManager == null) {
            plugin.getLogger().warning("⚠ eventManager is null. Initializing a new EventManager...");
            eventManager = new EventManager(chatUtils, xTeamsAPI, depUtils, cinematicManager, eventDataManager, modifierManager, plugin);
        }

        // Ahora puedes cargar el estado del evento sin problemas
        eventDataManager.loadEventState(eventManager);
    }
    public void unloadEvent() {
        if (eventManager != null) {
            if (eventManager.getState() == EventManager.EventState.RUNNING || eventManager.getState() == EventManager.EventState.PAUSED) {
                eventDataManager.saveEventState(eventManager.getState(), eventManager.getParticipants());
            }
            eventDataManager.saveEventState(state, participants);
            if (teamDataManager != null) {
                teamDataManager.saveJsonFile(teamDataManager.readJsonFile());
            } else {
                plugin.getLogger().warning("⚠ teamDataManager is null, skipping saving team data.");
            }
        }
    }
    private void loadXTeams() {
        if (plugin.getTeamConfigManager() == null) {
            plugin.getLogger().warning("⚠ teamConfigManager is null on xTeams loading.");
        } else {
            TeamManager teamManager = new TeamManager(plugin, teamConfigManager);
            plugin.setTeamManager(teamManager);
            teamConfigManager.loadTeamsFromConfig();
            loadXTeamsCmd();
        }
    }
    private void loadXTeamsCmd() {
        if (plugin.getCommand("xteams") == null) {
            plugin.getLogger().severe("❌ Error: xTeams command is not registered in plugin.yml");
        } else {
            plugin.getCommand("xteams").setExecutor(new XTeamsCommand(plugin, chatUtils, teamConfigManager));
            plugin.getCommand("xteams").setTabCompleter(new XTeamsTabCompleter(plugin, chatUtils));
        }
    }

    private void loadManagers() {
        TeamDataManager teamDataManager = new TeamDataManager(plugin);
        BossKillManager bossKillManager = new BossKillManager(xTeamsAPI, teamDataManager, broadcastManager);
    }
    private void loadListeners(PVPManager pvpManager) {
        plugin.getServer().getPluginManager().registerEvents(new CompassListener(configManager, compassManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PvpListener(pvpManager), plugin); // Agregado 'plugin'
        plugin.getServer().getPluginManager().registerEvents(new PortalEnterListener(teamDataManager, xTeamsAPI, chatUtils), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BossKillListener(bossKillManager), plugin);
    }
    private void loadCommands() {
        if (plugin.getCommand("speedrunboss") == null) {
            plugin.getLogger().severe("❌ Error: SpeedrunBoss command is not registered in plugin.yml");
        } else {
            if (xTeamsAPI == null) {
                plugin.getLogger().warning("⚠ xTeamsAPI is not initialized. Commands will not work succesfully.");
            } else {
                plugin.getCommand("speedrunboss").setExecutor(new CommandHandler(chatUtils, xTeamsAPI, teamDataManager, configManager, gracePeriodManager, modifierManager, plugin, eventManager, pvpManager, broadcastManager, compassManager, this, stunManager));
                plugin.getCommand("speedrunboss").setTabCompleter(new CommandTabCompleter());}
        }
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

    public void unloadModifiers() {
        if (gracePeriodManager != null) {
            gracePeriodManager.gracePeriodTask.cancel();
        }
    }
}

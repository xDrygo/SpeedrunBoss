package org.eldrygo.Utils;

import org.bukkit.Bukkit;
import org.eldrygo.API.SpeedrunBossExtension;
import org.eldrygo.BossRace.Listeners.BossKillListener;
import org.eldrygo.BossRace.Listeners.PortalEnterListener;
import org.eldrygo.BossRace.Listeners.WitherSkullListener;
import org.eldrygo.BossRace.Managers.BossKillManager;
import org.eldrygo.Cinematics.Managers.CinematicManager;
import org.eldrygo.Cinematics.Managers.CountdownBossBarManager;
import org.eldrygo.Compass.Listeners.CompassListener;
import org.eldrygo.Compass.Managers.CompassManager;
import org.eldrygo.Event.Managers.EventDataManager;
import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.Handlers.CommandHandler;
import org.eldrygo.Handlers.CommandTabCompleter;
import org.eldrygo.Interface.Listeners.PlayerConnectionListener;
import org.eldrygo.Interface.Managers.WaitingScreenManager;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Managers.StunManager;
import org.eldrygo.Menu.Inventory.Listeners.SPBInventoryListener;
import org.eldrygo.Menu.Inventory.Managers.SPBInventoryManager;
import org.eldrygo.Modifiers.Listeners.PvpListener;
import org.eldrygo.Modifiers.Managers.GracePeriodManager;
import org.eldrygo.Modifiers.Managers.PVPManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.eldrygo.XTeams.Command.XTeamsCommand;
import org.eldrygo.XTeams.Command.XTeamsTabCompleter;
import org.eldrygo.XTeams.Managers.TeamManager;
import org.eldrygo.XTeams.TeamLPModule.TeamGroupLinker;

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
    private final ConfigManager configManager;
    private final XTeamsAPI xTeamsAPI;
    private final org.eldrygo.XTeams.Managers.ConfigManager teamConfigManager;
    private final CinematicManager cinematicManager;
    private final StunManager stunManager;
    private final CountdownBossBarManager countdownBossBarManager;
    private final SettingsUtils settingsUtils;
    private final TeamGroupLinker teamGroupLinker;
    private final CompassManager compassManager;
    private final PlayerUtils playerUtils;
    private final WaitingScreenManager waitingScreenManager;
    private final SPBInventoryManager spbInventoryManager;

    public LoadUtils(SpeedrunBoss plugin, BossKillManager bossKillManager, BroadcastManager broadcastManager, ChatUtils chatUtils,
                     EventManager eventManager, EventDataManager eventDataManager, GracePeriodManager gracePeriodManager,
                     PVPManager pvpManager, TeamDataManager teamDataManager, CinematicManager cinematicManager,
                     ConfigManager configManager, XTeamsAPI xTeamsAPI, org.eldrygo.XTeams.Managers.ConfigManager teamConfigManager,
                     StunManager stunManager, CountdownBossBarManager countdownBossBarManager,
                     SettingsUtils settingsUtils, TeamGroupLinker teamGroupLinker, CompassManager compassManager, PlayerUtils playerUtils, WaitingScreenManager waitingScreenManager, SPBInventoryManager spbInventoryManager) {
        this.plugin = plugin;
        this.bossKillManager = bossKillManager;
        this.broadcastManager = broadcastManager;
        this.chatUtils = chatUtils;
        this.eventManager = eventManager;
        this.eventDataManager = eventDataManager;
        this.gracePeriodManager = gracePeriodManager;
        this.pvpManager = pvpManager;
        this.teamDataManager = teamDataManager;
        this.configManager = configManager;
        this.xTeamsAPI = xTeamsAPI;
        this.teamConfigManager = teamConfigManager;
        this.cinematicManager = cinematicManager;
        this.stunManager = stunManager;
        this.countdownBossBarManager = countdownBossBarManager;
        this.settingsUtils = settingsUtils;
        this.teamGroupLinker = teamGroupLinker;
        this.compassManager = compassManager;
        this.playerUtils = playerUtils;
        this.waitingScreenManager = waitingScreenManager;
        this.spbInventoryManager = spbInventoryManager;
    }
    public void loadFeatures() {
        loadConfig();
        loadSettings();
        loadXTeams();
        loadPlaceholderAPI();
        loadVault();
        loadEvent();
        loadListeners(pvpManager);
        loadCommands();
    }

    // loadFeature Methods:
    public void loadConfig() {
        plugin.getLogger().info("Loading files...");
        configManager.loadConfig();
        configManager.loadMessages();
        configManager.loadInventories();
        configManager.loadPrefixes();
    }

    private void loadSettings() {
        settingsUtils.loadSettings();
    }

    private void loadEvent() {
        // Verifica si eventManager ya está inicializado correctamente
        plugin.getLogger().info("Loading event manager...");
        if (eventManager == null) {
            plugin.getLogger().warning("⚠ eventManager is null. Initializing a new EventManager...");
            eventManager = new EventManager(chatUtils, xTeamsAPI, cinematicManager, playerUtils, broadcastManager, eventDataManager, plugin);
        }

        // Ahora puedes cargar el estado del evento sin problemas
        eventDataManager.loadEventState(eventManager);
        plugin.getLogger().info("✅ Event state was succesfully loaded.");
    }
    public void unloadEvent() {
        plugin.getLogger().info("Unloading Event Manager...");
        if (eventManager != null) {
            if (eventManager.getState() == EventManager.EventState.RUNNING || eventManager.getState() == EventManager.EventState.PAUSED) {
                eventDataManager.saveEventState(eventManager.getState(), eventManager.getParticipants());
                plugin.getLogger().info("✅ Event data file succesfully saved.");
            }
            if (teamDataManager != null) {
                teamDataManager.saveJsonFile(teamDataManager.readJsonFile());
                plugin.getLogger().info("✅ Team data file was succesfully saved.");
            } else {
                plugin.getLogger().warning("⚠ teamDataManager is null, skipping saving team data.");
            }
        }
    }
    private void loadXTeams() {
        plugin.getLogger().info("Loading xTeams...");
        if (plugin.getTeamConfigManager() == null) {
            plugin.getLogger().warning("⚠ teamConfigManager is null on xTeams loading.");
        } else {
            TeamManager teamManager = new TeamManager(plugin, teamConfigManager, teamGroupLinker);
            plugin.setTeamManager(teamManager);
            teamConfigManager.loadTeamsFromConfig();
            teamGroupLinker.reloadTeamGroupConfig();
            playerUtils.updateTeamGroups();
            loadXTeamsCmd();
        }
    }
    private void loadXTeamsCmd() {
        plugin.getLogger().info("Registering xTeams Command...");
        if (plugin.getCommand("xteams") == null) {
            plugin.getLogger().severe("❌ Error: xTeams command is not registered in plugin.yml");
        } else {
            plugin.getCommand("xteams").setExecutor(new XTeamsCommand(plugin, chatUtils, teamConfigManager));
            plugin.getCommand("xteams").setTabCompleter(new XTeamsTabCompleter(plugin, chatUtils));
            plugin.getLogger().info("✅ xTeams command was successfully registered.");
        }
    }

    private void loadListeners(PVPManager pvpManager) {
        plugin.getServer().getPluginManager().registerEvents(new PvpListener(pvpManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PortalEnterListener(teamDataManager, xTeamsAPI, chatUtils), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BossKillListener(bossKillManager, xTeamsAPI), plugin);
        plugin.getServer().getPluginManager().registerEvents(new WitherSkullListener(settingsUtils), plugin);
        plugin.getServer().getPluginManager().registerEvents(new CompassListener(configManager, compassManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new SPBInventoryListener(plugin, spbInventoryManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerConnectionListener(broadcastManager, playerUtils), plugin);
    }
    private void loadCommands() {
        plugin.getLogger().info("Loading SpeedrunBoss command...");
        if (plugin.getCommand("speedrunboss") == null) {
            plugin.getLogger().severe("❌ Error: SpeedrunBoss command is not registered in plugin.yml");
        } else {
            if (xTeamsAPI == null) {
                plugin.getLogger().warning("⚠ xTeamsAPI is not initialized. Commands will not work succesfully.");
            } else {
                plugin.getCommand("speedrunboss").setExecutor(new CommandHandler(chatUtils, xTeamsAPI, teamDataManager, configManager, gracePeriodManager, plugin, eventManager, pvpManager, broadcastManager, this, stunManager, countdownBossBarManager, cinematicManager, compassManager, playerUtils, waitingScreenManager, spbInventoryManager, teamConfigManager, teamGroupLinker));
                plugin.getCommand("speedrunboss").setTabCompleter(new CommandTabCompleter(cinematicManager, xTeamsAPI, plugin));}
            plugin.getLogger().info("✅ SpeedrunBoss command was successfully loaded.");
        }
    }

    private void loadPlaceholderAPI() {
        plugin.getLogger().info(ChatUtils.formatColor("&eLoading PlaceholderAPI..."));
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SpeedrunBossExtension(plugin, teamDataManager, xTeamsAPI, configManager).register();
            plugin.getLogger().info("✅ PlaceholderAPI detected. PAPI dependency successfully loaded.");
        } else {
            plugin.getLogger().warning("⚠ PlaceholderAPI not detected. Disabling plugin...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }
    private void loadVault() {
        plugin.getLogger().info(ChatUtils.formatColor("&eLoading Vault..."));
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            plugin.getLogger().info("✅ Vault detected. Vault dependency successfully loaded.");
        } else {
            plugin.getLogger().warning("⚠ Vault not detected. Disabling plugin...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public void unloadTasks() {
        plugin.getLogger().info(ChatUtils.formatColor("&eUnloading Tasks..."));
        if (gracePeriodManager != null && gracePeriodManager.gracePeriodTask != null) {
            gracePeriodManager.gracePeriodTask.cancel();
            plugin.getLogger().info("✅ Grace Period task was successfully canceled.");
        }
        if (pvpManager != null && pvpManager.pvpTask != null) {
            pvpManager.pvpTask.cancel();
            plugin.getLogger().info("✅ PvP enabling task was successfully canceled.");
        }
    }
}

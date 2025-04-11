package org.eldrygo.Utils;

import org.bukkit.Bukkit;
import org.eldrygo.API.SpeedrunBossExtension;
import org.eldrygo.BossRace.Listeners.BossKillListener;
import org.eldrygo.BossRace.Listeners.PortalEnterListener;
import org.eldrygo.BossRace.Listeners.WitherSkullListener;
import org.eldrygo.BossRace.Managers.BossKillManager;
import org.eldrygo.Cinematics.CinematicSequence;
import org.eldrygo.Cinematics.Managers.CinematicManager;
import org.eldrygo.Cinematics.Managers.CountdownBossBarManager;
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
    private final ModifierManager modifierManager;
    private final ConfigManager configManager;
    private final XTeamsAPI xTeamsAPI;
    private final org.eldrygo.XTeams.Managers.ConfigManager teamConfigManager;
    private final DepUtils depUtils;
    private final CinematicManager cinematicManager;
    private final WitherSkullListener witherSkullListener;
    private final StunManager stunManager;
    private final CountdownBossBarManager countdownBossBarManager;
    private final CinematicSequence cinematicSequence;
    private final SettingsUtils settingsUtils;
    private TeamGroupLinker teamGroupLinker;

    public LoadUtils(SpeedrunBoss plugin, BossKillManager bossKillManager, BroadcastManager broadcastManager, ChatUtils chatUtils,
                     EventManager eventManager, EventDataManager eventDataManager, GracePeriodManager gracePeriodManager,
                     PVPManager pvpManager, TeamDataManager teamDataManager, CinematicManager cinematicManager, ModifierManager modifierManager,
                     ConfigManager configManager, XTeamsAPI xTeamsAPI, org.eldrygo.XTeams.Managers.ConfigManager teamConfigManager, DepUtils depUtils,
                     WitherSkullListener witherSkullListener, StunManager stunManager, CountdownBossBarManager countdownBossBarManager,
                     CinematicSequence cinematicSequence, SettingsUtils settingsUtils, TeamGroupLinker teamGroupLinker) {
        this.plugin = plugin;
        this.bossKillManager = bossKillManager;
        this.broadcastManager = broadcastManager;
        this.chatUtils = chatUtils;
        this.eventManager = eventManager;
        this.eventDataManager = eventDataManager;
        this.gracePeriodManager = gracePeriodManager;
        this.pvpManager = pvpManager;
        this.teamDataManager = teamDataManager;
        this.modifierManager = modifierManager;
        this.configManager = configManager;
        this.xTeamsAPI = xTeamsAPI;
        this.teamConfigManager = teamConfigManager;
        this.depUtils = depUtils;
        this.cinematicManager = cinematicManager;
        this.witherSkullListener = witherSkullListener;
        this.stunManager = stunManager;
        this.countdownBossBarManager = countdownBossBarManager;
        this.cinematicSequence = cinematicSequence;
        this.settingsUtils = settingsUtils;
        this.teamGroupLinker = teamGroupLinker;
    }
    public void loadFeatures() {
        loadConfig();
        loadSettings();
        loadXTeams();
        loadPlaceholderAPI();
        loadVault();
        loadEvent();
        loadManagers();
        loadListeners(pvpManager);
        loadCommands();
    }

    // loadFeature Methods:
    public void loadConfig() {
        plugin.getLogger().info(ChatUtils.formatColor("&eLoading configuration files..."));
        configManager.loadConfig();
        configManager.loadMessages();
        configManager.loadPrefixes();
    }

    private void loadSettings() {
        settingsUtils.loadSettings();
    }

    private void loadEvent() {
        // Verifica si eventManager ya está inicializado correctamente
        plugin.getLogger().info(ChatUtils.formatColor("&eLoading Event Manager..."));
        if (eventManager == null) {
            plugin.getLogger().warning(ChatUtils.formatColor("&6⚠ eventManager is null. Initializing a new EventManager..."));
            eventManager = new EventManager(chatUtils, xTeamsAPI, depUtils, cinematicManager, eventDataManager, modifierManager, plugin);
        }

        // Ahora puedes cargar el estado del evento sin problemas
        eventDataManager.loadEventState(eventManager);
        plugin.getLogger().info(ChatUtils.formatColor("&a✅ Event state was succesfully loaded."));
    }
    public void unloadEvent() {
        plugin.getLogger().info(ChatUtils.formatColor("&eUnloading Event Manager..."));
        if (eventManager != null) {
            if (eventManager.getState() == EventManager.EventState.RUNNING || eventManager.getState() == EventManager.EventState.PAUSED) {
                eventDataManager.saveEventState(eventManager.getState(), eventManager.getParticipants());
                plugin.getLogger().info(ChatUtils.formatColor("&a✅ Event data file succesfully saved."));
            }
            if (teamDataManager != null) {
                teamDataManager.saveJsonFile(teamDataManager.readJsonFile());
                plugin.getLogger().info(ChatUtils.formatColor("&a✅ Team data file was succesfully saved."));
            } else {
                plugin.getLogger().warning(ChatUtils.formatColor("&6⚠ teamDataManager is null, skipping saving team data."));
            }
        }
    }
    private void loadXTeams() {
        plugin.getLogger().info(ChatUtils.formatColor("&eLoading xTeams..."));
        if (plugin.getTeamConfigManager() == null) {
            plugin.getLogger().warning(ChatUtils.formatColor("&6⚠ teamConfigManager is null on xTeams loading."));
        } else {
            TeamManager teamManager = new TeamManager(plugin, teamConfigManager, teamGroupLinker);
            plugin.setTeamManager(teamManager);
            teamConfigManager.loadTeamsFromConfig();
            loadXTeamsCmd();
        }
    }
    private void loadXTeamsCmd() {
        plugin.getLogger().info(ChatUtils.formatColor("&eRegistering xTeams Command..."));
        if (plugin.getCommand("xteams") == null) {
            plugin.getLogger().severe(ChatUtils.formatColor("&c❌ Error: xTeams command is not registered in plugin.yml"));
        } else {
            plugin.getCommand("xteams").setExecutor(new XTeamsCommand(plugin, chatUtils, teamConfigManager));
            plugin.getCommand("xteams").setTabCompleter(new XTeamsTabCompleter(plugin, chatUtils));
            plugin.getLogger().info(ChatUtils.formatColor("&a✅ xTeams command was successfully registered."));
        }
    }

    private void loadManagers() {
        TeamDataManager teamDataManager = new TeamDataManager(plugin, xTeamsAPI);
        BossKillManager bossKillManager = new BossKillManager(xTeamsAPI, teamDataManager, broadcastManager);
    }
    private void loadListeners(PVPManager pvpManager) {
        plugin.getServer().getPluginManager().registerEvents(new PvpListener(pvpManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PortalEnterListener(teamDataManager, xTeamsAPI, chatUtils), plugin);
        plugin.getServer().getPluginManager().registerEvents(new BossKillListener(bossKillManager), plugin);
        plugin.getServer().getPluginManager().registerEvents(new WitherSkullListener(settingsUtils), plugin);
    }
    private void loadCommands() {
        plugin.getLogger().info(ChatUtils.formatColor("&eLoading SpeedrunBoss command..."));
        if (plugin.getCommand("speedrunboss") == null) {
            plugin.getLogger().severe(ChatUtils.formatColor("&c❌ Error: SpeedrunBoss command is not registered in plugin.yml"));
        } else {
            if (xTeamsAPI == null) {
                plugin.getLogger().warning(ChatUtils.formatColor("&6⚠ xTeamsAPI is not initialized. Commands will not work succesfully."));
            } else {
                plugin.getCommand("speedrunboss").setExecutor(new CommandHandler(chatUtils, xTeamsAPI, teamDataManager, configManager, gracePeriodManager, modifierManager, plugin, eventManager, pvpManager, broadcastManager, this, stunManager, countdownBossBarManager, cinematicManager, cinematicSequence));
                plugin.getCommand("speedrunboss").setTabCompleter(new CommandTabCompleter());}
            plugin.getLogger().info(ChatUtils.formatColor("✅ SpeedrunBoss command was successfully loaded."));
        }
    }

    private void loadPlaceholderAPI() {
        plugin.getLogger().info(ChatUtils.formatColor("&eLoading PlaceholderAPI..."));
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SpeedrunBossExtension(plugin, teamDataManager, xTeamsAPI, configManager).register();
            plugin.getLogger().info(ChatUtils.formatColor("&a✅ PlaceholderAPI detected. PAPI dependency successfully loaded."));
        } else {
            plugin.getLogger().warning(ChatUtils.formatColor("&6⚠ PlaceholderAPI not detected. Disabling plugin..."));
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }
    private void loadVault() {
        plugin.getLogger().info(ChatUtils.formatColor("&eLoading Vault..."));
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            plugin.getLogger().info(ChatUtils.formatColor("&a✅ Vault detected. Vault dependency successfully loaded."));
        } else {
            plugin.getLogger().warning(ChatUtils.formatColor("&6⚠ Vault not detected. Disabling plugin..."));
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public void unloadTasks() {
        plugin.getLogger().info(ChatUtils.formatColor("&eUnloading Tasks..."));
        if (gracePeriodManager != null && gracePeriodManager.gracePeriodTask != null) {
            gracePeriodManager.gracePeriodTask.cancel();
            plugin.getLogger().info(ChatUtils.formatColor("&a✅ Grace Period task was successfully canceled."));
        }
        if (pvpManager != null && pvpManager.pvpTask != null) {
            pvpManager.pvpTask.cancel();
            plugin.getLogger().info(ChatUtils.formatColor("&a✅ PvP enabling task was successfully canceled."));
        }
        if (countdownBossBarManager != null && countdownBossBarManager.bossbarTask != null) {
            countdownBossBarManager.bossbarTask.cancel();
            plugin.getLogger().info(ChatUtils.formatColor("&a✅ BossBar Countdown was successfully canceled."));
        }
    }
}

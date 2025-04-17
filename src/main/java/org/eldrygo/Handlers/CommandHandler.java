package org.eldrygo.Handlers;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.eldrygo.Cinematics.Managers.CinematicManager;
import org.eldrygo.Cinematics.Managers.CountdownBossBarManager;
import org.eldrygo.Cinematics.Managers.FireworkManager;
import org.eldrygo.Cinematics.Models.CinematicSequence;
import org.eldrygo.Compass.Managers.CompassManager;
import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Managers.StunManager;
import org.eldrygo.Menu.Inventory.Managers.SPBInventoryManager;
import org.eldrygo.Menu.Inventory.Model.InventoryPlayer;
import org.eldrygo.Modifiers.Managers.GracePeriodManager;
import org.eldrygo.Modifiers.Managers.PVPManager;
import org.eldrygo.Spawn.Managers.SpawnManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Time.Managers.TimeBarManager;
import org.eldrygo.Time.Managers.TimeManager;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.Utils.LoadUtils;
import org.eldrygo.Utils.PlayerUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.eldrygo.XTeams.Models.Team;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor {
    private final ChatUtils chatUtils;
    private final XTeamsAPI xTeamsAPI;
    private final TeamDataManager teamDataManager;
    private final ConfigManager configManager;
    private final GracePeriodManager gracePeriodManager;
    private final SpeedrunBoss plugin;
    private final EventManager eventManager;
    private final PVPManager pvpManager;
    private final BroadcastManager broadcastManager;
    private final LoadUtils loadUtils;
    private final StunManager stunManager;
    private final CountdownBossBarManager countdownBossBarManager;
    private final CinematicManager cinematicManager;
    private final CompassManager compassManager;
    private final PlayerUtils playerUtils;
    private final SPBInventoryManager spbInventoryManager;
    private final SpawnManager spawnManager;
    private final TimeManager timeManager;
    private final TimeBarManager timeBarManager;
    private Map<String, String> pendingTaskCancellations = new HashMap<>();

    public CommandHandler(ChatUtils chatUtils, XTeamsAPI xTeamsAPI, TeamDataManager teamDataManager, ConfigManager configManager, GracePeriodManager gracePeriodManager, SpeedrunBoss plugin, EventManager eventManager, PVPManager pvpManager, BroadcastManager broadcastManager, LoadUtils loadUtils, StunManager stunManager, CountdownBossBarManager countdownBossBarManager, CinematicManager cinematicManager, CompassManager compassManager, PlayerUtils playerUtils, SPBInventoryManager spbInventoryManager, SpawnManager spawnManager, TimeManager timeManager, TimeBarManager timeBarManager) {
        this.chatUtils = chatUtils;
        this.xTeamsAPI = xTeamsAPI;
        this.teamDataManager = teamDataManager;
        this.configManager = configManager;
        this.gracePeriodManager = gracePeriodManager;
        this.plugin = plugin;
        this.eventManager = eventManager;
        this.pvpManager = pvpManager;
        this.broadcastManager = broadcastManager;
        this.loadUtils = loadUtils;
        this.stunManager = stunManager;
        this.countdownBossBarManager = countdownBossBarManager;
        this.cinematicManager = cinematicManager;
        this.compassManager = compassManager;
        this.playerUtils = playerUtils;
        this.spbInventoryManager = spbInventoryManager;
        this.spawnManager = spawnManager;
        this.timeManager = timeManager;
        this.timeBarManager = timeBarManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            if (sender.hasPermission("spb.menu")) {
                handleSPBMenu(sender);
            } else {
                handleSPBInfo(sender);
            }
        }
        if (args.length > 0) {
            String subcommand = args[0].toLowerCase();

            if (subcommand.equals("admin")) {
                return handleAdmin(sender, args);
            }
            if (sender.hasPermission("spb.admin")) {
                sender.sendMessage(chatUtils.getMessage("error.unknown_command", (Player) sender));
            }
        }
        return false;
    }
    private void handleSPBMenu(CommandSender sender) {
        if (sender instanceof Player) {
            spbInventoryManager.openMainInventory(new InventoryPlayer((Player) sender));
        } else {
            sender.sendMessage(chatUtils.getMessage("administration.menu.only_players", null));
        }
    }
    private void handleSPBInfo(CommandSender sender) {
        List<String> spbInfo = chatUtils.getMessageConfig().getStringList("playerutils.spbinfo.string");
        Player target;
        if (!(sender instanceof Player)) {
            target = null;
            sender.sendMessage(chatUtils.getMessage("error.only_by_player", target));
        } else {
            target = (Player) sender;
            if (spbInfo.isEmpty()) {
                sender.sendMessage(chatUtils.getMessage("error.no_spb_info", target));
                return;
            }
            for (String line : spbInfo) {
                String teamName = xTeamsAPI.getPlayerTeamName(target);
                String teamDisplayName = xTeamsAPI.getPlayerTeamDisplayName(target);

                String formattedLine = PlaceholderAPI.setPlaceholders(target, line);
                String wardenKilled = teamDataManager.hasKilledBoss(teamName, "warden") ? "#a0ff72✔" : "#ff7272✖";
                String witherKilled = teamDataManager.hasKilledBoss(teamName, "wither") ? "#a0ff72✔" : "#ff7272✖";
                String elderGuardianKilled = teamDataManager.hasKilledBoss(teamName, "elder_guardian") ? "#a0ff72✔" : "#ff7272✖";
                String enderDragonKilled = teamDataManager.hasKilledBoss(teamName, "ender_dragon") ? "#a0ff72✔" : "#ff7272✖";


                // Asegurarse de que `getTeamMembers` devuelva una lista
                List<String> teamMembers = new ArrayList<>(xTeamsAPI.getTeamMembers(teamName));  // Convertir el Set a List si es necesario

                // Aplicar formato a cada miembro
                String noMembersMsg = ChatUtils.formatColor(configManager.getMessageString("playerutils.spbinfo.no_members"));
                String memberFormat = configManager.getMessageString("playerutils.spbinfo.members");
                String separator = configManager.getMessageString("playerutils.spbinfo.members_separator");
                String teamMembersFormatted = teamMembers.isEmpty() ? noMembersMsg :
                        teamMembers.stream()
                                .map(member -> memberFormat.replace("%member_name%", member))
                                .collect(Collectors.joining(separator));

                teamDisplayName = ChatUtils.formatColor(teamDisplayName);
                wardenKilled = ChatUtils.formatColor(wardenKilled);
                witherKilled = ChatUtils.formatColor(witherKilled);
                elderGuardianKilled = ChatUtils.formatColor(elderGuardianKilled);
                enderDragonKilled = ChatUtils.formatColor(enderDragonKilled);
                teamMembersFormatted = ChatUtils.formatColor(teamMembersFormatted);

                sender.sendMessage(ChatUtils.formatColor(formattedLine)
                        .replace("%team_name%", teamName)
                        .replace("%team_display_name%", teamDisplayName)
                        .replace("%team_members%", teamMembersFormatted)
                        .replace("%warden_killed%", wardenKilled)
                        .replace("%wither_killed%", witherKilled)
                        .replace("%elderguardian_killed%", elderGuardianKilled)
                        .replace("%enderdragon_killed%", enderDragonKilled));
            }
        }
    }
    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(chatUtils.getMessage("error.unknown_command", null));
            } else {
                sender.sendMessage(chatUtils.getMessage("error.unknown_command", (Player) sender));
            }
            return false;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "graceperiod" -> {
                if (!sender.hasPermission("spb.admin.graceperiod") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    handleGracePeriod(sender, args);
                }
            }
            case "pvp" -> {
                if (!sender.hasPermission("spb.admin.pvp") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handlePvP(sender, args);
                }
            }
            case "stun" -> {
                if (!sender.hasPermission("spb.admin.stun") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleStun(sender, args);
                }
            }
            case "event" -> {
                if (!sender.hasPermission("spb.admin.event") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    handleEvent(sender, args);
                }
            }
            case "broadcast" -> {
                if (!sender.hasPermission("spb.admin.broadcast") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleBroadcast(sender, args);
                }
            }
            case "spawn" -> {
                if (!sender.hasPermission("spb.admin.spawn") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    handleSpawn(sender, args);
                }
            }
            case "killerhandle" -> {
                if (!sender.hasPermission("spb.admin.killerhandle") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleKillerHandle(sender, args);
                }
            }
            case "givecompass" -> {
                if (!sender.hasPermission("spb.admin.givecompass") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleCompass(sender, args);
                }
            }
            case "task" -> {
                if (!sender.hasPermission("spb.admin.task") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleTask(sender, args);
                }
            }
            case "cinematic" -> {
                if (!sender.hasPermission("spb.admin.cinematic") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleCinematic(sender, args);
                }
            }
            case "bossregister" -> {
                if (!sender.hasPermission("spb.admin.bossregister") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleBossRegister(sender, args);
                }
            }
            case "dimregister" -> {
                if (!sender.hasPermission("spb.admin.dimregister") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleDimRegister(sender, args);
                }
            }
            case "time" -> {
                if (!sender.hasPermission("spb.admin.time") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    handleTime(sender, args);
                }
            }
            case "reload" -> {
                if (!sender.hasPermission("spb.admin.reload") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleReload(sender);
                }
            }
            case "reset" -> {
                if (!sender.hasPermission("spb.admin.reset") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    handleReset(sender);
                }
            }
            case "help" -> {
                if (!sender.hasPermission("spb.admin.help") && !sender.hasPermission("spb.admin") && !(sender.isOp())) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleHelp(sender);
                }
            }
        }
        return false;
    }
    private void handleTime(CommandSender sender, String[] args) {
        Player target;
        if (!(sender instanceof Player)) {
            target = null;
        } else {
            target = (Player) sender;
        }
        if (args.length == 2) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
        }
        String scope = args[2].toLowerCase();
        switch (scope) {
            case "time" -> {
                String action = args[3].toLowerCase();
                switch (action) {
                    case "start" -> {
                        timeManager.start();
                        sender.sendMessage(chatUtils.getMessage("administration.time.time.start", null));
                    }
                    case "stop" -> {
                        timeManager.stop();
                        sender.sendMessage(chatUtils.getMessage("administration.time.time.pause", null));
                    }
                    case "resume" -> {
                        timeManager.resume();
                        sender.sendMessage(chatUtils.getMessage("administration.time.time.resume", null));
                    }
                    case "pause" -> {
                        timeManager.pause();
                        sender.sendMessage(chatUtils.getMessage("administration.time.time.pause", null));
                    }
                }
            }
            case "bossbar" -> {
                String action = args[3].toLowerCase();
                switch (action) {
                    case "show" -> {
                        sender.sendMessage(chatUtils.getMessage("administration.time.bossbar.show", null));
                        timeBarManager.showAllBars();
                    }
                    case "hide" -> {
                        sender.sendMessage(chatUtils.getMessage("administration.time.bossbar.hide", null));
                        timeBarManager.hideAllBars();
                    }
                }
            }
        }
    }
    private boolean handleBroadcast(CommandSender sender, String[] args) {
        Player target = (sender instanceof Player) ? (Player) sender : null;

        if (args.length < 3) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
            return false;
        }

        // Une todos los argumentos desde el tercero y convierte \n en salto real
        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        message = message.replace("\\n", "\n");

        broadcastManager.sendCommandBroadcastMessage(target, message);
        return true;
    }
    private void handleSpawn(CommandSender sender, String[] args) {
        Player player = (sender instanceof Player p) ? p : null;

        if (args.length < 3) {
            sender.sendMessage(chatUtils.getMessage("administration.spawn.error.usage", null));
            return;
        }

        String action = args[2].toLowerCase();

        switch (action) {
            case "set" -> {
                if (args.length < 4) {
                    sender.sendMessage(chatUtils.getMessage("administration.spawn.set.usage", null));
                    return;
                }

                if (player == null) {
                    sender.sendMessage(chatUtils.getMessage("administration.spawn.error.only_players", null));
                    return;
                }

                String setType = args[3].toLowerCase();
                switch (setType) {
                    case "global" -> {
                        spawnManager.setGlobalSpawn(player.getLocation());
                        sender.sendMessage(chatUtils.getMessage("administration.spawn.set.global.success", null));
                    }

                    case "team" -> {
                        if (args.length < 5) {
                            sender.sendMessage(chatUtils.getMessage("administration.spawn.set.team.usage", null));
                            return;
                        }
                        String team = args[4];
                        spawnManager.setTeamSpawn(team, player.getLocation());
                        sender.sendMessage(chatUtils.getMessage("administration.spawn.set.team.success", null).replace("%team%", team));
                    }

                    default -> sender.sendMessage(chatUtils.getMessage("administration.spawn.set.usage", null));
                }
            }

            case "tp" -> {
                if (args.length < 4) {
                    sender.sendMessage(chatUtils.getMessage("administration.spawn.tp.usage", null));
                    return;
                }

                String type = args[3].toLowerCase();

                switch (type) {
                    case "global" -> {
                        Location loc = spawnManager.getGlobalSpawn();
                        if (loc == null) {
                            sender.sendMessage(chatUtils.getMessage("administration.spawn.error.global_spawn_not_defined", null));
                            return;
                        }

                        if (args.length == 4) {
                            if (player == null) {
                                sender.sendMessage(chatUtils.getMessage("administration.spawn.error.only_players", null));
                                return;
                            }

                            player.teleport(loc);
                            sender.sendMessage(chatUtils.getMessage("administration.spawn.teleport.global.success", null));
                            return;
                        }

                        String target = args[4];

                        if (target.equalsIgnoreCase("*")) {
                            spawnManager.teleportAllToGlobalSpawn();
                            sender.sendMessage(chatUtils.getMessage("administration.spawn.teleport.global.all.success", null));
                            return;
                        }

                        Team team = plugin.getTeamManager().getTeam(target);
                        if (team != null) {
                            for (String member : team.getMembers()) {
                                Player memberPlayer = Bukkit.getPlayerExact(member);
                                if (memberPlayer != null && memberPlayer.isOnline()) {
                                    memberPlayer.teleport(loc);
                                    memberPlayer.sendMessage(chatUtils.getMessage("spawn.teleport.global", null));
                                }
                            }
                            sender.sendMessage(chatUtils.getMessage("administration.spawn.teleport.global.team.success", null).replace("%team%", team.getName()));
                            return;
                        }

                        Player targetPlayer = Bukkit.getPlayerExact(target);
                        if (targetPlayer != null && targetPlayer.isOnline()) {
                            targetPlayer.teleport(loc);
                            targetPlayer.sendMessage(chatUtils.getMessage("spawn.teleport.global", null));
                            sender.sendMessage(chatUtils.getMessage("administration.spawn.teleport.global.player.success", null).replace("%target%", targetPlayer.getName()));
                            return;
                        }

                        sender.sendMessage(chatUtils.getMessage("error.player_not_found", null));
                    }

                    case "team" -> {
                        if (args.length < 5) {
                            sender.sendMessage(chatUtils.getMessage("administration.spawn.tp.usage", null));
                            return;
                        }

                        if (args[3].equalsIgnoreCase("self")) {
                            if (player == null) {
                                sender.sendMessage(chatUtils.getMessage("administration.spawn.error.only_players", null));
                                return;
                            }

                            String targetTeam = args[4];
                            Location loc = spawnManager.getTeamSpawn(targetTeam);
                            if (loc == null) {
                                sender.sendMessage(chatUtils.getMessage("administration.spawn.error.team_spawn_not_defined", null).replace("%team%", targetTeam));
                                return;
                            }

                            player.teleport(loc);
                            sender.sendMessage(chatUtils.getMessage("administration.spawn.teleport.team.self.success", null).replace("%team%", targetTeam));
                            return;
                        }

                        String target = args[4];

                        if (target.equalsIgnoreCase("*")) {
                            for (Team t : plugin.getTeamManager().getAllTeams()) {
                                spawnManager.teleportTeam(t.getName());
                            }
                            sender.sendMessage(chatUtils.getMessage("administration.spawn.teleport.team.all.success", null));
                            return;
                        }

                        Player targetPlayer = Bukkit.getPlayerExact(target);
                        if (targetPlayer != null && targetPlayer.isOnline()) {
                            String teamName = xTeamsAPI.getPlayerTeamName(targetPlayer);
                            if (teamName == null) {
                                sender.sendMessage(chatUtils.getMessage("administration.spawn.error.player_not_in_a_team", null).replace("%target%", targetPlayer.getName()));
                                return;
                            }

                            Location loc = spawnManager.getTeamSpawn(teamName);
                            if (loc == null) {
                                sender.sendMessage(chatUtils.getMessage("administration.spawn.error.team_spawn_not_defined", null).replace("%team%", teamName));
                                return;
                            }

                            targetPlayer.teleport(loc);
                            sender.sendMessage(chatUtils.getMessage("administration.spawn.teleport.team.player.success", null).replace("%target%", targetPlayer.getName()));
                            return;
                        }

                        // Try as team name
                        boolean success = spawnManager.teleportTeam(target);
                        if (!success) {
                            sender.sendMessage(chatUtils.getMessage("administration.spawn.error.team_spawn_not_defined", null).replace("%team%", target));
                            return;
                        }

                        sender.sendMessage(chatUtils.getMessage("administration.spawn.teleport.team.success", null).replace("%team%", target));
                    }

                    default -> sender.sendMessage(chatUtils.getMessage("administration.spawn.tp.usage", null));
                }
            }

            default -> sender.sendMessage(chatUtils.getMessage("administration.spawn.error.usage", null));
        }
    }

    private void handleEvent(CommandSender sender, String[] args) {
        Player target;
        if (!(sender instanceof Player)) {
            target = null;
        } else {
            target = (Player) sender;
        }
        if (args.length == 2) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
        }
        String action = args[2].toLowerCase();
        switch (action) {
            case "start" -> eventManager.startEvent(target);
            case "end" -> eventManager.endEvent(target);
            case "pause" -> eventManager.pauseEvent(target);
            case "resume" -> eventManager.resumeEvent(target);
        }
    }
    private boolean handlePvP(CommandSender sender, String[] args) {
        Player target = (sender instanceof Player) ? (Player) sender : null;
        if (args.length < 3) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
            return false;
        }
        String action = args[2].toLowerCase();
        boolean silent = args.length >= 4 && args[3].equalsIgnoreCase("silent");
        switch (action) {
            case "enable" -> {
                pvpManager.forceStartPvP();
                sender.sendMessage(chatUtils.getMessage("administration.pvp.enable", target));
                if (!silent) {
                    broadcastManager.sendPVPEnableMessage();
                }
            }
            case "disable" -> {
                pvpManager.stopPvP();
                sender.sendMessage(chatUtils.getMessage("administration.pvp.disable", target));
                if (!silent) {
                    broadcastManager.sendPVPDisableMessage();
                }
            }
            default -> {
                sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
                return false;
            }
        }
        return true;
    }
    private boolean handleStun(CommandSender sender, String[] args) {
        Player target = (sender instanceof Player) ? (Player) sender : null;
        if (args.length < 3) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
            return false;
        }
        String action = args[2].toLowerCase();
        switch (action) {
            case "enable" -> {
                stunManager.stunAllPlayers();
                sender.sendMessage(chatUtils.getMessage("administration.stun.enable", target));
            }
            case "disable" -> {
                stunManager.unStunAllPlayers();
                sender.sendMessage(chatUtils.getMessage("administration.stun.disable", target));
            }
            case "player" -> {
                if (args.length < 5) {
                    sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
                    return false;
                }

                Player targetCmd = Bukkit.getPlayer(args[3]);
                if (targetCmd != null && targetCmd.isOnline()) {
                    String subaction = args[4].toLowerCase();
                    switch (subaction) {
                        case "true" -> {
                            boolean withTime = args.length >= 6;
                            if (withTime) {
                                try {
                                    long time = Long.parseLong(args[5]);
                                    stunManager.stunPlayer(targetCmd, time);
                                    sender.sendMessage(chatUtils.getMessage("administration.stun.player.true.withtime_success", target)
                                            .replace("%target%", targetCmd.getName())
                                            .replace("%time%", String.valueOf(time)));
                                } catch (NumberFormatException e) {
                                    sender.sendMessage(chatUtils.getMessage("error.invalid_number", target));
                                }
                            } else {
                                stunManager.stunPlayerIndefinitely(targetCmd);
                                sender.sendMessage(chatUtils.getMessage("administration.stun.player.true.success", target)
                                        .replace("%target%", targetCmd.getName()));
                            }
                        }
                        case "false" -> {
                            stunManager.unStunPlayer(targetCmd);
                            sender.sendMessage(chatUtils.getMessage("administration.stun.player.false.success", target)
                                    .replace("%target%", targetCmd.getName()));
                        }
                        default -> sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
                    }
                } else {
                    sender.sendMessage(chatUtils.getMessage("administration.stun.player_not_found", target));
                }
            }
            default -> {
                sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
                return false;
            }
        }
        return true;
    }
    private void handleGracePeriod(CommandSender sender, String[] args) {
        Player target;
        if (!(sender instanceof Player)) {
            target = null;
        } else {
            target = (Player) sender;
        }
        if (args.length == 2) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
        }

        String action = args[2].toLowerCase();
        switch (action) {
            case "forcestart" -> {
                if (args.length < 4) {
                    sender.sendMessage(chatUtils.getMessage("error.invalid_usage", target));
                } else {
                    if (gracePeriodManager.isGracePeriodActive()) {
                        sender.sendMessage(chatUtils.getMessage("administration.graceperiod.forcestart.already", target));
                    } else {
                        GracePeriodManager gracePeriodManager = new GracePeriodManager(plugin, xTeamsAPI, broadcastManager, chatUtils, playerUtils, timeManager);
                        gracePeriodManager.startGracePeriod();
                        sender.sendMessage(chatUtils.getMessage("administration.graceperiod.forcestart.success", target));
                    }
                }
            }
            case "forcestop" -> {
                if (gracePeriodManager != null) {
                    gracePeriodManager.stopGracePeriod();
                    sender.sendMessage(chatUtils.getMessage("administration.graceperiod.forcestop.success", target));
                } else {
                    sender.sendMessage(chatUtils.getMessage("administration.graceperiod.forcestop.already", target));
                }
            }
        }
    }
    private boolean handleKillerHandle(CommandSender sender, String[] args) {
        Player msgTarget;
        if (!(sender instanceof Player)) {
            msgTarget = null;
        } else {
            msgTarget = (Player) sender;
        }
        if (args.length < 3) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(chatUtils.getMessage("error.only_by_player", msgTarget));
            } else {
                giveKillerHandle((Player) sender);
                plugin.getLogger().info("⚠ The player " + sender + " received the Killer Handle by himself.");
                sender.sendMessage(chatUtils.getMessage("administration.killer_handle.give_self", (Player) sender));
            }
            return false;
        } else {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(chatUtils.getMessage("error.player_not_found", target));
                return true;
            } else {
                giveKillerHandle(target);
                plugin.getLogger().info("⚠ The player " + target + " received the Killer Handle by " + sender + ".");
                sender.sendMessage(chatUtils.getMessage("administration.killer_handle.give_other", target));
                return false;
            }
        }
    }
    private void giveKillerHandle(Player target) {
        // Crea la espada (puedes cambiar el Material a otro tipo si lo deseas)
        ItemStack sword = new ItemStack(Material.GOLDEN_SWORD);

        // Obtén el ItemMeta de la espada para poder modificar su nombre y encantamientos
        ItemMeta meta = sword.getItemMeta();

        if (meta != null) {
            // Establecer el nombre de la espada
            meta.setDisplayName(ChatUtils.formatColor("&r&f%prefix% #ffd085Kill Handle ⚔").replace("%prefix%", plugin.prefix));

            // Agregar el encantamiento
            meta.addEnchant(Enchantment.SHARPNESS, 999, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            // Establecer el ItemMeta de la espada
            sword.setItemMeta(meta);
        }

        // Darle la espada al jugador
        target.getInventory().addItem(sword);
    }
    private boolean handleCompass(CommandSender sender, String[] args) {
        Player msgTarget;
        if (!(sender instanceof Player)) {
            msgTarget = null;
        } else {
            msgTarget = (Player) sender;
        }
        if (args.length < 3) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(chatUtils.getMessage("error.only_by_player", msgTarget));
            } else {
                compassManager.giveTrackingCompass((Player) sender);
                sender.sendMessage(chatUtils.getMessage("commands.compass.give.success", (Player) sender));
            }
            return false;
        } else {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(chatUtils.getMessage("error.player_not_found", target));
                return true;
            } else {
                compassManager.giveTrackingCompass(target);
                sender.sendMessage(chatUtils.getMessage("commands.compass.give.success_other", null));
                return false;
            }
        }
    }
    private boolean handleCinematic(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", getTargetPlayer(sender)));
            return false;
        }

        String action = args[2].toLowerCase();

        switch (action) {
            case "start" -> {
                if (args.length < 4) {
                    sender.sendMessage(chatUtils.getMessage("error.invalid_usage", getTargetPlayer(sender)));
                    return false;
                }

                String cinematic = args[3].toLowerCase();
                if (!cinematicManager.getCinematicList().contains(cinematic)) {
                    sender.sendMessage(chatUtils.getMessage("administration.cinematic.start.not_found", getTargetPlayer(sender))
                            .replace("%cinematic%", cinematic));
                    return false;
                }

                return handleStartCinematic(sender, cinematic);
            }

            case "stop" -> {
                if (args.length >= 4 && "confirm".equalsIgnoreCase(args[3])) {
                    return confirmStopCinematic(sender);
                }
                handleStopCinematic(sender);
            }

            default -> {
                sender.sendMessage(chatUtils.getMessage("error.invalid_usage", getTargetPlayer(sender)));
                return false;
            }
        }
        return false;
    }
    private Player getTargetPlayer(CommandSender sender) {
        return (sender instanceof Player player) ? player : null;
    }
    private boolean handleStartCinematic(CommandSender sender, String cinematic) {
        CinematicSequence current = cinematicManager.getRunningSequence();
        if (current != null) {
            sender.sendMessage(chatUtils.getMessage("administration.cinematic.start.cinematic_already_running", getTargetPlayer(sender)));
            return false;
        }

        cinematicManager.startCinematic(cinematic);

        // Si después de iniciar no hay secuencia activa, asumimos que algo falló
        if (cinematicManager.getRunningSequence() == null) {
            sender.sendMessage(chatUtils.getMessage("administration.cinematic.start.failed", getTargetPlayer(sender)));
            return false;
        }

        sender.sendMessage(chatUtils.getMessage("administration.cinematic.start.success", getTargetPlayer(sender))
                .replace("%cinematic%", cinematic));
        return true;
    }
    private void handleStopCinematic(CommandSender sender) {
        CinematicSequence sequence = cinematicManager.getRunningSequence();
        if (sequence == null) {
            sender.sendMessage(chatUtils.getMessage("administration.cinematic.stop.no_cinematic_running", getTargetPlayer(sender)));
            return;
        }

        String cinematic = sequence.getCinematicId();

        if (sender instanceof Player player) {
            sendStopConfirmationRequest(player, cinematic);
        } else {
            confirmStopCinematic(sender);
        }
    }
    private boolean confirmStopCinematic(CommandSender sender) {
        CinematicSequence sequence = cinematicManager.getRunningSequence();
        if (sequence == null) {
            sender.sendMessage(chatUtils.getMessage("administration.cinematic.stop.no_cinematic_running", getTargetPlayer(sender)));
            return false;
        }

        String cinematic = sequence.getCinematicId();

        sequence.stop();
        cinematicManager.stopCinematic();
        countdownBossBarManager.bossBar.setVisible(false);
        countdownBossBarManager.voidBossBar.setVisible(false);

        sender.sendMessage(chatUtils.getMessage("administration.cinematic.stop.success", getTargetPlayer(sender))
                .replace("%cinematic%", cinematic));
        return true;
    }
    private void sendStopConfirmationRequest(Player player, String cinematic) {
        List<String> confirmationLines = chatUtils.getMessageConfig()
                .getStringList("administration.cinematic.stop.confirmation_request");

        if (confirmationLines.isEmpty()) {
            confirmationLines = List.of(
                    " ",
                    "%prefix% #FF3535&lCinematic Stop Confirmation ⚠",
                    " ",
                    " #ccccccYou are about to stop the cinematic &f%cinematic%#cccccc.",
                    " &7For security reasons, we need you to confirm the action of stopping it.",
                    " ",
                    "  #fff18d&lTo confirm, type &r#fff9cc/spb admin cinematic stop confirm",
                    " ",
                    "#FF4444&o(This action can't be undone.)",
                    " "
            );
        }

        for (String line : confirmationLines) {
            player.sendMessage(ChatUtils.formatColor(line
                    .replace("%cinematic%", cinematic)
                    .replace("%prefix%", plugin.prefix)));
        }
    }
    private boolean handleTask(CommandSender sender, String[] args) {
        boolean isConsole = !(sender instanceof Player);
        String senderId = isConsole ? "CONSOLE" : ((Player) sender).getUniqueId().toString();

        if (args.length < 4) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", isConsole ? null : (Player) sender));
            return false;
        }

        String action = args[2].toLowerCase();
        String task = args[3].toLowerCase();

        switch (action) {
            case "cancel" -> {
                boolean taskExists = switch (task) {
                    case "graceperiod" -> gracePeriodManager.gracePeriodTask != null;
                    case "pvp" -> pvpManager.pvpTask != null;
                    default -> false;
                };

                if (!taskExists) {
                    sender.sendMessage(chatUtils.getMessage("administration.task.cancel.task_not_initialized", isConsole ? null : (Player) sender)
                            .replace("%task%", task));
                    return false;
                }

                pendingTaskCancellations.put(senderId, task);
                List<String> confirmationLines = chatUtils.getMessageConfig().getStringList("administration.task.cancel.confirmation_request");

                if (confirmationLines.isEmpty()) {
                    confirmationLines = List.of(
                            " ",
                            "%prefix% #FF3535&lTask Cancel Confirmation ⚠",
                            " ",
                            " #ccccccYou are about to cancel the task &f%task%#cccccc.",
                            " &7For security reasons, we need you to confirm the action of canceling the task.",
                            " ",
                            "  #fff18d&lTo confirm, type &r#fff9cc/spb admin task confirm &r%task%",
                            " ",
                            "#FF4444&o(This action can't be undone.)",
                            " "
                    );
                }

                for (String line : confirmationLines) {
                    sender.sendMessage(ChatUtils.formatColor(line
                            .replace("%task%", task)
                            .replace("%prefix%", plugin.prefix)));
                }
            }

            case "confirm" -> {
                String pending = pendingTaskCancellations.get(senderId);
                if (pending == null || !pending.equalsIgnoreCase(task)) {
                    sender.sendMessage(chatUtils.getMessage("administration.task.cancel.no_pending_confirmation", isConsole ? null : (Player) sender));
                    return false;
                }

                pendingTaskCancellations.remove(senderId);
                switch (task) {
                    case "graceperiod" -> {
                        gracePeriodManager.gracePeriodTask.cancel();
                        gracePeriodManager.gracePeriodTask = null;
                    }
                    case "pvp" -> {
                        pvpManager.pvpTask.cancel();
                        pvpManager.pvpTask = null;
                    }
                    default -> {
                        sender.sendMessage(chatUtils.getMessage("administration.task.cancel.task_not_found", isConsole ? null : (Player) sender)
                                .replace("task", task));
                        return false;
                    }
                }

                sender.sendMessage(chatUtils.getMessage("administration.task.cancel.success", isConsole ? null : (Player) sender)
                        .replace("%task%", task));
                return true;
            }

            default -> {
                sender.sendMessage(chatUtils.getMessage("error.invalid_usage", isConsole ? null : (Player) sender));
                return false;
            }
        }
        return false;
    }
    private boolean handleBossRegister(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", sender instanceof Player player ? player : null));
            return false;
        }

        String teamName = args[2];
        String bossName = args[3].toLowerCase();
        String valueStr = args[4].toLowerCase();

        // Validar valor booleano
        if (!valueStr.equals("true") && !valueStr.equals("false")) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_boolean", sender instanceof Player player ? player : null));
            return false;
        }
        boolean value = Boolean.parseBoolean(valueStr);

        // Validar nombre del boss
        List<String> validBosses = List.of("wither", "warden", "elder_guardian", "ender_dragon");
        if (!validBosses.contains(bossName)) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_boss", sender instanceof Player player ? player : null)
                    .replace("%boss%", bossName));
            return false;
        }

        Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            sender.sendMessage(chatUtils.getMessage("error.only_players_can_use_this", null));
            return false;
        }

        // Verificar si se pidió confirmación
        if (args.length >= 6 && args[5].equalsIgnoreCase("confirm")) {
            // Aquí se procederá a registrar el boss para el equipo
            if (value) {
                teamDataManager.addKilledBoss(teamName, bossName);
            } else {
                teamDataManager.delKilledBoss(teamName, bossName);
            }

            sender.sendMessage(chatUtils.getMessage("administration.bossregister.success", player)
                    .replace("%team%", teamName)
                    .replace("%boss%", bossName)
                    .replace("%value%", String.valueOf(value)));
            return true;
        }

        // Si no hay confirmación, enviar mensaje de confirmación
        sendBossRegisterConfirmationRequest(player, teamName, bossName, value);
        return false;
    }
    private void sendBossRegisterConfirmationRequest(Player player, String teamName, String bossName, boolean value) {
        List<String> confirmationLines = chatUtils.getMessageConfig()
                .getStringList("administration.bossregister.confirmation_request");

        if (confirmationLines.isEmpty()) {
            confirmationLines = List.of(
                    " ",
                    "%prefix% #FF3535&lBoss Registration Confirmation ⚠",
                    " ",
                    " #ccccccYou are about to register the boss &f%boss%#cccccc for team &f%team%#cccccc.",
                    " &7For security reasons, we need you to confirm the action of registering it.",
                    " ",
                    "  #fff18d&lTo confirm, type &r#fff9cc/spb admin bossregister %team% %boss% %value% confirm",
                    " ",
                    "#FF4444&o(This action can't be undone.)",
                    " "
            );
        }

        for (String line : confirmationLines) {
            player.sendMessage(ChatUtils.formatColor(line
                    .replace("%team%", teamName)
                    .replace("%boss%", bossName)
                    .replace("%value%", String.valueOf(value))
                    .replace("%prefix%", plugin.prefix)));
        }
    }
    private boolean handleDimRegister(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", sender instanceof Player player ? player : null));
            return false;
        }

        String teamName = args[2];
        String dimName = args[3].toLowerCase();
        String valueStr = args[4].toLowerCase();

        // Validar valor booleano
        if (!valueStr.equals("true") && !valueStr.equals("false")) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_boolean", sender instanceof Player player ? player : null));
            return false;
        }
        boolean value = Boolean.parseBoolean(valueStr);

        // Validar nombre del boss
        List<String> validDimensions = List.of("end", "nether");
        if (!validDimensions.contains(dimName)) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_dimension", sender instanceof Player player ? player : null)
                    .replace("%dimension%", dimName));
            return false;
        }

        Player player = sender instanceof Player ? (Player) sender : null;
        if (player == null) {
            sender.sendMessage(chatUtils.getMessage("error.only_players_can_use_this", null));
            return false;
        }

        // Verificar si se pidió confirmación
        if (args.length >= 6 && args[5].equalsIgnoreCase("confirm")) {
            // Aquí se procederá a registrar el boss para el equipo
            if (value) {
                teamDataManager.addDimensionRegister(teamName, dimName);
            } else {
                teamDataManager.delDimensionRegister(teamName, dimName);
            }

            sender.sendMessage(chatUtils.getMessage("administration.dimregister.success", player)
                    .replace("%team%", teamName)
                    .replace("%dimension%", dimName)
                    .replace("%value%", String.valueOf(value)));
            return true;
        }

        // Si no hay confirmación, enviar mensaje de confirmación
        sendDimRegisterConfirmationRequest(player, teamName, dimName, value);
        return false;
    }
    private void sendDimRegisterConfirmationRequest(Player player, String teamName, String dimName, boolean value) {
        List<String> confirmationLines = chatUtils.getMessageConfig()
                .getStringList("administration.bossregister.confirmation_request");

        if (confirmationLines.isEmpty()) {
            confirmationLines = List.of(
                    " ",
                    "%prefix% #FF3535&lDimension Registration Confirmation ⚠",
                    " ",
                    " #ccccccYou are about to register the dimension &f%dimension%#cccccc for team &f%team%#cccccc.",
                    " &7For security reasons, we need you to confirm the action of registering it.",
                    " ",
                    "  #fff18d&lTo confirm, type &r#fff9cc/spb admin dimregister %team% %dimension% %value% confirm",
                    " ",
                    "#FF4444&o(This action can't be undone.)",
                    " "
            );
        }

        for (String line : confirmationLines) {
            player.sendMessage(ChatUtils.formatColor(line
                    .replace("%team%", teamName)
                    .replace("%dimension%", dimName)
                    .replace("%value%", String.valueOf(value))
                    .replace("%prefix%", plugin.prefix)));
        }
    }
    private boolean handleReload(CommandSender sender) {
        String executor;
        if (sender instanceof Player) {
            sender.sendMessage(chatUtils.getMessage("administration.reload_command.success", (Player) sender));
            executor = sender.getName();
        } else {
            executor = "console";
        }
        loadUtils.loadConfig();
        plugin.getLogger().info("✔  Sucessfully reloaded config by " + executor + ".");
        return false;
    }
    private boolean handleHelp(CommandSender sender) {
        sender.sendMessage(ChatUtils.formatColor("&7"));
        sender.sendMessage(ChatUtils.formatColor("                         &r&lSpeedrun#fd3c9e&lBoss &8» &r&fHelp"));
        sender.sendMessage(ChatUtils.formatColor("&7"));
        sender.sendMessage(ChatUtils.formatColor("                 #fff18d&lɪɴꜰᴏʀᴍᴀᴛɪᴏɴ ᴄᴏᴍᴍᴀɴᴅꜱ"));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ #707070» #ccccccDisplays player-need information."));
        sender.sendMessage(ChatUtils.formatColor("&7"));
        sender.sendMessage(ChatUtils.formatColor("                    #fff18d&lᴘʟᴜɢɪɴ ᴄᴏᴍᴍᴀɴᴅꜱ"));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ʜᴇʟᴘ #707070» #ccccccShows this help message"));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ʀᴇʟᴏᴀᴅ #707070» #ccccccReloads the plugin configuration"));
        sender.sendMessage(ChatUtils.formatColor("&7"));
        sender.sendMessage(ChatUtils.formatColor("                #fff18d&lᴀᴅᴍɪɴɪꜱᴛʀᴀᴛɪᴏɴ ᴄᴏᴍᴍᴀɴᴅꜱ"));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ᴘᴠᴘ <ᴇɴᴀʙʟᴇ / ᴅɪꜱᴀʙʟᴇ> <ꜱɪʟᴇɴᴛ> #707070- #ccccccManage the PvP status."));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ꜱᴛᴜɴ <ᴇɴᴀʙʟᴇ / ᴅɪꜱᴀʙʟᴇ/ ᴘʟᴀʏᴇʀ> <ᴘʟᴀʏᴇʀ> <ꜱᴛᴀᴛᴜꜱ> #707070- #ccccccManage the Stun on players."));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ᴇᴠᴇɴᴛ <ꜱᴛᴀʀᴛ / ᴘᴀᴜꜱᴇ / ʀᴇꜱᴜᴍᴇ / ᴇɴᴅ> #707070- #ccccccManage the event task."));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ɢʀᴀᴄᴇᴘᴇʀɪᴏᴅ <ꜰᴏʀᴄᴇꜱᴛᴀʀᴛ / ꜰᴏʀᴄᴇꜱᴛᴏᴘ> <ꜱᴇᴄᴏɴᴅꜱ> #707070- #ccccccManage the Grace Period."));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ʙʀᴏᴀᴅᴄᴀꜱᴛ <ᴍᴇꜱꜱᴀɢᴇ> #707070- #ccccccBroadcast the message of the arg."));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ɢɪᴠᴇᴄᴏᴍᴘᴀꜱꜱ <ᴘʟᴀʏᴇʀ> #707070- #ccccccGive the Structure compass."));
        sender.sendMessage(ChatUtils.formatColor("&f  /ꜱᴘʙ ᴀᴅᴍɪɴ ᴋɪʟʟᴇʀʜᴀɴᴅʟᴇ <ᴘʟᴀʏᴇʀ> #707070- #ccccccGive the Killer Handle."));
        sender.sendMessage(ChatUtils.formatColor("&7"));
        return false;
    }
    private void handleReset(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender) {
            plugin.getLogger().warning("Resetting all event features...");
            timeManager.stop();
            timeBarManager.hideAllBars();
            eventManager.endEvent(null);
            pvpManager.stopPvP();
            stunManager.unStunAllPlayers();
            if (gracePeriodManager != null) { gracePeriodManager.stopGracePeriod(); }
            CinematicSequence sequence = cinematicManager.getRunningSequence();
            if (sequence != null) {
                sequence.stop();
                cinematicManager.stopCinematic();
                countdownBossBarManager.bossBar.setVisible(false);
                countdownBossBarManager.voidBossBar.setVisible(false);
            }
            assert gracePeriodManager != null;
            gracePeriodManager.gracePeriodTask.cancel();
            gracePeriodManager.gracePeriodTask = null;
            pvpManager.pvpTask.cancel();
            pvpManager.pvpTask = null;
            teamDataManager.removeAllDimensionsAndBosses();
            sender.sendMessage(chatUtils.getMessage("administration.reset.success", null));
        } else {
            sender.sendMessage(chatUtils.getMessage("administration.reset.only_console", null));
            plugin.getLogger().warning("Player " + sender.getName() + " tried to use reset command.");
        }
    }
}

package org.eldrygo.Handlers;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.eldrygo.API.XTeamsAPI;
import org.eldrygo.Compass.Managers.CompassManager;
import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Modifiers.Managers.GracePeriodManager;
import org.eldrygo.Modifiers.Managers.ModifierManager;
import org.eldrygo.Modifiers.Managers.PVPManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class CommandHandler implements CommandExecutor {
    private final ChatUtils chatUtils;
    private final XTeamsAPI xTeamsAPI;
    private final TeamDataManager teamDataManager;
    private final ConfigManager configManager;
    private final GracePeriodManager gracePeriodManager;
    private final ModifierManager modifierManager;
    private final SpeedrunBoss plugin;
    private final EventManager eventManager;
    private final PVPManager pvpManager;
    private final BroadcastManager broadcastManager;

    public CommandHandler(ChatUtils chatUtils, XTeamsAPI xTeamsAPI, TeamDataManager teamDataManager, ConfigManager configManager, GracePeriodManager gracePeriodManager, ModifierManager modifierManager, SpeedrunBoss plugin, EventManager eventManager, PVPManager pvpManager, BroadcastManager broadcastManager) {
        this.chatUtils = chatUtils;
        this.xTeamsAPI = xTeamsAPI;
        this.teamDataManager = teamDataManager;
        this.configManager = configManager;
        this.gracePeriodManager = gracePeriodManager;
        this.modifierManager = modifierManager;
        this.plugin = plugin;
        this.eventManager = eventManager;
        this.pvpManager = pvpManager;
        this.broadcastManager = broadcastManager;
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            handleSPBInfo(sender);
            return false;
        }

        String subcommand = args[0].toLowerCase();

        if (subcommand.equals("admin")) {
            return handleAdmin(sender, args);
        }
        sender.sendMessage(chatUtils.getMessage("error.unknown_command", (Player) sender));
        return false;
    }

    private void handleSPBInfo(CommandSender sender) {
        List<String> spbInfo = chatUtils.getMessageConfig().getStringList("playerutils.spbinfo.string");
        if (spbInfo.isEmpty()) {
            sender.sendMessage(chatUtils.getMessage("error.no_spb_info", (Player) sender));
            return;
        }
        for (String line : spbInfo) {
            String teamName = xTeamsAPI.getPlayerTeamName((OfflinePlayer) sender);
            String teamDisplayName = xTeamsAPI.getPlayerTeamDisplayName((OfflinePlayer) sender);

            String formattedLine = PlaceholderAPI.setPlaceholders((OfflinePlayer) sender, line);
            String wardenKilled = teamDataManager.hasKilledBoss(teamName, "warden") ? "#a0ff72✔" : "#ff7272✖";
            String witherKilled = teamDataManager.hasKilledBoss(teamName, "wither") ? "#a0ff72✔" : "#ff7272✖";
            String elderGuardianKilled = teamDataManager.hasKilledBoss(teamName, "elder_guardian") ? "#a0ff72✔" : "#ff7272✖";
            String enderDragonKilled = teamDataManager.hasKilledBoss(teamName, "ender_dragon") ? "#a0ff72✔" : "#ff7272✖";

            List<String> teamMembers = (List<String>) xTeamsAPI.getTeamMembers(teamName);
            String memberFormat = configManager.getMessageString("playerutils.spbinfo.members");
            String separator = configManager.getMessageString("playerutils.spbinfo.members_separator");

            // Aplicar formato a cada miembro
            String teamMembersFormatted = teamMembers.isEmpty() ? configManager.getMessageString("playerutils.spbinfo.no_members") :
                    teamMembers.stream()
                            .map(member -> memberFormat.replace("%member_name%", member))
                            .collect(Collectors.joining(separator));

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

    private boolean handleAdmin(CommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.sendMessage(chatUtils.getMessage("error.unknown_command", (Player) sender));
            return false;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "graceperiod" -> {
                if (!sender.hasPermission("spb.admin.graceperiod") && !sender.hasPermission("spb.admin")) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleGracePeriod(sender, args);
                }
            }
            case "pvp" -> {
                if (!sender.hasPermission("spb.admin.pvp") && !sender.hasPermission("spb.admin")) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handlePvP(sender, args);
                }
            }
            case "event" -> {
                if (!sender.hasPermission("spb.admin.event") && !sender.hasPermission("spb.admin")) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleEvent(sender, args);
                }
            }
            case "broadcast" -> {
                if (!sender.hasPermission("spb.admin.broadcast") && !sender.hasPermission("spb.admin")) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleBroadcast(sender, args);
                }
            }
            case "givecompass" -> {
                if (!sender.hasPermission("spb.admin.compassgive") && !sender.hasPermission("spb.admin")) {
                    sender.sendMessage(chatUtils.getMessage("error.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleCompassGive(sender, args);
                }
            }
        }
        return false;
    }

    private boolean handleBroadcast(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", (Player) sender));
            return false;
        }

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            messageBuilder.append(args[i]).append(" ");
        }
        String message = messageBuilder.toString().trim();

        broadcastManager.sendCommandBroadcastMessage((Player) sender, message);
        return false;
    }

    private boolean handleEvent(CommandSender sender, String[] args) {
        if (args.length == 2) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", (Player) sender));
            return false;
        }
        String action = args[2].toLowerCase();
        switch (action) {
            case "start" -> eventManager.startEvent((Player) sender);
            case "end" -> eventManager.endEvent((Player) sender);
            case "pause" -> eventManager.pauseEvent((Player) sender);
            case "resume" -> eventManager.resumeEvent((Player) sender);
        }
        return false;
    }

    private boolean handlePvP(CommandSender sender, String[] args) {
        if (args.length == 2) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", (Player) sender));
            return false;
        }

        String action = args[2].toLowerCase();
        switch (action) {
            case "enable" -> {
                pvpManager.forceStartPvP();
                sender.sendMessage(chatUtils.getMessage("administration.pvp.enable", (Player) sender));
                if (!(args[3].equals("silent"))) {
                    broadcastManager.sendPVPEnableMessage();
                }
            }
            case "disable" -> {
                pvpManager.stopPvP();
                sender.sendMessage(chatUtils.getMessage("administration.pvp.disable", (Player) sender));
                if (!(args[3].equals("silent"))) {
                    broadcastManager.sendPVPDisableMessage();
                }
            }
        }
        return false;
    }

    private boolean handleGracePeriod(CommandSender sender, String[] args) {
        if (args.length == 2) {
            sender.sendMessage(chatUtils.getMessage("error.invalid_usage", (Player) sender));
            return false;
        }

        String action = args[2].toLowerCase();
        switch (action) {
            case "forcestart" -> {
                if (args.length < 4) {
                    sender.sendMessage(chatUtils.getMessage("error.invalid_usage", (Player) sender));
                    return false;
                } else {
                    if (modifierManager.isGracePeriodActive()) {
                        sender.sendMessage(chatUtils.getMessage("administration.graceperiod.forcestart.already", (Player) sender));
                    } else {
                        long duration = Integer.parseInt(args[3]);
                        GracePeriodManager gracePeriodManager = new GracePeriodManager(plugin, xTeamsAPI, broadcastManager);
                        gracePeriodManager.startGracePeriod();
                        sender.sendMessage(chatUtils.getMessage("administration.graceperiod.forcestart.success", (Player) sender));
                    }
                }
            }
            case "forcestop" -> {
                if (gracePeriodManager != null) {
                    gracePeriodManager.stopGracePeriod();
                    sender.sendMessage(chatUtils.getMessage("administration.graceperiod.forcestop.success", (Player) sender));
                } else {
                    sender.sendMessage(chatUtils.getMessage("administration.graceperiod.forcestop.already", (Player) sender));
                }
            }
        }
        return false;
    }

    private boolean handleCompassGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            Player target = (Player) sender;
            CompassManager.giveTrackingCompass(target);
            sender.sendMessage(chatUtils.getMessage("commands.compass.give.success", target));
            return false;
        } else {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(chatUtils.getMessage("error.player_not_found", target));
                return true;
            } else {
                CompassManager.giveTrackingCompass(target);
                sender.sendMessage(chatUtils.getMessage("commands.compass.give.success_other", target));
                return false;
            }
        }
    }
}

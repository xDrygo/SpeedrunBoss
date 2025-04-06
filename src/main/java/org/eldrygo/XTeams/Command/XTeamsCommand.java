package org.eldrygo.XTeams.Command;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.eldrygo.XTeams.Managers.ConfigManager;
import org.eldrygo.XTeams.Models.Team;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.ChatUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class XTeamsCommand implements CommandExecutor {
    private final SpeedrunBoss plugin;
    private final ChatUtils chatUtils;
    private final ConfigManager teamConfigManager;

    public XTeamsCommand(SpeedrunBoss plugin, ChatUtils chatUtils, ConfigManager teamConfigManager) {
        this.plugin = plugin;
        this.chatUtils = chatUtils;
        this.teamConfigManager = teamConfigManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.unknown_command", null));
            } else {
                sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.unknown_command", (Player) sender));
            }
            return false;
        }

        String subcommand = args[0].toLowerCase();

        // Call the commands
        switch (subcommand) {
            case "create" -> {
                if (!sender.hasPermission("xteams.command.create") && !sender.hasPermission("xteams.admin")) {
                    sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleCreate(sender, args);
                }
            }
            case "delete" -> {
                if (!sender.hasPermission("xteams.command.delete") && !sender.hasPermission("xteams.admin")) {
                    sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleDelete(sender, args);
                }
            }
            case "setdisplay" -> {
                if (!sender.hasPermission("xteams.command.setdisplay") && !sender.hasPermission("xteams.admin")) {
                    sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleSetDisplay(sender, args);
                }
            }
            case "list" -> {
                if (!sender.hasPermission("xteams.command.list") && !sender.hasPermission("xteams.admin")) {
                    sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleList(sender);
                }
            }
            case "join" -> {
                if (!sender.hasPermission("xteams.command.join") && !sender.hasPermission("xteams.admin")) {
                    sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleJoin(sender, args);
                }
            }
            case "teaminfo" -> {
                if (!sender.hasPermission("xteams.command.teaminfo") && !sender.hasPermission("xteams.admin")) {
                    sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleInfoTeam(sender, args);
                }
            }
            case "playerinfo" -> {
                if (!sender.hasPermission("xteams.command.playerinfo") && !sender.hasPermission("xteams.admin")) {
                    sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleInfoPlayer(sender, args);
                }
            }
            case "leave" -> {
                if (!sender.hasPermission("xteams.command.leave") && !sender.hasPermission("xteams.admin")) {
                    sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.no_permission", (Player) sender));
                    return true;
                } else {
                    return handleLeave(sender, args);
                }
            }
            default -> {
                sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.unknown_command", (Player) sender));
                return false;
            }
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (args.length < 2) { // Se requieren al menos 2 argumentos
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.team_not_specified.create", (Player) sender));
            return false;
        }
        if (args.length < 3) { // Se requieren al menos 3 argumentos
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.priority_not_specified", (Player) sender));
            return false;
        }

        String teamName = args[1];
        int priority;

        try {
            priority = Integer.parseInt(args[2]); // Intenta convertir el argumento en un número
        } catch (NumberFormatException e) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.invalid_priority", (Player) sender));
            return false;
        }

        Team team = plugin.getTeamManager().getTeamByName(teamName);
        if (team != null) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.team_already_exists", (Player) sender)
                    .replace("%team%", teamName));
            return false;
        }

        plugin.getTeamManager().createTeam(teamName, teamName, priority, new HashSet<>());
        sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.commands.create.success", (Player) sender)
                .replace("%team%", teamName));
        teamConfigManager.saveTeamsToConfig();

        return true;
    }

    private boolean handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.team_not_specified.delete", (Player) sender));
            return false;
        }

        String teamName = args[1];
        Team team = plugin.getTeamManager().getTeamByName(teamName);
        if (teamName.equals("*")) {
            if (!sender.hasPermission("xteams.command.delete.all") && !sender.hasPermission("xteams.admin")) {
                sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.all_not_permission.delete", (Player) sender));
                return false;
            }
            if (plugin.getTeamManager().getAllTeams().isEmpty()) {
                sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.anyteam_not_found", (Player) sender));
                return false;
            } else {
                if (sender.hasPermission("xteams.command.delete.*")) {
                    plugin.getTeamManager().deleteAllTeams(); // No return value
                    sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.commands.delete.successall", (Player) sender));
                    teamConfigManager.saveTeamsToConfig();
                } else {
                    sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.no_permission", (Player) sender));
                    return true;
                }
            }
        } else {
            if (team == null) {
                sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.team_not_found", (Player) sender)
                        .replace("%team%", teamName));
                return false;
            } else {
                plugin.getTeamManager().deleteTeam(teamName); // No return value
                sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.commands.delete.success", (Player) sender)
                        .replace("%team%", teamName));
                teamConfigManager.saveTeamsToConfig();
            }
        }
        return true;
    }

    private boolean handleSetDisplay(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.setdisplay.displayname_not_specified", (Player) sender));
            return false;
        }

        String teamName = args[1];
        String displayName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        if (!displayName.startsWith("\"") || !displayName.endsWith("\"")) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.setdisplay.invalid_format", (Player) sender));
            return false;
        }

        displayName = displayName.substring(1, displayName.length() - 1);

        Team team = plugin.getTeamManager().getTeamByName(teamName);
        if (team == null) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.team_not_found", (Player) sender)
                    .replace("%team%", teamName));
            return false;
        }

        team.setDisplayName(displayName);
        teamConfigManager.saveTeamsToConfig();

        sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.commands.setdisplay.success", (Player) sender)
                .replace("%team%", teamName).replace("%display_name%", displayName));

        return true;
    }

    private boolean handleJoin(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.team_not_specified.join", (Player) sender));
            return false;
        }

        String teamName = args[1];
        Team team = plugin.getTeamManager().getTeamByName(teamName);

        // Primero comprobamos si el equipo existe
        if (team == null) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.team_not_found", (Player) sender).replace("%team%", teamName));
            return false;
        }

        Player player = (Player) sender;

        if (args.length > 2) {
            Player targetPlayer = Bukkit.getPlayer(args[2]);

            if (targetPlayer == null || !targetPlayer.isOnline()) {
                sender.sendMessage(PlaceholderAPI.setPlaceholders(player, chatUtils.xTeamsGetMessage("xteams.error.commands.player_not_found", targetPlayer).replace("%player%", args[2])));
                return false;
            }

            if (plugin.getTeamManager().isInTeam(targetPlayer, teamName)) {
                sender.sendMessage(PlaceholderAPI.setPlaceholders(targetPlayer, chatUtils.xTeamsGetMessage("xteams.error.commands.join.other.already_in_team", targetPlayer).replace("%team%", teamName)));
                return false;
            }

            plugin.getTeamManager().joinTeam(targetPlayer, teamName);
            sender.sendMessage(PlaceholderAPI.setPlaceholders(targetPlayer, chatUtils.xTeamsGetMessage("xteams.commands.join.other.success", targetPlayer).replace("%team%", teamName)));
            teamConfigManager.saveTeamsToConfig();
        } else {
            if (!(sender instanceof Player)) {
                Bukkit.getConsoleSender().sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.join.self_onlyplayer", (Player) sender));
                return false;
            }
            if (plugin.getTeamManager().isInTeam(player, teamName)) {
                sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.join.self.already_in_team", (Player) sender).replace("%team%", teamName));
                return false;
            }

            plugin.getTeamManager().joinTeam(player, teamName);
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.commands.join.self.success", (Player) sender).replace("%team%", teamName));
            teamConfigManager.saveTeamsToConfig();
        }

        return true;
    }

    private boolean handleInfoTeam(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.team_not_specified.teaminfo", (Player) sender));
            return false;
        }

        String teamName = args[1];
        Map<String, Object> teamInfo = plugin.getTeamManager().getTeamInfo(teamName);
        Team team = plugin.getTeamManager().getTeamByName(teamName);
        if (team == null) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.team_not_found", (Player) sender)
                    .replace("%team%", teamName));
            return false;
        }

        String displayName = (String) teamInfo.get("displayName");
        String name = (String) teamInfo.get("name");
        int priority = (int) teamInfo.get("priority");
        Set<String> membersSet = (Set<String>) teamInfo.get("members");
        List<String> members = new ArrayList<>(membersSet);

        StringBuilder message = new StringBuilder();

        List<String> headerLines = chatUtils.getMessageList("xteams.commands.teaminfo.string.header");
        for (String line : headerLines) {
            line = ChatUtils.formatColor(line.replace("%display_name%", displayName)
                    .replace("%team%", name)
                    .replace("%prefix%", plugin.xTeamsPrefix)
                    .replace("%priority%", String.valueOf(priority)));
            message.append(PlaceholderAPI.setPlaceholders((OfflinePlayer) sender, line))
                    .append("\n");
        }

        if (members.isEmpty()) {
            message.append(chatUtils.xTeamsGetMessage("xteams.commands.teaminfo.string.no_members", (Player) sender)).append("\n");
        } else {
            message.append(chatUtils.xTeamsGetMessage("xteams.commands.teaminfo.string.members_header", (Player) sender)).append("\n");
            for (String member : members) {
                message.append(chatUtils.xTeamsGetMessage("xteams.commands.teaminfo.string.members_row", (Player) sender)
                        .replace("%member%", member)).append("\n");
            }
        }

        List<String> footerLines = chatUtils.getMessageList("xteams.commands.teaminfo.string.footer");
        for (String line : footerLines) {
            message.append(PlaceholderAPI.setPlaceholders((OfflinePlayer) sender, ChatUtils.formatColor(line))).append("\n");
        }

        sender.sendMessage(message.toString());
        return true;
    }

    private boolean handleInfoPlayer(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.player_not_specified", (Player) sender));
            return false;
        }

        Player player = (Player) sender;
        Player targetPlayer = Bukkit.getPlayer(args[1]);  // Corregido el índice aquí

        if (targetPlayer == null || (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline())) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.player_not_found", targetPlayer).replace("%player%", args[1]));
            return false;
        }

        List<Team> playerTeams = plugin.getTeamManager().getPlayerTeams(targetPlayer);

        if (playerTeams.isEmpty()) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.commands.playerinfo.string.no_teams", targetPlayer));
            return true;
        }

        // Ordena los equipos por prioridad
        playerTeams.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        Team mainTeam = playerTeams.get(0);

        StringBuilder message = new StringBuilder();

        // Header del mensaje
        message.append(chatUtils.xTeamsGetMessage("xteams.commands.playerinfo.string.header", targetPlayer))
                .append("\n");

        // Información del equipo principal
        message.append(chatUtils.xTeamsGetMessage("xteams.commands.playerinfo.string.main_team", targetPlayer)
                        .replace("%display_name%", mainTeam.getDisplayName())
                        .replace("%team%", mainTeam.getName())
                        .replace("%priority%", String.valueOf(mainTeam.getPriority())))
                .append("\n");

        // Información de otros equipos
        message.append(chatUtils.xTeamsGetMessage("xteams.commands.playerinfo.string.team_list_header", targetPlayer))
                .append("\n");
        for (Team team : playerTeams) {
            message.append(chatUtils.xTeamsGetMessage("xteams.commands.playerinfo.string.team_list_row", targetPlayer)
                            .replace("%display_name%", team.getDisplayName())
                            .replace("%team%", team.getName())
                            .replace("%priority%", String.valueOf(team.getPriority())))
                    .append("\n");
        }

        // Footer (si es necesario añadirlo)
        message.append(chatUtils.xTeamsGetMessage("xteams.commands.playerinfo.string.footer", targetPlayer))
                .append("\n");

        sender.sendMessage(message.toString());
        return true;
    }


    private boolean handleList(CommandSender sender) {
        Set<Team> teams = plugin.getTeamManager().getAllTeams();
        if (teams == null || teams.isEmpty()) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.commands.list.empty", (Player) sender));
        } else {
            StringBuilder message = new StringBuilder();
            String header = chatUtils.xTeamsGetMessage("xteams.commands.list.string.header", (Player) sender);
            message.append(header).append("\n");

            for (Team team : teams) {
                String teamMessage = chatUtils.xTeamsGetMessage("xteams.commands.list.string.row", (Player) sender)
                        .replace("%team%", team.getName())
                        .replace("%priority%", String.valueOf(team.getPriority()))
                        .replace("%display_name%", team.getDisplayName());
                message.append(teamMessage).append("\n");
            }

            sender.sendMessage(message.toString());
            return true;
        }
        return false;
    }

    private boolean handleLeave(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.team_not_specified.leave", (Player) sender));
            return false;
        }

        String teamName = args[1];
        Team team = plugin.getTeamManager().getTeamByName(teamName);

        if (team == null && !(teamName.equals("*"))) {
            sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.team_not_found", (Player) sender).replace("%team%", teamName));
            return false;
        }


        Player player = (Player) sender;

        if (teamName.equals("*")) {
            if (!sender.hasPermission("xteams.command.leave.all") && !sender.hasPermission("xteams.admin")) {
                sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.all_not_permission.leave", (Player) sender));
                return false;
            }
            if (args.length > 2) {
                Player targetPlayer = Bukkit.getPlayer(args[2]);

                if (targetPlayer == null || !targetPlayer.isOnline()) {
                    sender.sendMessage(PlaceholderAPI.setPlaceholders(player, chatUtils.xTeamsGetMessage("xteams.error.commands.player_not_found", targetPlayer).replace("%player%", args[2])));
                    return false;
                }

                if (!plugin.getTeamManager().isInTeam(targetPlayer, teamName)) {
                    sender.sendMessage(PlaceholderAPI.setPlaceholders(player, chatUtils.xTeamsGetMessage("xteams.error.commands.leave.other.not_in_anyteam", targetPlayer).replace("%player%", args[2]).replace("%team%", teamName)));
                    return false;
                }

                plugin.getTeamManager().leaveTeam(targetPlayer, teamName);
                sender.sendMessage(PlaceholderAPI.setPlaceholders(targetPlayer, chatUtils.xTeamsGetMessage("xteams.commands.leave.other.successall", targetPlayer).replace("%team%", teamName)));
                teamConfigManager.saveTeamsToConfig();
            } else {
                if (!(sender instanceof Player)) {
                    Bukkit.getConsoleSender().sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.onlyplayer.leave", (Player) sender));
                    return false;
                }
                if (!plugin.getTeamManager().isInTeam(player, teamName)) {
                    sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.leave.self.not_in_anyteam", player).replace("%team%", teamName));
                    return false;
                }

                plugin.getTeamManager().leaveTeam(player, teamName);
                sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.commands.leave.self.successall", player).replace("%team%", teamName));
                teamConfigManager.saveTeamsToConfig();
            }
        } else {
            if (args.length > 2) {
                Player targetPlayer = Bukkit.getPlayer(args[2]);

                if (targetPlayer == null || !targetPlayer.isOnline()) {
                    sender.sendMessage(PlaceholderAPI.setPlaceholders(player, chatUtils.xTeamsGetMessage("xteams.error.commands.player_not_found", targetPlayer).replace("%player%", args[2])));
                    return false;
                }

                if (!plugin.getTeamManager().isInTeam(targetPlayer, teamName)) {
                    sender.sendMessage(PlaceholderAPI.setPlaceholders(player, chatUtils.xTeamsGetMessage("xteams.error.commands.leave.other.not_in_team", targetPlayer).replace("%player%", args[2]).replace("%team%", teamName)));
                    return false;
                }

                plugin.getTeamManager().leaveTeam(targetPlayer, teamName);
                sender.sendMessage(PlaceholderAPI.setPlaceholders(targetPlayer, chatUtils.xTeamsGetMessage("xteams.commands.leave.other.success", targetPlayer).replace("%team%", teamName)));
                teamConfigManager.saveTeamsToConfig();
            } else {
                if (!(sender instanceof Player)) {
                    Bukkit.getConsoleSender().sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.onlyplayer.leave", (Player) sender));
                    return false;
                }
                if (!plugin.getTeamManager().isInTeam(player, teamName)) {
                    sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.error.commands.leave.self.not_in_team", player).replace("%team%", teamName));
                    return false;
                }

                plugin.getTeamManager().leaveTeam(player, teamName);
                sender.sendMessage(chatUtils.xTeamsGetMessage("xteams.commands.leave.self.success", player).replace("%team%", teamName));
                teamConfigManager.saveTeamsToConfig();
            }}

        return true;
    }
}
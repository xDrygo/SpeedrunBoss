package org.eldrygo.Handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.eldrygo.Cinematics.Managers.CinematicManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.eldrygo.XTeams.Models.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CommandTabCompleter implements TabCompleter {
    private final CinematicManager cinematicManager;
    private final SpeedrunBoss plugin;

    public CommandTabCompleter(CinematicManager cinematicManager, XTeamsAPI xTeamsAPI, SpeedrunBoss plugin) {
        this.cinematicManager = cinematicManager;
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission("spb.admin")) {
            completions.add("admin");
            return filter(completions, args[0]);
        }

        if (args[0].equalsIgnoreCase("admin")) {
            if (args.length == 2) {
                completions.addAll(Arrays.asList(
                        "pvp", "event", "graceperiod", "broadcast", "killerhandle", "reload", "help", "task", "compass", "cinematic", "bossregister"
                ));
                return filter(completions, args[1]);
            }

            if (args.length == 3) {
                switch (args[1].toLowerCase()) {
                    case "pvp" -> completions.addAll(Arrays.asList("enable", "disable"));
                    case "event" -> completions.addAll(Arrays.asList("start", "pause", "resume", "end"));
                    case "graceperiod" -> completions.addAll(Arrays.asList("forcestart", "forcestop"));
                    case "broadcast" -> completions.add("<mensaje>");
                    case "killerhandle", "compass" -> completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                    case "stun" -> completions.addAll(Arrays.asList("enable", "disable", "player"));
                    case "task" -> completions.add("confirm");
                    case "cinematic" -> completions.addAll(Arrays.asList("start", "stop"));
                    case "bossregister" -> {
                        completions.add("<team>");
                        return getMatches(args[2], getTeamsList());
                    }
                    case "spawn" -> {
                        completions.addAll(List.of("setglobal", "setteam", "tpglobal", "tpteam"));
                        return filter(completions, args[2]);
                    }
                }
                return filter(completions, args[2]);
            }

            if (args.length == 4) {
                if (args[1].equalsIgnoreCase("pvp")) {
                    completions.add("silent");
                    return filter(completions, args[3]);
                } else if (args[1].equalsIgnoreCase("graceperiod") && args[2].equalsIgnoreCase("forcestart")) {
                    completions.add("<segundos>");
                    return filter(completions, args[3]);
                } else if (args[1].equalsIgnoreCase("task") && args[2].equalsIgnoreCase("confirm")) {
                    completions.addAll(Arrays.asList("graceperiod", "pvp", "bbcountdown"));
                    return filter(completions, args[3]);
                } else if (args[1].equalsIgnoreCase("cinematic")) {
                    if (args[2].equalsIgnoreCase("stop")) {
                        completions.add("confirm");
                        return filter(completions, args[3]);
                    } else if (args[2].equalsIgnoreCase("start")) {
                        completions.addAll(cinematicManager.getCinematicList());
                        return filter(completions, args[3]);
                    }
                } else if (args[1].equalsIgnoreCase("bossregister")) {
                    return getMatches(args[3], List.of("wither", "warden", "elder_guardian", "ender_dragon"));
                } else if (args[1].equalsIgnoreCase("dimregister")) {
                    return getMatches(args[3], List.of("end", "nether"));
                } else if (args[1].equalsIgnoreCase("spawn")) {
                    if (args[2].equalsIgnoreCase("setteam") || args[2].equalsIgnoreCase("tpteam")) {
                        completions.addAll(getTeamsList());
                        return filter(completions, args[3]);
                    }
                }
            }

            if (args.length == 5) {
                if (args[1].equalsIgnoreCase("stun") && args[2].equalsIgnoreCase("player")) {
                    completions.addAll(Arrays.asList("true", "false"));
                    return filter(completions, args[4]);
                } else if (args[1].equalsIgnoreCase("bossregister")) {
                    return getMatches(args[4], List.of("true", "false", "confirm"));
                } else if (args[1].equalsIgnoreCase("dimregister")) {
                    return getMatches(args[4], List.of("true", "false", "confirm"));
                }
            }

            if (args.length == 6) {
                if (args[1].equalsIgnoreCase("stun") && args[2].equalsIgnoreCase("player") && args[4].equalsIgnoreCase("true")) {
                    completions.add("<tiempo>");
                    return filter(completions, args[5]);
                }
            }
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String input) {
        return list.stream()
                .filter(option -> option.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<String> getMatches(String arg, List<String> options) {
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(arg.toLowerCase())) {
                matches.add(option);
            }
        }
        return matches;
    }

    private List<String> getTeamsList() {
        List<String> teams = new ArrayList<>();

        // Acceder al gestor de equipos del plugin y obtener todos los equipos
        Set<Team> allTeams = plugin.getTeamManager().getAllTeams();

        // Verificar si hay equipos disponibles
        if (allTeams != null && !allTeams.isEmpty()) {
            // Iterar sobre todos los equipos y agregar sus nombres a la lista
            for (Team team : allTeams) {
                teams.add(team.getName());  // Añadir el nombre del equipo a la lista
            }
        }

        return teams;
    }
}
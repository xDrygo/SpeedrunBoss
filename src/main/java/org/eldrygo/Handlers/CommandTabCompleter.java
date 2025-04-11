package org.eldrygo.Handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommandTabCompleter implements TabCompleter {
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
                        "pvp", "event", "graceperiod", "broadcast", "killerhandle", "reload", "help", "task"
                ));
                return filter(completions, args[1]);
            }

            if (args.length == 3) {
                switch (args[1].toLowerCase()) {
                    case "pvp" -> completions.addAll(Arrays.asList("enable", "disable"));
                    case "event" -> completions.addAll(Arrays.asList("start", "pause", "resume", "end"));
                    case "graceperiod" -> completions.addAll(Arrays.asList("forcestart", "forcestop"));
                    case "broadcast" -> completions.add("<mensaje>");
                    case "killerhandle" -> completions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                    case "stun" -> completions.addAll(Arrays.asList("enable", "disable", "player"));
                    case "task" -> completions.add("confirm"); // confirmación de task
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
                    // Lista de tasks válidas para cancelar
                    completions.addAll(Arrays.asList("graceperiod", "pvp", "bbcountdown"));
                    return filter(completions, args[3]);
                }
            }

            if (args.length == 5) {
                if (args[1].equalsIgnoreCase("stun") && args[2].equalsIgnoreCase("player")) {
                    completions.addAll(Arrays.asList("true", "false"));
                    return filter(completions, args[4]);
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
}

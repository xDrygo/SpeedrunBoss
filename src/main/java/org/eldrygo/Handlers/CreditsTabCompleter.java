package org.eldrygo.Handlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CreditsTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("drygo");
            completions.add("jose90");
            completions.add("zephy");
        } else if (args.length == 2) {
            for (Player player : sender.getServer().getOnlinePlayers()) {
                completions.add(player.getName());
            }
        }
        return completions;
    }
}

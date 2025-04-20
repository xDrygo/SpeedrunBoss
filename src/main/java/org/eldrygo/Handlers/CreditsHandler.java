package org.eldrygo.Handlers;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.Utils.ChatUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CreditsHandler implements CommandExecutor {
    private final ConfigManager configManager;

    public CreditsHandler(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("spb.credits") && !sender.hasPermission("spb.admin") && !sender.isOp()) return false;
        if (args.length == 0) return false;
        Player target;
        if (args.length > 1) {
            target = Bukkit.getPlayer(args[1]);
        } else {
            if (sender instanceof Player) {
                target = ((Player) sender).getPlayer();
            } else {
                target = null;
            }
        }

        String credit = args[0];

        if (target == null) return false;

        switch (credit) {
            case "drygo" -> creditDrygo(target);
            case "jose90" -> creditJose90(target);
            case "zephy" -> creditZephy(target);
            default -> { return false; }
        }
        return true;
    }

    private void creditDrygo(Player target) {
        List<String> creditDrygoMsg = configManager.getMessageConfig().getStringList("credits.drygo");

        if (creditDrygoMsg.isEmpty()) { return; }

        for (String line : creditDrygoMsg) {
            target.sendMessage(ChatUtils.formatColor(line));
        }
    }
    private void creditJose90(Player target) {
        List<String> creditJose90Msg = configManager.getMessageConfig().getStringList("credits.jose90");

        if (creditJose90Msg.isEmpty()) { return; }

        for (String line : creditJose90Msg) {
            target.sendMessage(ChatUtils.formatColor(line));
        }
    }
    private void creditZephy(Player target) {
        List<String> creditZephyMsg = configManager.getMessageConfig().getStringList("credits.zephy");

        if (creditZephyMsg.isEmpty()) { return; }

        for (String line : creditZephyMsg) {
            target.sendMessage(ChatUtils.formatColor(line));
        }
    }
}

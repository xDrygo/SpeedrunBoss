package org.eldrygo.Handlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.eldrygo.Managers.Files.PlayerDataManager;
import org.eldrygo.Utils.ChatUtils;
import org.jetbrains.annotations.NotNull;

public class SkillHandler implements CommandExecutor {

    private final PlayerDataManager playerDataManager;
    private final ChatUtils chatUtils;

    public SkillHandler(PlayerDataManager playerDataManager, ChatUtils chatUtils) {
        this.playerDataManager = playerDataManager;
        this.chatUtils = chatUtils;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("spb.skill") && !sender.hasPermission("spb.admin") && !sender.isOp()) return false;

        if (args.length < 2) {
            sender.sendMessage(chatUtils.getMessage("skill.usage", null));
            return true;
        }

        String action = args[0];
        String playerName = args[1];

        if (action.equalsIgnoreCase("check")) {
            String status = playerDataManager.getStatus(playerName);
            sender.sendMessage(chatUtils.getMessage("skill.check", null)
                    .replace("%target%", playerName)
                    .replace("%skill%", status));
            return true;
        }

        if (action.equalsIgnoreCase("set")) {
            if (args.length < 3) {
                sender.sendMessage(chatUtils.getMessage("skill.usage", null));
                return true;
            }

            String status = args[2].toLowerCase();
            if (!status.equals("noob") && !status.equals("pro")) {
                sender.sendMessage(chatUtils.getMessage("skill.invalid_skill", null));
                return true;
            }

            playerDataManager.setStatus(playerName, status);
            playerDataManager.save();

            sender.sendMessage(chatUtils.getMessage("skill.set", null)
                    .replace("%target%", playerName)
                    .replace("%skill%", status));
            return true;
        }

        sender.sendMessage(chatUtils.getMessage("skill.usage", null));
        return true;
    }
}

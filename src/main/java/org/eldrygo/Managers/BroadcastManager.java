package org.eldrygo.Managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import org.eldrygo.API.XTeamsAPI;
import org.eldrygo.Utils.ChatUtils;

public class BroadcastManager {
    private final ChatUtils chatUtils;
    private final XTeamsAPI xTeamsAPI;

    public BroadcastManager(ChatUtils chatUtils, XTeamsAPI xTeamsAPI) {
        this.chatUtils = chatUtils;
        this.xTeamsAPI = xTeamsAPI;
    }

    public void sendBossKilledMessage(String bossName, Player player) {
        for (Player players : Bukkit.getServer().getOnlinePlayers()) {
            players.sendMessage(chatUtils.getMessage("broadcast.boss_kill." + bossName, player)
                    .replace("%team%", (xTeamsAPI.getPlayerTeamDisplayName(player)))
                    //.replace("%time%", <get time variable>)
            );
            Location location = players.getLocation();
            players.playSound(location, Sound.BLOCK_NOTE_BLOCK_BIT, 10f, 1.5f);
        }
    }
    public void sendPVPEnableMessage() {
        for (Player players : Bukkit.getServer().getOnlinePlayers()) {
            players.sendMessage(chatUtils.getMessage("broadcast.pvp.enable", players));
            Location location = players.getLocation();
            players.playSound(location, Sound.ENTITY_ALLAY_DEATH, 10f, 0.1f);
        }
    }
    public void sendPVPDisableMessage() {
        for (Player players : Bukkit.getServer().getOnlinePlayers()) {
            players.sendMessage(chatUtils.getMessage("broadcast.pvp.disable", players));
            Location location = players.getLocation();
            players.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 10f, 1.25f);
        }
    }
    public void sendGracePeriodEndMessage() {
        for (Player players : Bukkit.getServer().getOnlinePlayers()) {
            players.sendMessage(chatUtils.getMessage("broadcast.graceperiod_ended", players));
            Location location = players.getLocation();
            players.playSound(location, Sound.ENTITY_ALLAY_HURT, 10f, 0.1f);
        }
    }
    public void sendCommandBroadcastMessage(Player sender, String message) {
        for (Player players : Bukkit.getServer().getOnlinePlayers()) {
            String formattedMessage = chatUtils.getMessage("administration.broadcast_command.prefix", sender) + message;
            players.sendMessage(formattedMessage);
            Location location = players.getLocation();
            players.playSound(location, Sound.ENTITY_VILLAGER_YES, 10f, 2f);
            players.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 10f, 1.25f);
        }
    }
}

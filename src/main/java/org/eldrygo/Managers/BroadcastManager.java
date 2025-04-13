package org.eldrygo.Managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.Utils.PlayerUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;

public class BroadcastManager {
    private final ChatUtils chatUtils;
    private final XTeamsAPI xTeamsAPI;
    public PlayerUtils playerUtils;

    public BroadcastManager(ChatUtils chatUtils, XTeamsAPI xTeamsAPI, PlayerUtils playerUtils) {
        this.chatUtils = chatUtils;
        this.xTeamsAPI = xTeamsAPI;
        this.playerUtils = playerUtils;
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
    public void sendDimensionEnterMessage(String teamName, Player player, String dimension) {
        for (Player players : Bukkit.getServer().getOnlinePlayers()) {
            Location location = players.getLocation();
            switch (dimension) {
                case "nether" -> {
                    players.sendMessage(chatUtils.getMessage("broadcast.dimension_enter.nether", players)
                            .replace("%team%", teamName)
                            .replace("%target%", String.valueOf(player)));
                    players.playSound(location, Sound.BLOCK_ANCIENT_DEBRIS_PLACE, 10f, 2f);
                    players.playSound(location, Sound.BLOCK_NOTE_BLOCK_BASS, 10f, 2f);
                }
                case "end" -> {
                    players.sendMessage(chatUtils.getMessage("broadcast.dimension_enter.end", players)
                            .replace("%team%", teamName)
                            .replace("%target%", String.valueOf(player)));
                    players.playSound(location, Sound.BLOCK_PORTAL_TRAVEL, 10f, 2f);
                    players.playSound(location, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 10f, 2f);
                }
            }
        }
    }

    public void sendCommandBroadcastMessage(Player sender, String message) {
        String[] lines = ChatUtils.formatColor(message).split("\n"); // soporta líneas con '\n'

        for (Player players : Bukkit.getServer().getOnlinePlayers()) {
            String prefix = chatUtils.getMessage("administration.broadcast_command.prefix", sender);
            players.sendMessage(" ");
            players.sendMessage(prefix);
            for (String line : lines) {
                players.sendMessage(" " + line);
            }
            players.sendMessage(" ");

            Location location = players.getLocation();
            players.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10f, 1.5f);
            players.playSound(location, Sound.BLOCK_NOTE_BLOCK_BIT, 10f, 2f);
        }
    }
    public void sendPlayerConnectionMessage(Player player, String type) {
        switch (type) {
            case "join" -> {
                Location playerLocation = player.getLocation();
                player.playSound(playerLocation, Sound.ENTITY_PLAYER_LEVELUP, 10f, 2f);
                for (Player players : Bukkit.getServer().getOnlinePlayers()) {
                    players.sendMessage(chatUtils.getMessage("broadcast.connection.global.join", players)
                            .replace("%target%", player.getName())
                            .replace("%target_prefix%", playerUtils.getPrefix(player)));
                }
            }
            case "leave" -> {
                for (Player players : Bukkit.getServer().getOnlinePlayers()) {
                    players.sendMessage(chatUtils.getMessage("broadcast.connection.global.leave", players)
                            .replace("%target%", player.getName())
                            .replace("%target_prefix%", playerUtils.getPrefix(player)));
                }

            }
        }
    }
    public void sendAdminConnectionMessage(Player player, String type) {
        switch (type) {
            case "join" -> {
                Location playerLocation = player.getLocation();
                player.playSound(playerLocation, Sound.ENTITY_PLAYER_LEVELUP, 10f, 2f);
                for (Player players : playerUtils.getAdminPlayers()) {
                    Location targetLocation = players.getLocation();
                    player.playSound(targetLocation, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 10f, 1.25f);
                    players.sendMessage(chatUtils.getMessage("broadcast.connection.admin.join", players)
                            .replace("%target%", player.getName())
                            .replace("%target_prefix%", playerUtils.getPrefix(player)));
                }
            }
            case "leave" -> {
                for (Player players : playerUtils.getAdminPlayers()) {
                    Location targetLocation = players.getLocation();
                    player.playSound(targetLocation, Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 10f, 1.25f);
                    players.sendMessage(chatUtils.getMessage("broadcast.connection.admin.leave", players)
                            .replace("%target%", player.getName())
                            .replace("%target_prefix%", playerUtils.getPrefix(player)));
                }
            }
        }
    }
}

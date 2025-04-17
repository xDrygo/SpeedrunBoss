package org.eldrygo.Managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import org.eldrygo.Time.Managers.TimeManager;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.Utils.PlayerUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;

public class BroadcastManager {
    private final ChatUtils chatUtils;
    private final XTeamsAPI xTeamsAPI;
    public PlayerUtils playerUtils;
    private final TimeManager timeManager;

    public BroadcastManager(ChatUtils chatUtils, XTeamsAPI xTeamsAPI, PlayerUtils playerUtils, TimeManager timeManager) {
        this.chatUtils = chatUtils;
        this.xTeamsAPI = xTeamsAPI;
        this.playerUtils = playerUtils;
        this.timeManager = timeManager;
    }

    public void sendBossKilledMessage(String bossName, Player player) {
        for (Player players : Bukkit.getServer().getOnlinePlayers()) {
            String team = xTeamsAPI.getPlayerTeamDisplayName(player);
            String teamName = xTeamsAPI.getPlayerTeamName(player);
            String time = timeManager.getTimeForBoss(teamName, bossName);
            players.sendMessage(chatUtils.getMessage("broadcast.boss_kill." + bossName, player)
                    .replace("%team%", team)
                    .replace("%time%", time)
            );
            Location location = players.getLocation();
            players.playSound(location, Sound.BLOCK_NOTE_BLOCK_BIT, 10f, 1.5f);
        }
    }
    public void sendAlreadyKilledBoss(String bossName, Player player) {
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
}

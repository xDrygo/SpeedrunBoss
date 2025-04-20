package org.eldrygo.Handlers;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LeaderboardHandler implements CommandExecutor {
    private final ConfigManager configManager;
    private final TeamDataManager teamDataManager;
    private final XTeamsAPI xTeamsAPI;

    public LeaderboardHandler(ConfigManager configManager, TeamDataManager teamDataManager, XTeamsAPI xTeamsAPI) {
        this.configManager = configManager;
        this.teamDataManager = teamDataManager;
        this.xTeamsAPI = xTeamsAPI;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!sender.hasPermission("spb.leaderboard") && !sender.hasPermission("spb.admin") && !sender.isOp()) return false;

        List<String> leaderboardString = configManager.getMessageConfig().getStringList("leaderboard.string");

        if (leaderboardString.isEmpty()) { return false; }

        String yourTeam = "#FF3333No tienes equipo.";
        if (sender instanceof Player) { yourTeam = xTeamsAPI.getPlayerTeamName((Player) sender); }

        for (String line : leaderboardString) {
            sender.sendMessage(ChatUtils.formatColor(line)
                    .replace("%your_team%", yourTeam)
                    .replace("%team_1%", getKilledBosses("team_1"))
                    .replace("%team_2%", getKilledBosses("team_2"))
                    .replace("%team_3%", getKilledBosses("team_3"))
                    .replace("%team_4%", getKilledBosses("team_4"))
                    .replace("%team_5%", getKilledBosses("team_5"))
                    .replace("%team_6%", getKilledBosses("team_6"))
                    .replace("%team_7%", getKilledBosses("team_7"))
                    .replace("%team_8%", getKilledBosses("team_8"))
                    .replace("%team_9%", getKilledBosses("team_9"))
                    .replace("%team_10%", getKilledBosses("team_10"))
                    .replace("%team_11%", getKilledBosses("team_11"))
                    .replace("%team_12%", getKilledBosses("team_12"))
                    .replace("%team_13%", getKilledBosses("team_13"))
                    .replace("%team_14%", getKilledBosses("team_14"))
                    .replace("%team_15%", getKilledBosses("team_15"))
                    .replace("%team_16%", getKilledBosses("team_16"))
            );
        }
        return false;
    }

    private String getKilledBosses(String teamName){
        String warden = "ढ ";
        String elder_guardian = "द ";
        String wither = "त ";
        String ender_dragon = "ऩ ";

        if (teamDataManager.getKilledBosses(teamName).contains("warden")) {
            warden = "ड ";
        }
        if (teamDataManager.getKilledBosses(teamName).contains("elder_guardian")) {
            elder_guardian = "थ ";
        }
        if (teamDataManager.getKilledBosses(teamName).contains("wither")) {
            wither = "ण ";
        }
        if (teamDataManager.getKilledBosses(teamName).contains("ender_dragon")) {
            ender_dragon = "ध ";
        } else if (teamDataManager.hasKilledRequiredBosses(teamName)) {
            ender_dragon = "न ";
        }

        return warden + elder_guardian + wither + ender_dragon;
    }
}

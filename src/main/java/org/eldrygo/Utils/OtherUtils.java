package org.eldrygo.Utils;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.eldrygo.Managers.Files.ConfigManager;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.XTeams.API.XTeamsAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OtherUtils {
    private final SpeedrunBoss plugin;
    private final XTeamsAPI xTeamsAPI;
    private final ConfigManager configManager;

    public OtherUtils(SpeedrunBoss plugin, XTeamsAPI xTeamsAPI, ConfigManager configManager) {
        this.plugin = plugin;
        this.xTeamsAPI = xTeamsAPI;
        this.configManager = configManager;
    }

    public static void applyKeepInventoryToAllWorlds() {
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.KEEP_INVENTORY, true);
        }
    }

    public String getWinnerTeamDisplay() {
        String teamName = plugin.winnerTeam;

        return xTeamsAPI.getTeamDisplayName(teamName);
    }

    public String getWinnerTeamMembers() {
        String teamName = plugin.winnerTeam;
        List<String> teamMembers = new ArrayList<>(xTeamsAPI.getTeamMembers(teamName));

        String noMembersMsg = " ";
        String memberFormat = configManager.getMessageString("cinematics.winner.titles.winner.teammates.members");
        String separator = configManager.getMessageString("cinematics.winner.titles.winner.members_separator");

        String teamMembersFormatted = teamMembers.isEmpty() ? noMembersMsg :
                teamMembers.stream()
                        .map(member -> memberFormat.replace("%member_name%", member))
                        .collect(Collectors.joining(separator));

        return ChatUtils.formatColor(teamMembersFormatted);
    }


}

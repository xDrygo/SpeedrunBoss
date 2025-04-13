package org.eldrygo.BossRace.Managers;

import org.bukkit.entity.Player;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.eldrygo.XTeams.Models.Team;

public class DimensionBroadcastManager {
    private final XTeamsAPI xTeamsAPI;
    private final TeamDataManager teamDataManager;
    private final BroadcastManager broadcastManager;

    public DimensionBroadcastManager(XTeamsAPI xTeamsAPI, TeamDataManager teamDataManager, BroadcastManager broadcastManager) {
        this.xTeamsAPI = xTeamsAPI;
        this.teamDataManager = teamDataManager;
        this.broadcastManager = broadcastManager;
    }

    public void onDimensionEnter(String dimension, Player player) {
        String teamName = xTeamsAPI.getPlayerTeamName(player);
        Team team = xTeamsAPI.getTeamByName(teamName);

        if (teamName != "No Team") {
            if (!teamDataManager.hasRegisteredDimension(teamName, dimension)) {
                if (team != null) {
                    if (dimension != null) {
                        teamDataManager.addDimensionRegister(teamName, dimension);
                        broadcastManager.sendDimensionEnterMessage(teamName, player, dimension);
                    }
                }
            }
        }

    }
}
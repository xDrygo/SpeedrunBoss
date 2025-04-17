package org.eldrygo.BossRace.Managers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.eldrygo.Event.Managers.EventManager;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Time.Managers.TimeManager;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.eldrygo.XTeams.Models.Team;

import static org.eldrygo.Event.Managers.EventManager.EventState.RUNNING;

public class BossKillManager {
    private final TeamDataManager teamDataManager;
    private final BroadcastManager broadcastManager;
    private final XTeamsAPI xTeamsAPI;
    private final EventManager eventManager;
    private final TimeManager timerManager;

    // Constructor que recibe las dependencias necesarias
    public BossKillManager(XTeamsAPI xTeamsAPI, TeamDataManager teamDataManager, BroadcastManager broadcastManager, EventManager eventManager, TimeManager timerManager) {
        this.xTeamsAPI = xTeamsAPI;
        this.teamDataManager = teamDataManager;
        this.broadcastManager = broadcastManager;
        this.eventManager = eventManager;
        this.timerManager = timerManager;
    }

    // Este método se llama cuando un boss es asesinado
    public void onBossKilled(Entity entity, Player player) {
        // Obtener el nombre del equipo del jugador
        String teamName = xTeamsAPI.getPlayerTeamName(player);
        Team team = xTeamsAPI.getTeamByName(teamName);

        if (eventManager.getState() == RUNNING && timerManager.isRunning()) {
            if (team != null) {
                // Convertir el tipo de entidad a un nombre de boss
                String bossName = getBossName(entity.getType());

                if (bossName != null) {
                    if (!teamDataManager.getKilledBosses(teamName).contains(bossName)) {
                        teamDataManager.addKilledBoss(teamName, bossName);
                        timerManager.recordBossKill(teamName, bossName);
                        broadcastManager.sendBossKilledMessage(bossName, player);
                    } else {
                        broadcastManager.sendAlreadyKilledBoss(bossName, player);
                    }
                }
            }
        }
    }

    // Método auxiliar para obtener el nombre del boss según el tipo de entidad
    private String getBossName(EntityType type) {
        return switch (type) {
            case ENDER_DRAGON -> "ender_dragon";
            case WITHER -> "wither";
            case WARDEN -> "warden";
            case ELDER_GUARDIAN -> "elder_guardian";
            default -> null;
        };
    }

    /*
    TO DO LIST:
    -
     */
}
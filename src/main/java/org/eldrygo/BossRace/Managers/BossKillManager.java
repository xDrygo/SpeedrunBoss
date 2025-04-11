package org.eldrygo.BossRace.Managers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.XTeams.API.XTeamsAPI;
import org.eldrygo.XTeams.Models.Team;

public class BossKillManager {
    private final TeamDataManager teamDataManager;
    private final BroadcastManager broadcastManager;
    private final XTeamsAPI xTeamsAPI;

    // Constructor que recibe las dependencias necesarias
    public BossKillManager(XTeamsAPI xTeamsAPI, TeamDataManager teamDataManager, BroadcastManager broadcastManager) {
        this.xTeamsAPI = xTeamsAPI;
        this.teamDataManager = teamDataManager;
        this.broadcastManager = broadcastManager;
    }

    // Este método se llama cuando un boss es asesinado
    public void onBossKilled(Entity entity, Player player) {
        // Obtener el nombre del equipo del jugador
        String teamName = xTeamsAPI.getPlayerTeamName(player);
        Team team = xTeamsAPI.getTeamByName(teamName);

        // Verificar que el equipo no sea nulo
        if (team != null) {
            // Convertir el tipo de entidad a un nombre de boss
            String bossName = getBossName(entity.getType());

            if (bossName != null) {
                // Registrar el boss asesinado en el archivo de datos
                teamDataManager.addKilledBoss(teamName, bossName);
                broadcastManager.sendBossKilledMessage(bossName, player);
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
package org.eldrygo.BossRace.Managers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.eldrygo.API.XTeamsAPI;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Models.Team;

public class BossKillManager {
    private final XTeamsAPI xTeamsAPI;
    private final TeamDataManager teamDataManager;
    private final BroadcastManager broadcastManager;

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
                broadcastManager.sendBossKilledMessage(teamName, bossName, player);
            }
        }
    }

    // Método auxiliar para obtener el nombre del boss según el tipo de entidad
    private String getBossName(EntityType type) {
        switch (type) {
            case ENDER_DRAGON:
                return "ender_dragon";
            case WITHER:
                return "wither";
            case WARDEN:
                return "warden";
            case ELDER_GUARDIAN:
                return "elder_guardian";
            default:
                return null; // No es un boss relevante
        }
    }
}
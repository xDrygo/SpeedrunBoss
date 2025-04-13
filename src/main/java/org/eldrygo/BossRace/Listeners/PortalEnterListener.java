package org.eldrygo.BossRace.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.entity.Player;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Utils.ChatUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;

public class PortalEnterListener implements Listener {
    private final TeamDataManager teamDataManager;
    private final XTeamsAPI xTeamsAPI;
    private final ChatUtils chatUtils;

    public PortalEnterListener(TeamDataManager teamDataManager, XTeamsAPI xTeamsAPI, ChatUtils chatUtils) {
        this.teamDataManager = teamDataManager;
        this.xTeamsAPI = xTeamsAPI;
        this.chatUtils = chatUtils;
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();

        // Verificar si el portal es un portal al End
        if (event.getCause() == PlayerPortalEvent.TeleportCause.END_PORTAL) {

            // Obtener el nombre del equipo del jugador
            String teamName = xTeamsAPI.getPlayerTeamName(player);

            // Asegurarse de que el jugador tiene un equipo asignado
            if (teamName == null || teamName.isEmpty()) {
                player.sendMessage(chatUtils.getMessage("warning.no_team_assigned", player));
                event.setCancelled(true);
                return;
            }

            // Comprobar si el equipo ha matado todos los bosses necesarios
            if (!teamDataManager.hasKilledRequiredBosses(teamName)) {
                player.sendMessage(chatUtils.getMessage("warning.try_enter_end_without_required_bosses", player));
                event.setCancelled(true);  // Cancelar la teletransportación
            }
        }
    }
}

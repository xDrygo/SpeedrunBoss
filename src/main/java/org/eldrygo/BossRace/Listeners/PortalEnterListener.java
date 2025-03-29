package org.eldrygo.BossRace.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.entity.Player;
import org.eldrygo.API.XTeamsAPI;
import org.eldrygo.Managers.Files.TeamDataManager;
import org.eldrygo.Utils.ChatUtils;

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
    public void onPortalEnter(EntityPortalEnterEvent event) {
        if (event.getEntity() instanceof Player player) {

            // Verificar si el jugador intenta entrar al portal del End
            if (event.getLocation().getWorld().getName().equals("world_end")) {

                // Obtener el nombre del equipo del jugador (suponiendo que usas XTeams o algo similar)
                String teamName = xTeamsAPI.getPlayerTeamName(player); // Este debería ser obtenido dinámicamente del jugador

                // Comprobar si el equipo ha matado todos los bosses necesarios
                if (!teamDataManager.hasKilledRequiredBosses(teamName)) {
                    // Si no han matado todos los bosses, cancelar el evento para que no entren al End
                    player.sendMessage(chatUtils.getMessage("warning.try_enter_end_without_required_bosses", player));
                    event.setCancelled(true);
                }
            }
        }
    }
}

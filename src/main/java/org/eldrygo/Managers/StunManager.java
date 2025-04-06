package org.eldrygo.Managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.DepUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.GameMode.SPECTATOR;

public class StunManager implements Listener {
    private final Set<UUID> stunnedPlayers = new HashSet<>();
    private final SpeedrunBoss plugin;
    private final XTeamsAPI xTeamsAPI;
    private final DepUtils depUtils;

    public StunManager(SpeedrunBoss plugin, XTeamsAPI xTeamsAPI, DepUtils depUtils) {
        this.plugin = plugin;
        this.xTeamsAPI = xTeamsAPI;
        this.depUtils = depUtils;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Aturde a un jugador por un tiempo determinado.
     *
     * @param player El jugador a aturdir.
     * @param durationTicks Duración del aturdimiento en ticks (20 ticks = 1 segundo).
     */
    public void stunPlayer(Player player, long durationTicks) {
        if (player == null || stunnedPlayers.contains(player.getUniqueId())) return;

        stunnedPlayers.add(player.getUniqueId());

        new BukkitRunnable() {
            @Override
            public void run() {
                unStunPlayer(player);
            }
        }.runTaskLater(plugin, durationTicks);
    }

    /**
     * Aturde a un jugador hasta que se desactive manualmente.
     *
     * @param player El jugador a aturdir.
     */
    public void stunPlayerIndefinitely(Player player) {
        if (player == null || stunnedPlayers.contains(player.getUniqueId())) return;

        stunnedPlayers.add(player.getUniqueId());
    }
    public void stunAllPlayers() {
        for (Player players : Bukkit.getOnlinePlayers()) {
            String vaultGroup = depUtils.getPrimaryGroup(players);
            String teamName = xTeamsAPI.getPlayerTeamName(players);
            if (!(players.getGameMode() == SPECTATOR) && !(vaultGroup.contains("team_")) && !(teamName == null) && !(teamName.isEmpty())) { // Si NO cumple la condición, lo stuneamos
                stunPlayerIndefinitely(players);
            }
        }
    }
    public void unStunAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            unStunPlayer(player);
        }
    }

    /**
     * Quita el aturdimiento de un jugador.
     *
     * @param player El jugador a liberar del aturdimiento.
     */
    public void unStunPlayer(Player player) {
        if (player == null || !stunnedPlayers.contains(player.getUniqueId())) return;

        stunnedPlayers.remove(player.getUniqueId());
    }

    /**
     * Evento para evitar que los jugadores aturdidos se muevan.
     */
    public void onPlayerMove(PlayerMoveEvent event) {
        if (stunnedPlayers.contains(event.getPlayer().getUniqueId())) {
            // Comparar las coordenadas completas, incluyendo las decimales
            if (event.getFrom().getX() != event.getTo().getX() ||
                    event.getFrom().getY() != event.getTo().getY() ||
                    event.getFrom().getZ() != event.getTo().getZ()) {
                event.setCancelled(true); // Cancela el movimiento si hay cualquier diferencia
            }
        }
    }

    /**
     * Evento para evitar que los jugadores aturdidos interactúen.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (stunnedPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    /**
     * Evento para evitar que los jugadores aturdidos ataquen a otros.
     */
    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (stunnedPlayers.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }


    /**
     * Verifica si un jugador está aturdido.
     *
     * @param player El jugador a comprobar.
     * @return `true` si está aturdido, `false` en caso contrario.
     */
    public boolean isPlayerStunned(Player player) {
        return player != null && stunnedPlayers.contains(player.getUniqueId());
    }
}

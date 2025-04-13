package org.eldrygo.Managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.eldrygo.SpeedrunBoss;
import org.eldrygo.Utils.PlayerUtils;
import org.eldrygo.XTeams.API.XTeamsAPI;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.bukkit.GameMode.SPECTATOR;

public class StunManager implements Listener {
    private final Set<UUID> stunnedPlayers = new HashSet<>();
    private final SpeedrunBoss plugin;
    private final XTeamsAPI xTeamsAPI;
    public PlayerUtils playerUtils;

    public StunManager(SpeedrunBoss plugin, XTeamsAPI xTeamsAPI, PlayerUtils playerUtils) {
        this.plugin = plugin;
        this.xTeamsAPI = xTeamsAPI;
        this.playerUtils = playerUtils;
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
        plugin.getLogger().info("✅ " + player.getName() + " now is stunned.");

        new BukkitRunnable() {
            @Override
            public void run() {
                unStunPlayer(player);
                plugin.getLogger().info("✅ " + player.getName() + " now is not stunned.");
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
        plugin.getLogger().info("✅ Stunning all players...");
        for (Player players : Bukkit.getOnlinePlayers()) {
            String vaultGroup = playerUtils.getPrimaryGroup(players);
            String teamName = xTeamsAPI.getPlayerTeamName(players);
            if (!(players.getGameMode() == SPECTATOR) && !(vaultGroup.contains("team_")) && !(teamName == null) && !(teamName.isEmpty())) {
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
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (stunnedPlayers.contains(event.getPlayer().getUniqueId())) {
            if (event.getFrom().getX() != event.getTo().getX() ||
                    event.getFrom().getZ() != event.getTo().getZ()) {

                // Restaurar la posición en X y Z, pero permitir la Y y la rotación
                Location from = event.getFrom().clone();
                Location to = event.getTo().clone();

                to.setX(from.getX());
                to.setZ(from.getZ());

                // Mantiene la Y y la rotación (pitch/yaw)
                event.setTo(to);
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


}

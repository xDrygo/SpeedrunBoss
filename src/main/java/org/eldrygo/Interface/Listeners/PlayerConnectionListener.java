package org.eldrygo.Interface.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.eldrygo.Managers.BroadcastManager;
import org.eldrygo.Utils.PlayerUtils;

public class PlayerConnectionListener implements Listener {
    private final BroadcastManager broadcastManager;
    private final PlayerUtils playerUtils;

    public PlayerConnectionListener(BroadcastManager broadcastManager, PlayerUtils playerUtils) {
        this.broadcastManager = broadcastManager;
        this.playerUtils = playerUtils;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(null);

        playerUtils.updatePlayerGroupBasedOnTeam(player);

        if (!player.hasPermission("spb.bypass_connectionmessage")) {
            broadcastManager.sendPlayerConnectionMessage(player, "join");
        } else {
            broadcastManager.sendAdminConnectionMessage(player, "join");
        }
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);

        if (!player.hasPermission("spb.bypass_connectionmessage")) {
            broadcastManager.sendPlayerConnectionMessage(player, "leave");
        } else {
            broadcastManager.sendAdminConnectionMessage(player, "leave");
        }
    }
}

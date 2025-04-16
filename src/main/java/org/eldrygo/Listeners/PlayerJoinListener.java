package org.eldrygo.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.eldrygo.Utils.PlayerUtils;

public class PlayerJoinListener implements Listener {
    private final PlayerUtils playerUtils;

    public PlayerJoinListener(PlayerUtils playerUtils) {
        this.playerUtils = playerUtils;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        playerUtils.updatePlayerGroupBasedOnTeam(player);
    }
}

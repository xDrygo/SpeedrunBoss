package org.eldrygo.Time.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.eldrygo.Time.Managers.TimeBarManager;

public class TimeBarJoinListener implements Listener {

    private final TimeBarManager timeBarManager;

    public TimeBarJoinListener(TimeBarManager timeBarManager) {
        this.timeBarManager = timeBarManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (timeBarManager.isBarsVisible()) {
            timeBarManager.addPlayerToBars(event.getPlayer());
        }
    }
}

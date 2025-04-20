package org.eldrygo.Cinematics.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.eldrygo.Cinematics.Managers.CinematicManager;
import org.eldrygo.Cinematics.Models.CinematicSequence;

public class CinematicListener implements Listener {
    private final CinematicManager cinematicManager;

    public CinematicListener(CinematicManager cinematicManager) {
        this.cinematicManager = cinematicManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        CinematicSequence sequence = cinematicManager.getRunningSequence();
        if (sequence != null) {
            sequence.addPlayer(event.getPlayer());
        }
    }
}

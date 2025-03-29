package org.eldrygo.Modifiers.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.eldrygo.Modifiers.Managers.ModifierManager;

public class PvpListener implements Listener {
    private final ModifierManager modifierManager;

    public PvpListener(ModifierManager modifierManager) {
        this.modifierManager = modifierManager;
    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        // Verificamos que ambas entidades sean jugadores
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            if (modifierManager.isPvpEnabled()) {
                event.setCancelled(true);
            }
        }
    }
}
